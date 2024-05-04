package me.eglp.gv2.util.selfcheck;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.main.task.GraphiteTask;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.text.CommandHandler;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.lang.DefaultMessage;
import me.eglp.gv2.util.lang.LocalizedString;
import me.eglp.gv2.util.lang.defaults.DefaultLocale;
import me.eglp.gv2.util.lang.defaults.DefaultLocales;
import me.eglp.gv2.util.permission.DefaultPermissions;
import me.eglp.gv2.util.selfcheck.checks.GraphiteTasksCheck;
import me.eglp.gv2.util.versioning.Beta;
import me.eglp.gv2.util.webinterface.GraphiteWebinterface;
import me.eglp.gv2.util.webinterface.WebinterfaceHandler;
import me.eglp.gv2.util.webinterface.base.WebinterfaceRequestEvent;
import me.eglp.gv2.util.webinterface.base.WebinterfaceResponse;
import me.mrletsplay.mrcore.config.ConfigValueType;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.mrcore.misc.classfile.ClassField;
import me.mrletsplay.mrcore.misc.classfile.ClassFile;
import me.mrletsplay.mrcore.misc.classfile.ClassMethod;
import me.mrletsplay.mrcore.misc.classfile.FieldAccessFlag;
import me.mrletsplay.mrcore.misc.classfile.Instruction;
import me.mrletsplay.mrcore.misc.classfile.annotation.Annotation;
import me.mrletsplay.mrcore.misc.classfile.annotation.value.AnnotationElementBooleanValue;
import me.mrletsplay.mrcore.misc.classfile.attribute.AttributeCode;
import me.mrletsplay.mrcore.misc.classfile.attribute.AttributeRuntimeVisibleAnnotations;
import me.mrletsplay.mrcore.misc.classfile.attribute.DefaultAttributeType;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolFieldRefEntry;
import me.mrletsplay.mrcore.misc.classfile.pool.entry.ConstantPoolMethodRefEntry;

public class Selfcheck {

	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<name>.+?)\\}");

	private static GraphiteTask periodicCheck;
	private static List<GraphiteCheck> checks;

	static {
		checks = new ArrayList<>();
		checks.add(new GraphiteTasksCheck());
	}

	public static void startPeriodicCheck() {
		periodicCheck = Graphite.getScheduler().scheduleWithFixedDelay("selfcheck-periodic", () -> {
			for(GraphiteCheck check : checks) {
				check.runCheck();
			}
		}, 10000);
	}

	public static GraphiteTask getPeriodicCheckTask() {
		return periodicCheck;
	}

	public static List<GraphiteCheck> getChecks() {
		return checks;
	}

	public static void runFullCheck() {
		Graphite.log("Running selfcheck...");
		runLocaleTest();
		runPermissionTest();
		runCommandTest();
		checkUnusedMessages();
		checkIncorrectCode();
		checkWIHandlers();
		runDefaultLocalesTest();
		generateLocaleDescriptorFiles();
		generateSQLSchema();
	}

	public static void runLocaleTest() {
		List<LocalizedString> allStrs = new ArrayList<>();
		allStrs.addAll(Arrays.asList(DefaultLocaleString.values()));
		allStrs.addAll(Arrays.asList(DefaultMessage.values()));
		List<LocalizedString> alreadySeen = new ArrayList<>();
		for(LocalizedString str : allStrs) {
			String mpth = str.getMessagePath();
			for(LocalizedString seen : alreadySeen) {
				if(mpth.equals(seen.getMessagePath())) {
					Graphite.log("Path of " + str + " \"" + mpth + "\" is equal to " + seen + " \"" + seen.getMessagePath() + "\"");
				}else if(isParent(mpth, seen.getMessagePath())) {
					Graphite.log("Path of " + str + " \"" + mpth + "\" is parent of " + seen + " \"" + seen.getMessagePath() + "\"");
				}else if(isParent(seen.getMessagePath(), mpth)) {
					Graphite.log("Path of " + str + " \"" + mpth + "\" is child of " + seen + " \"" + seen.getMessagePath() + "\"");
				}

				if(str.getFallback().equalsIgnoreCase(seen.getFallback())) {
					Graphite.log("Duplicate message for " + str + " and " + seen + ": \"" + seen.getFallback() + "\"");
				}
			}

			if(!mpth.replace('.', '_').replace('-', '_').toUpperCase().equals(((Enum<?>) str).name())) {
				Graphite.log("Path of " + str + " (" + mpth + ") doesn't make sense");
			}

			if(str.getFallback().isBlank()) {
				Graphite.log("Fallback for " + str + " is empty");
			}

			Matcher m = PLACEHOLDER_PATTERN.matcher(str.getFallback());
			while(m.find()) {
				String ph = m.group("name");
				if(ph.contains("-")) Graphite.log(str + " contains placeholders in an invalid format: " + ph);
			}

			alreadySeen.add(str);
		}
	}

	public static void runDefaultLocalesTest() {
		Set<String> defaultLocales = DefaultLocales.getDefaultLocaleKeys();

		for(String l : defaultLocales) {
			DefaultLocale df = DefaultLocales.getDefaultLocale(l);
			Set<String> paths = new HashSet<>();
			for(String pathInLocale : df.getMessageConfig().getKeys(true, true)) {
				if(df.getMessageConfig().getTypeOf(pathInLocale) != ConfigValueType.STRING) continue;
				if(!isValidMessagePath(pathInLocale)) paths.add(pathInLocale);
			}

			Set<String> allPaths = new HashSet<>();
			Arrays.stream(DefaultLocaleString.values()).forEach(d -> allPaths.add(d.getMessagePath()));
			Arrays.stream(DefaultMessage.values()).forEach(d -> allPaths.add(d.getMessagePath()));
			if(!paths.isEmpty()) {
				Graphite.log("Default locale \"" + l + "\" contains unused messages:");
				paths.forEach(p -> Graphite.log("\t- " + p));

				File clean = new File("SELFCHECK/" + l + ".yml");
				Graphite.log("A cleaned version will be written to " + clean.getPath());
				IOUtils.createFile(clean);
				paths.forEach(p -> df.getMessageConfig().unset(p));
				df.getMessageConfig().save(clean);
			}
		}
	}

	public static void generateLocaleDescriptorFiles() {
		// Collect CLASS -> REFERENCED_ENUMS

		Map<String, List<DefaultLocaleString>> classesToEnums1 = findEnumReferencesAdvanced(DefaultLocaleString.class);
		Map<String, List<DefaultMessage>> classesToEnums2 = findEnumReferencesAdvanced(DefaultMessage.class);

		// Reverse the two maps to have ENUM -> REFERENCING_CLASSES

		Map<LocalizedString, List<String>> enumsToClasses = new HashMap<>();

		BiConsumer<String, List<? extends LocalizedString>> reversor = (cls, ens) -> {
			ens.forEach(en -> {
				List<String> refCls = enumsToClasses.getOrDefault(en, new ArrayList<>());
				refCls.add(cls);
				enumsToClasses.put(en, refCls);
			});
		};

		classesToEnums1.forEach(reversor);
		classesToEnums2.forEach(reversor);

		Map<CommandCategory, List<Command>> cmds = new HashMap<>();
		for(Command c : CommandHandler.getCommands()) CommandHandler.addCommands(c, cmds);

		List<Command> allCommandsWithSubCommands = cmds.values().stream()
				.flatMap(l -> l.stream())
				.collect(Collectors.toList());

		JSONArray allEnums = new JSONArray();
		enumsToClasses.forEach((en, refCls) -> {
			JSONObject thisEnum = new JSONObject();
			thisEnum.put("path", en.getMessagePath());
			thisEnum.put("enum_name", ((Enum<?>) en).name());
			thisEnum.put("fallback", en.getFallback());
			thisEnum.put("referenced_by", new JSONArray(refCls));

			JSONArray specialRoles = new JSONArray();
			Command descOf = allCommandsWithSubCommands.stream()
					.filter(c -> c.getDescription() == en)
					.findFirst().orElse(null);
			Command usageOf = allCommandsWithSubCommands.stream()
					.filter(c -> c.getUsage() == en)
					.findFirst().orElse(null);

			if(descOf != null) specialRoles.add("Command description of \"" + descOf.getFullName() + "\"");
			if(usageOf != null) specialRoles.add("Command usage of \"" + usageOf.getFullName() + "\"");

			thisEnum.put("special_roles", specialRoles);

			JSONArray placeholders = new JSONArray();
			Matcher m = PLACEHOLDER_PATTERN.matcher(en.getFallback());
			while(m.find()) {
				String ph = m.group("name");
				if(ph.startsWith("emote_")) continue; // Ignore JDAEmotes
				placeholders.add(ph);
			}
			thisEnum.put("placeholders", placeholders);

			allEnums.add(thisEnum);
		});

		File localeDescFile = new File("SELFCHECK/locale-descriptor.json");
		IOUtils.writeBytes(localeDescFile, allEnums.toFancyString().getBytes(StandardCharsets.UTF_8));
	}

	public static void generateSQLSchema() {
		try {
			List<String> schemas = new ArrayList<>();
			Graphite.getMySQL().getTables().forEach(t -> schemas.add(t.createQuery()));
			File schemaFile = new File("SELFCHECK/schema.sql");
			IOUtils.writeBytes(schemaFile, schemas.stream().collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new FriendlyException(e);
		}
	}

	private static boolean isValidMessagePath(String path) {
		return DefaultMessage.getByPath(path) != null || DefaultLocaleString.getByPath(path) != null;
	}

	public static void runPermissionTest() {
		for(Field f : DefaultPermissions.class.getDeclaredFields()) {
			try {
				if(!f.getName().replace("_", ".").toLowerCase().equals(((String) f.get(null)).replace("-", "."))) {
					Graphite.log("Path of " + f.getName() + " (" + f.get(null) + ") doesn't make sense");
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static void checkIncorrectCode() {
		File f = new File(Graphite.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		try {
			checkIncorrectCode(f);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isParent(String parent, String child) {
		String[] p1 = parent.split("\\.");
		String[] p2 = child.split("\\.");
		for(int i = 0; i < Math.min(p1.length, p2.length); i++) {
			if(!p1[i].equals(p2[i])) return false;
		}
		return true;
	}

	public static void runCommandTest() {
		Map<Command, List<String>> results = new LinkedHashMap<>();
		for(Command c : CommandHandler.getCommands()) {
			testCommandRecusively(c).forEach(results::put);
		}

		for(Map.Entry<Command, List<String>> en : results.entrySet()) {
			if(en.getValue().isEmpty()) continue;
			Graphite.log("Command: " + en.getKey().getFullName());
			for(String r : en.getValue()) {
				Graphite.log("	" + r);
			}
		}
	}

	private static Map<Command, List<String>> testCommandRecusively(Command command) {
		Map<Command, List<String>> results = new LinkedHashMap<>();
		Beta o = command.getAnnotation(Beta.class);
		if(o != null) return results;
		results.put(command, testCommand(command));
		for(Command c : command.getSubCommands()) {
			testCommandRecusively(c).forEach(results::put);
		}
		return results;
	}

	private static List<String> testCommand(Command c) {
		List<String> r = new ArrayList<>();
		SpecialSelfcheck s = c.getAnnotation(SpecialSelfcheck.class);

		if(c.getDescription() == null) {
			r.add("Description missing");
		}else {
			if(!(c.getDescription() instanceof Enum<?>)) {
				r.add("Description is not an enum value, but " + c.getDescription().getClass().getName());
			}else {
				String descName = ((Enum<?>) c.getDescription()).name();
				if(!descName.equals("COMMAND_" + c.getFullName().replace(' ', '_').toUpperCase() + "_DESCRIPTION")) {
					r.add("Description enum name (" + descName + ") doesn't make sense");
				}
			}
		}

		if(c.getUsage() == null && c.getSubCommands().isEmpty()) {
			r.add("Usage missing");
		}

		if(c.getUsage() != null) {
			if(!(c.getUsage() instanceof Enum<?>)) {
				r.add("Usage is not an enum value, but " + c.getUsage().getClass().getName());
			}else {
				String usgName = ((Enum<?>) c.getUsage()).name();
				if(!usgName.equals("COMMAND_" + c.getFullName().replace(' ', '_').toUpperCase() + "_USAGE")) {
					r.add("Usage enum name (" + usgName + ") doesn't make sense");
				}
			}
		}

		if(c.getPermission() == null && c.allowsServer() && c.getSubCommands().isEmpty() && (s == null || s.needsPermission())) r.add("Permission missing");

		return r;
	}

	private static void checkUnusedMessages() {
		List<DefaultMessage> msgs = checkUnusedEnumFields(DefaultMessage.class);
		if(!msgs.isEmpty()) {
			Graphite.log("There are unused messages:");
			msgs.forEach(m -> Graphite.log("\t- " + m.name()));
		}

		List<DefaultLocaleString> strs = checkUnusedEnumFields(DefaultLocaleString.class);
		if(!strs.isEmpty()) {
			Graphite.log("There are unused locale strings:");
			strs.forEach(m -> Graphite.log("\t- " + m.name()));
		}
	}

	private static <T extends Enum<T>> List<T> checkUnusedEnumFields(Class<T> enumClass) {
		try {
			File f = new File(Graphite.class.getProtectionDomain().getCodeSource().getLocation().getFile());
			String className = enumClass.getCanonicalName().replace('.', '/');
			ClassFile msgsClass = new ClassFile(new File(f, className + ".class"));

			for(ClassField fl : msgsClass.getFields()) {
				if(!fl.getAccessFlags().hasFlag(FieldAccessFlag.ENUM)) continue;
			}

			Set<String> found = new HashSet<>();
			findEnumReferences(f, className, found);

			List<T> msgs = new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
			msgs.removeIf(m -> found.contains(m.name()));
			return msgs;
		} catch (Exception e) {
			throw new FriendlyException(e);
		}
	}

	private static void findEnumReferences(File f, String className, Set<String> found) throws Exception {
		if(f.isFile()) {
			if(!f.getName().endsWith(".class")) return;
			ClassFile cf = new ClassFile(f);
			if(cf.getThisClass().getName().getValue().equals(className)) return;
			for(ClassMethod m : cf.getMethods()) {
				AttributeCode code = (AttributeCode) m.getAttribute(DefaultAttributeType.CODE);
				if(code == null) continue;
				try {
					code.getCode().parseCode().forEach(instr -> {
						if(instr.getInstruction() == Instruction.GETSTATIC) {
							int index = ((instr.getInformation()[0] & 0xff) << 8) | (instr.getInformation()[1] & 0xff);
							ConstantPoolFieldRefEntry fr = cf.getConstantPool().getEntry(index).as(ConstantPoolFieldRefEntry.class);
							if(fr.getClassInfo().getName().getValue().equals(className)) {
								found.add(fr.getNameAndType().getName().getValue());
							}
						}
					});
				}catch(Exception e) {
					code.getCode().parseCode().forEach(System.out::println);
					throw e;
				}
			}
		}else {
			for(File fl : f.listFiles()) findEnumReferences(fl, className, found);
		}
	}

	private static <T extends Enum<T>> Map<String, List<T>> findEnumReferencesAdvanced(Class<T> enumClass) {
		try {
			File f = new File(Graphite.class.getProtectionDomain().getCodeSource().getLocation().getFile());
			String className = enumClass.getCanonicalName().replace('.', '/');

			Map<String, List<String>> classesToEnums = new HashMap<>();
			findEnumReferencesAdvanced(f, className, classesToEnums);

			List<T> allEnums = Arrays.asList(enumClass.getEnumConstants());

			return classesToEnums.entrySet().stream()
					.collect(Collectors.toMap(
							en -> en.getKey(), // Class
							en -> en.getValue().stream() // Referenced enums
							.map(v -> allEnums.stream().filter(en2 -> en2.name().equals(v)).findFirst().get()) // Get enum with the specified name from the list of all enums
							.collect(Collectors.toList())));
		} catch (Exception e) {
			throw new FriendlyException(e);
		}
	}

	private static void findEnumReferencesAdvanced(File f, String className, Map<String, List<String>> classesToEnums) throws Exception {
		if(f.isFile()) {
			if(!f.getName().endsWith(".class")) return;
			ClassFile cf = new ClassFile(f);
			if(cf.getThisClass().getName().getValue().equals(className)) return;
			List<String> found = new ArrayList<>();
			for(ClassMethod m : cf.getMethods()) {
				AttributeCode code = (AttributeCode) m.getAttribute(DefaultAttributeType.CODE);
				if(code == null) continue;
				try {
					code.getCode().parseCode().forEach(instr -> {
						if(instr.getInstruction() == Instruction.GETSTATIC) {
							int index = ((instr.getInformation()[0] & 0xff) << 8) | (instr.getInformation()[1] & 0xff);
							ConstantPoolFieldRefEntry fr = cf.getConstantPool().getEntry(index).as(ConstantPoolFieldRefEntry.class);
							if(fr.getClassInfo().getName().getValue().equals(className)) {
								found.add(fr.getNameAndType().getName().getValue());
							}
						}
					});
				}catch(Exception e) {
					code.getCode().parseCode().forEach(System.out::println);
					throw e;
				}
			}
			classesToEnums.put(cf.getThisClass().getName().getValue(), found);
		}else {
			for(File fl : f.listFiles()) findEnumReferencesAdvanced(fl, className, classesToEnums);
		}
	}

	private static void checkIncorrectCode(File f) throws Exception {
		if(f.isFile()) {
			if(!f.getName().endsWith(".class")) return;
			ClassFile cf = new ClassFile(f);
			for(ClassMethod m : cf.getMethods()) {
				AttributeCode code = (AttributeCode) m.getAttribute(DefaultAttributeType.CODE);
				if(code == null) continue;
				try {
					code.getCode().parseCode().forEach(instr -> {
						if(instr.getInstruction() == Instruction.INVOKEVIRTUAL) {
							int index = ((instr.getInformation()[0] & 0xff) << 8) | (instr.getInformation()[1] & 0xff);
							ConstantPoolMethodRefEntry ref = cf.getConstantPool().getEntry(index).as(ConstantPoolMethodRefEntry.class);

							if(ref.getClassInfo().getName().getValue().equals("me/eglp/gv2/util/webinterface/base/WebinterfaceRequestEvent")) {
								AttributeRuntimeVisibleAnnotations a = (AttributeRuntimeVisibleAnnotations) m.getAttribute(DefaultAttributeType.RUNTIME_VISIBLE_ANNOTATIONS);
								if(a != null) {
									Annotation an = Arrays.stream(a.getAnnotations())
											.filter(o -> o.getType().getClassName().equals("me.eglp.gv2.util.webinterface.WebinterfaceHandler"))
											.findFirst().orElse(null);

									if(an != null) {
										AnnotationElementBooleanValue requireUser = (AnnotationElementBooleanValue) an.getElementValue("requireUser");
										AnnotationElementBooleanValue requireGuild = (AnnotationElementBooleanValue) an.getElementValue("requireGuild");

										if(requireUser != null && !requireUser.getValue() && ref.getNameAndType().getName().getValue().equals("getUser")) {
											Graphite.log("Method " + m + " in class " + cf.getThisClass().getName().getValue() + " calls getUser() despite having requireUser = false");
										}

										if((requireGuild == null || !requireGuild.getValue()) && ref.getNameAndType().getName().getValue().equals("getSelectedGuild")) {
											Graphite.log("Method " + m + " in class " + cf.getThisClass().getName().getValue() + " calls getSelectedGuild() despite having requireGuild = false");
										}
									}
								}
							}
						}
					});
				}catch(Exception e) {
					code.getCode().parseCode().forEach(System.out::println);
					throw e;
				}
			}
		}else {
			for(File fl : f.listFiles()) checkIncorrectCode(fl);
		}
	}

	private static void checkWIHandlers() {
		for(Method m : GraphiteWebinterface.getHandlerMethods()) {
			if(m.isAnnotationPresent(WebinterfaceHandler.class)) {
				WebinterfaceHandler wh = m.getAnnotation(WebinterfaceHandler.class);
				SpecialSelfcheck sCh = m.getAnnotation(SpecialSelfcheck.class);

				if(!Modifier.isStatic(m.getModifiers()) ||
						!Arrays.equals(m.getParameterTypes(), new Class[] {WebinterfaceRequestEvent.class}) ||
						!m.getReturnType().equals(WebinterfaceResponse.class)) {
					System.out.println("Request handler \"" + m.getName() + "\" in class " + m.getDeclaringClass().getSimpleName() + " does not match the format \"public static WebinterfaceResponse handler(WebinterfaceRequestEvent event)\"");
				}

				if((wh.requireGuildAdmin() || wh.requirePermissions().length > 0) && !wh.requireGuild()) {
					System.out.println("Request handler \"" + m.getName() + "\" in class " + m.getDeclaringClass().getSimpleName() + " requires admin but not a guild");
				}

				if(!wh.requireGuildAdmin() && wh.requirePermissions().length == 0 && (sCh == null || !sCh.ignoreAccessibleToEveryone())) {
					System.out.println("Request handler \"" + m.getName() + "\" in class " + m.getDeclaringClass().getSimpleName() + " is accessible to everyone without explicit declaration using @SpecialSelfcheck");
				}
			}
		}
	}

}
