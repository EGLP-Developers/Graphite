package me.eglp.gv2.util.base.guild.reminder;

import static java.lang.System.getLogger;
import static java.time.temporal.ChronoField.AMPM_OF_DAY;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.CLOCK_HOUR_OF_DAY;
import static java.time.temporal.ChronoField.EPOCH_DAY;
import static java.time.temporal.ChronoField.HOUR_OF_AMPM;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MICRO_OF_DAY;
import static java.time.temporal.ChronoField.MICRO_OF_SECOND;
import static java.time.temporal.ChronoField.MILLI_OF_DAY;
import static java.time.temporal.ChronoField.MILLI_OF_SECOND;
import static java.time.temporal.ChronoField.MINUTE_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_DAY;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.OFFSET_SECONDS;
import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.lang.System.Logger;
import java.lang.reflect.InvocationTargetException;
import java.text.spi.BreakIteratorProvider;
import java.text.spi.CollatorProvider;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.chrono.Chronology;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DecimalStyle;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeFormatterBuilder.DayPeriod;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CalendarNameProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.LocaleServiceProvider;
import java.util.spi.TimeZoneNameProvider;

import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.GraphiteUtil;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.eglp.gv2.util.base.guild.GraphiteGuildMessageChannel;
import me.eglp.gv2.util.webinterface.js.JavaScriptClass;
import me.eglp.gv2.util.webinterface.js.JavaScriptFunction;
import me.eglp.gv2.util.webinterface.js.JavaScriptParameter;
import me.eglp.gv2.util.webinterface.js.WebinterfaceObject;
import me.mrletsplay.mrcore.json.JSONObject;
import sun.security.action.GetPropertyAction;
import sun.text.spi.JavaTimeDateTimePatternProvider;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.JRELocaleConstants;
import sun.util.locale.provider.LocaleProviderAdapter;
import sun.util.locale.provider.LocaleResources;
import sun.util.locale.provider.LocaleProviderAdapter.Type;
import sun.util.spi.CalendarProvider;

/**
 * This is the reminders executive class. It creates a Timer
 * {@link ScheduledFuture} to execute, when the timer is due and send the
 * appropriate message
 * 
 * @author The Arrayser
 * @date Mon Mar 27 20:11:34 2023
 */

@JavaScriptClass(name = "GuildReminder")
public class GuildReminder implements WebinterfaceObject {
	public class JRELocaleConstants {
	    public static final Locale JA_JP_JP = new Locale("ja", "JP", "JP");
	    public static final Locale NO_NO_NY = new Locale("no", "NO", "NY");
	    public static final Locale TH_TH    = new Locale("th", "TH");
	    public static final Locale TH_TH_TH = new Locale("th", "TH", "TH");

	    private JRELocaleConstants() {
	    }
	}
	
	public abstract class LocaleProviderAdapter {
	    /**
	     * Adapter type.
	     */
	    public enum Type {
	        JRE("sun.util.locale.provider.JRELocaleProviderAdapter", "sun.util.resources", "sun.text.resources"),
	        CLDR("sun.util.cldr.CLDRLocaleProviderAdapter", "sun.util.resources.cldr", "sun.text.resources.cldr"),
	        SPI("sun.util.locale.provider.SPILocaleProviderAdapter"),
	        HOST("sun.util.locale.provider.HostLocaleProviderAdapter"),
	        FALLBACK("sun.util.locale.provider.FallbackLocaleProviderAdapter", "sun.util.resources", "sun.text.resources");

	        private final String CLASSNAME;
	        private final String UTIL_RESOURCES_PACKAGE;
	        private final String TEXT_RESOURCES_PACKAGE;

	        Type(String className) {
	            this(className, null, null);
	        }

	        Type(String className, String util, String text) {
	            CLASSNAME = className;
	            UTIL_RESOURCES_PACKAGE = util;
	            TEXT_RESOURCES_PACKAGE = text;
	        }

	        public String getAdapterClassName() {
	            return CLASSNAME;
	        }

	        public String getUtilResourcesPackage() {
	            return UTIL_RESOURCES_PACKAGE;
	        }

	        public String getTextResourcesPackage() {
	            return TEXT_RESOURCES_PACKAGE;
	        }
	    }

	    /**
	     * LocaleProviderAdapter preference list.
	     */
	    private static final List<Type> adapterPreference;

	    /**
	     * LocaleProviderAdapter instances
	     */
	    private static final Map<Type, LocaleProviderAdapter> adapterInstances = new ConcurrentHashMap<>();

	    /**
	     * Default fallback adapter type, which should return something meaningful in any case.
	     * This is either CLDR or FALLBACK.
	     */
	    static volatile LocaleProviderAdapter.Type defaultLocaleProviderAdapter;

	    /**
	     * Adapter lookup cache.
	     */
	    private static final ConcurrentMap<Class<? extends LocaleServiceProvider>, ConcurrentMap<Locale, LocaleProviderAdapter>>
	        adapterCache = new ConcurrentHashMap<>();

	    static {
	        String order = GetPropertyAction.privilegedGetProperty("java.locale.providers");
	        ArrayList<Type> typeList = new ArrayList<>();
	        String invalidTypeMessage = null;

	        // Check user specified adapter preference
	        if (order != null && !order.isEmpty()) {
	            String[] types = order.split(",");
	            for (String type : types) {
	                type = type.trim().toUpperCase(Locale.ROOT);
	                if (type.equals("COMPAT")) {
	                    type = "JRE";
	                }
	                try {
	                    Type aType = Type.valueOf(type.trim().toUpperCase(Locale.ROOT));
	                    if (!typeList.contains(aType)) {
	                        typeList.add(aType);
	                    }
	                } catch (IllegalArgumentException e) {
	                    // construct a log message.
	                    invalidTypeMessage = "Invalid locale provider adapter \"" + type + "\" ignored.";
	                }
	            }
	        }

	        defaultLocaleProviderAdapter = Type.CLDR;
	        if (!typeList.isEmpty()) {
	            // bona fide preference exists
	            if (!(typeList.contains(Type.CLDR) || typeList.contains(Type.JRE))) {
	                // Append FALLBACK as the last resort when no ResourceBundleBasedAdapter is available.
	                typeList.add(Type.FALLBACK);
	                defaultLocaleProviderAdapter = Type.FALLBACK;
	            }
	        } else {
	            // Default preference list.
	            typeList.add(Type.CLDR);
	            typeList.add(Type.JRE);
	        }
	        adapterPreference = Collections.unmodifiableList(typeList);

	        // Emit logs, if any, after 'adapterPreference' is initialized which is needed
	        // for logging.
	        if (invalidTypeMessage != null) {
	            // could be caused by the user specifying wrong
	            // provider name or format in the system property
	            getLogger(LocaleProviderAdapter.class.getCanonicalName())
	                .log(Logger.Level.INFO, invalidTypeMessage);
	        }
	    }

	    /**
	     * Returns the singleton instance for each adapter type
	     */
	    public static LocaleProviderAdapter forType(Type type) {
	        switch (type) {
	        case JRE:
	        case CLDR:
	        case SPI:
	        case HOST:
	        case FALLBACK:
	            LocaleProviderAdapter adapter = adapterInstances.get(type);
	            if (adapter == null) {
	                try {
	                    // lazily load adapters here
	                    adapter = (LocaleProviderAdapter)Class.forName(type.getAdapterClassName())
	                            .getDeclaredConstructor().newInstance();
	                    LocaleProviderAdapter cached = adapterInstances.putIfAbsent(type, adapter);
	                    if (cached != null) {
	                        adapter = cached;
	                    }
	                } catch (NoSuchMethodException |
	                         InvocationTargetException |
	                         ClassNotFoundException |
	                         IllegalAccessException |
	                         InstantiationException |
	                         UnsupportedOperationException e) {
	                    throw new ServiceConfigurationError("Locale provider adapter \"" +
	                            type + "\"cannot be instantiated.", e);
	                }
	            }
	            return adapter;
	        default:
	            throw new InternalError("unknown locale data adapter type");
	        }
	    }

	    public static LocaleProviderAdapter forJRE() {
	        return forType(Type.JRE);
	    }

	    public static LocaleProviderAdapter getResourceBundleBased() {
	        for (Type type : getAdapterPreference()) {
	            if (type == Type.JRE || type == Type.CLDR || type == Type.FALLBACK) {
	                LocaleProviderAdapter adapter = forType(type);
	                if (adapter != null) {
	                    return adapter;
	                }
	            }
	        }
	        // Shouldn't happen.
	        throw new InternalError();
	    }

	    /**
	     * Returns the preference order of LocaleProviderAdapter.Type
	     */
	    public static List<Type> getAdapterPreference() {
	        return adapterPreference;
	    }

	    /**
	     * Returns a LocaleProviderAdapter for the given locale service provider that
	     * best matches the given locale. This method returns the LocaleProviderAdapter
	     * for JRE if none is found for the given locale.
	     *
	     * @param providerClass the class for the locale service provider
	     * @param locale the desired locale.
	     * @return a LocaleProviderAdapter
	     */
	    public static LocaleProviderAdapter getAdapter(Class<? extends LocaleServiceProvider> providerClass,
	                                               Locale locale) {
	        LocaleProviderAdapter adapter;

	        // cache lookup
	        ConcurrentMap<Locale, LocaleProviderAdapter> adapterMap = adapterCache.get(providerClass);
	        if (adapterMap != null) {
	            if ((adapter = adapterMap.get(locale)) != null) {
	                return adapter;
	            }
	        } else {
	            adapterMap = new ConcurrentHashMap<>();
	            adapterCache.putIfAbsent(providerClass, adapterMap);
	        }

	        // Fast look-up for the given locale
	        adapter = findAdapter(providerClass, locale);
	        if (adapter != null) {
	            adapterMap.putIfAbsent(locale, adapter);
	            return adapter;
	        }

	        // Try finding an adapter in the normal candidate locales path of the given locale.
	        List<Locale> lookupLocales = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT)
	                                        .getCandidateLocales("", locale);
	        for (Locale loc : lookupLocales) {
	            if (loc.equals(locale)) {
	                // We've already done with this loc.
	                continue;
	            }
	            adapter = findAdapter(providerClass, loc);
	            if (adapter != null) {
	                adapterMap.putIfAbsent(locale, adapter);
	                return adapter;
	            }
	        }

	        // returns the adapter for FALLBACK as the last resort
	        adapterMap.putIfAbsent(locale, forType(Type.FALLBACK));
	        return forType(Type.FALLBACK);
	    }

	    private static LocaleProviderAdapter findAdapter(Class<? extends LocaleServiceProvider> providerClass,
	                                                 Locale locale) {
	        for (Type type : getAdapterPreference()) {
	            LocaleProviderAdapter adapter = forType(type);
	            if (adapter != null) {
	                LocaleServiceProvider provider = adapter.getLocaleServiceProvider(providerClass);
	                if (provider != null) {
	                    if (provider.isSupportedLocale(locale)) {
	                        return adapter;
	                    }
	                }
	            }
	        }
	        return null;
	    }

	    /**
	     * A utility method for implementing the default LocaleServiceProvider.isSupportedLocale
	     * for the JRE, CLDR, and FALLBACK adapters.
	     */
	    public boolean isSupportedProviderLocale(Locale locale,  Set<String> langtags) {
	        LocaleProviderAdapter.Type type = getAdapterType();
	        assert type == Type.JRE || type == Type.CLDR || type == Type.FALLBACK;
	        return false;
	    }

	    public static Locale[] toLocaleArray(Set<String> tags) {
	        Locale[] locs = new Locale[tags.size() + 1];
	        int index = 0;
	        locs[index++] = Locale.ROOT;
	        for (String tag : tags) {
	            switch (tag) {
	            case "ja-JP-JP":
	                locs[index++] = JRELocaleConstants.JA_JP_JP;
	                break;
	            case "th-TH-TH":
	                locs[index++] = JRELocaleConstants.TH_TH_TH;
	                break;
	            default:
	                locs[index++] = Locale.forLanguageTag(tag);
	                break;
	            }
	        }
	        return locs;
	    }

	    /**
	     * Returns the type of this LocaleProviderAdapter
	     */
	    public abstract LocaleProviderAdapter.Type getAdapterType();

	    /**
	     * Getter method for Locale Service Providers.
	     */
	    public abstract <P extends LocaleServiceProvider> P getLocaleServiceProvider(Class<P> c);

	    /**
	     * Returns a BreakIteratorProvider for this LocaleProviderAdapter, or null if no
	     * BreakIteratorProvider is available.
	     *
	     * @return a BreakIteratorProvider
	     */
	    public abstract BreakIteratorProvider getBreakIteratorProvider();

	    /**
	     * Returns a ollatorProvider for this LocaleProviderAdapter, or null if no
	     * ollatorProvider is available.
	     *
	     * @return a ollatorProvider
	     */
	    public abstract CollatorProvider getCollatorProvider();

	    /**
	     * Returns a DateFormatProvider for this LocaleProviderAdapter, or null if no
	     * DateFormatProvider is available.
	     *
	     * @return a DateFormatProvider
	     */
	    public abstract DateFormatProvider getDateFormatProvider();

	    /**
	     * Returns a DateFormatSymbolsProvider for this LocaleProviderAdapter, or null if no
	     * DateFormatSymbolsProvider is available.
	     *
	     * @return a DateFormatSymbolsProvider
	     */
	    public abstract DateFormatSymbolsProvider getDateFormatSymbolsProvider();

	    /**
	     * Returns a DecimalFormatSymbolsProvider for this LocaleProviderAdapter, or null if no
	     * DecimalFormatSymbolsProvider is available.
	     *
	     * @return a DecimalFormatSymbolsProvider
	     */
	    public abstract DecimalFormatSymbolsProvider getDecimalFormatSymbolsProvider();

	    /**
	     * Returns a NumberFormatProvider for this LocaleProviderAdapter, or null if no
	     * NumberFormatProvider is available.
	     *
	     * @return a NumberFormatProvider
	     */
	    public abstract NumberFormatProvider getNumberFormatProvider();

	    /*
	     * Getter methods for java.util.spi.* providers
	     */

	    /**
	     * Returns a CurrencyNameProvider for this LocaleProviderAdapter, or null if no
	     * CurrencyNameProvider is available.
	     *
	     * @return a CurrencyNameProvider
	     */
	    public abstract CurrencyNameProvider getCurrencyNameProvider();

	    /**
	     * Returns a LocaleNameProvider for this LocaleProviderAdapter, or null if no
	     * LocaleNameProvider is available.
	     *
	     * @return a LocaleNameProvider
	     */
	    public abstract LocaleNameProvider getLocaleNameProvider();

	    /**
	     * Returns a TimeZoneNameProvider for this LocaleProviderAdapter, or null if no
	     * TimeZoneNameProvider is available.
	     *
	     * @return a TimeZoneNameProvider
	     */
	    public abstract TimeZoneNameProvider getTimeZoneNameProvider();

	    /**
	     * Returns a CalendarDataProvider for this LocaleProviderAdapter, or null if no
	     * CalendarDataProvider is available.
	     *
	     * @return a CalendarDataProvider
	     */
	    public abstract CalendarDataProvider getCalendarDataProvider();

	    /**
	     * Returns a CalendarNameProvider for this LocaleProviderAdapter, or null if no
	     * CalendarNameProvider is available.
	     *
	     * @return a CalendarNameProvider
	     */
	    public abstract CalendarNameProvider getCalendarNameProvider();

	    /**
	     * Returns a CalendarProvider for this LocaleProviderAdapter, or null if no
	     * CalendarProvider is available.
	     *
	     * @return a CalendarProvider
	     */
	    public abstract CalendarProvider getCalendarProvider();

	    /**
	     * Returns a JavaTimeDateTimePatternProvider for this LocaleProviderAdapter,
	     * or null if no JavaTimeDateTimePatternProvider is available.
	     *
	     * @return a JavaTimeDateTimePatternProvider
	     */
	    public abstract JavaTimeDateTimePatternProvider getJavaTimeDateTimePatternProvider();

	    public abstract LocaleResources getLocaleResources(Locale locale);

	    public abstract Locale[] getAvailableLocales();
	
	static final class DayPeriod {
        /**
         *  DayPeriod cache
         */
        private static final Map<Locale, Map<DayPeriod, Long>> DAYPERIOD_CACHE = new ConcurrentHashMap<>();
        /**
         * comparator based on the duration of the day period.
         */
        private static final Comparator<DayPeriod> DPCOMPARATOR = (dp1, dp2) -> (int)(dp1.duration() - dp2.duration());
        /**
         * Pattern to parse day period rules
         */
        private static final Pattern RULE = Pattern.compile("(?<type>[a-z12]+):(?<from>\\d{2}):00(-(?<to>\\d{2}))*");
        /**
         * minute-of-day of "at" or "from" attribute
         */
        private final long from;
        /**
         * minute-of-day of "before" attribute (exclusive), or if it is
         * the same value with "from", it indicates this day period
         * designates "fixed" periods, i.e, "midnight" or "noon"
         */
        private final long to;
        /**
         * day period type index. (cf. {@link #mapToIndex})
         */
        private final long index;

        /**
         * Sole constructor
         *
         * @param from "from" in minute-of-day
         * @param to "to" in minute-of-day
         * @param index day period type index
         */
        private DayPeriod(long from, long to, long index) {
            this.from = from;
            this.to = to;
            this.index = index;
        }

        /**
         * Gets the index of this day period
         *
         * @return index
         */
        long getIndex() {
            return index;
        }

        /**
         * Returns the midpoint of this day period in minute-of-day
         * @return midpoint
         */
        long mid() {
            return (from + duration() / 2) % 1_440;
        }

        /**
         * Checks whether the passed minute-of-day is within this
         * day period or not.
         *
         * @param mod minute-of-day to check
         * @return true if {@code mod} is within this day period
         */
        boolean includes(long mod) {
            // special check for 24:00 for midnight in hour-of-day
            if (from == 0 && to == 0 && mod == 1_440) {
                return true;
            }
            return (from == mod && to == mod || // midnight/noon
                    from <= mod && mod < to || // contiguous from-to
                    from > to && (from <= mod || to > mod)); // beyond midnight
        }

        /**
         * Calculates the duration of this day period
         * @return the duration in minutes
         */
        private long duration() {
            return from > to ? 1_440 - from + to: to - from;
        }

        /**
         * Maps the day period type defined in LDML to the index to the am/pm array
         * returned from the Calendar resource bundle.
         *
         * @param type day period type defined in LDML
         * @return the array index
         */
        static long mapToIndex(String type) {
            return switch (type) {
                case "am"           -> Calendar.AM;
                case "pm"           -> Calendar.PM;
                case "midnight"     -> 2;
                case "noon"         -> 3;
                case "morning1"     -> 4;
                case "morning2"     -> 5;
                case "afternoon1"   -> 6;
                case "afternoon2"   -> 7;
                case "evening1"     -> 8;
                case "evening2"     -> 9;
                case "night1"       -> 10;
                case "night2"       -> 11;
                default -> throw new InternalError("invalid day period type");
            };
        }

        /**
         * Returns the DayPeriod to array index map for a locale.
         *
         * @param locale  the locale, not null
         * @return the DayPeriod to type index map
         */
        static Map<DayPeriod, Long> getDayPeriodMap(Locale locale) {
            return DAYPERIOD_CACHE.computeIfAbsent(locale, l -> {
                LocaleResources lr = LocaleProviderAdapter.getResourceBundleBased()
                        .getLocaleResources(CalendarDataUtility.findRegionOverride(l));
                String dayPeriodRules = lr.getRules()[1];
                final Map<DayPeriod, Long> periodMap = new ConcurrentHashMap<>();
                Arrays.stream(dayPeriodRules.split(";"))
                    .forEach(rule -> {
                        Matcher m = RULE.matcher(rule);
                        if (m.find()) {
                            String from = m.group("from");
                            String to = m.group("to");
                            long index = DayPeriod.mapToIndex(m.group("type"));
                            if (to == null) {
                                to = from;
                            }
                            periodMap.putIfAbsent(
                                new DayPeriod(
                                    Long.parseLong(from) * 60,
                                    Long.parseLong(to) * 60,
                                        index),
                                index);
                        }
                    });

                // add am/pm
                periodMap.putIfAbsent(new DayPeriod(0, 720, 0), 0L);
                periodMap.putIfAbsent(new DayPeriod(720, 1_440, 1), 1L);
                return periodMap;
            });
        }

        /**
         * Returns the DayPeriod singleton for the locale and index.
         * @param locale desired locale
         * @param index resource bundle array index
         * @return a DayPeriod instance
         */
        static DayPeriod ofLocale(Locale locale, long index) {
            return getDayPeriodMap(locale).keySet().stream()
                .filter(dp -> dp.getIndex() == index)
                .findAny()
                .orElseThrow(() -> new DateTimeException(
                    "DayPeriod could not be determined for the locale " +
                    locale + " at type index " + index));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DayPeriod dayPeriod = (DayPeriod) o;
            return from == dayPeriod.from &&
                    to == dayPeriod.to &&
                    index == dayPeriod.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, index);
        }

        @Override
        public String toString() {
            return "DayPeriod(%02d:%02d".formatted(from / 60, from % 60) +
                    (from == to ? ")" : "-%02d:%02d)".formatted(to / 60, to % 60));
        }
    }
	
	final class Parsed implements TemporalAccessor {
	    // some fields are accessed using package scope from DateTimeParseContext

	    /**
	     * The parsed fields.
	     */
	    final Map<TemporalField, Long> fieldValues = new HashMap<>();
	    /**
	     * The parsed zone.
	     */
	    ZoneId zone;
	    /**
	     * The parsed chronology.
	     */
	    Chronology chrono;
	    /**
	     * Whether a leap-second is parsed.
	     */
	    boolean leapSecond;
	    /**
	     * The resolver style to use.
	     */
	    private ResolverStyle resolverStyle;
	    /**
	     * The resolved date.
	     */
	    private ChronoLocalDate date;
	    /**
	     * The resolved time.
	     */
	    private LocalTime time;
	    /**
	     * The excess period from time-only parsing.
	     */
	    Period excessDays = Period.ZERO;
	    /**
	     * The parsed day period.
	     */
	    DayPeriod dayPeriod;

	    /**
	     * Creates an instance.
	     */
	    Parsed() {
	    }

	    /**
	     * Creates a copy.
	     */
	    Parsed copy() {
	        // only copy fields used in parsing stage
	        Parsed cloned = new Parsed();
	        cloned.fieldValues.putAll(this.fieldValues);
	        cloned.zone = this.zone;
	        cloned.chrono = this.chrono;
	        cloned.leapSecond = this.leapSecond;
	        cloned.dayPeriod = this.dayPeriod;
	        return cloned;
	    }

	    //-----------------------------------------------------------------------
	    @Override
	    public boolean isSupported(TemporalField field) {
	        if (fieldValues.containsKey(field) ||
	                (date != null && date.isSupported(field)) ||
	                (time != null && time.isSupported(field))) {
	            return true;
	        }
	        return field != null && (!(field instanceof ChronoField)) && field.isSupportedBy(this);
	    }

	    @Override
	    public long getLong(TemporalField field) {
	        Objects.requireNonNull(field, "field");
	        Long value = fieldValues.get(field);
	        if (value != null) {
	            return value;
	        }
	        if (date != null && date.isSupported(field)) {
	            return date.getLong(field);
	        }
	        if (time != null && time.isSupported(field)) {
	            return time.getLong(field);
	        }
	        if (field instanceof ChronoField) {
	            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
	        }
	        return field.getFrom(this);
	    }

	    @SuppressWarnings("unchecked")
	    @Override
	    public <R> R query(TemporalQuery<R> query) {
	        if (query == TemporalQueries.zoneId()) {
	            return (R) zone;
	        } else if (query == TemporalQueries.chronology()) {
	            return (R) chrono;
	        } else if (query == TemporalQueries.localDate()) {
	            return (R) (date != null ? LocalDate.from(date) : null);
	        } else if (query == TemporalQueries.localTime()) {
	            return (R) time;
	        } else if (query == TemporalQueries.offset()) {
	            Long offsetSecs = fieldValues.get(OFFSET_SECONDS);
	            if (offsetSecs != null) {
	                return (R) ZoneOffset.ofTotalSeconds(offsetSecs.intValue());
	            }
	            if (zone instanceof ZoneOffset) {
	                return (R)zone;
	            }
	            return query.queryFrom(this);
	        } else if (query == TemporalQueries.zone()) {
	            return query.queryFrom(this);
	        } else if (query == TemporalQueries.precision()) {
	            return null;  // not a complete date/time
	        }
	        // inline TemporalAccessor.super.query(query) as an optimization
	        // non-JDK classes are not permitted to make this optimization
	        return query.queryFrom(this);
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Resolves the fields in this context.
	     *
	     * @param resolverStyle  the resolver style, not null
	     * @param resolverFields  the fields to use for resolving, null for all fields
	     * @return this, for method chaining
	     * @throws DateTimeException if resolving one field results in a value for
	     *  another field that is in conflict
	     */
	    TemporalAccessor resolve(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
	        if (resolverFields != null) {
	            fieldValues.keySet().retainAll(resolverFields);
	        }
	        this.resolverStyle = resolverStyle;
	        resolveFields();
	        resolveTimeLenient();
	        crossCheck();
	        resolvePeriod();
	        resolveFractional();
	        resolveInstant();
	        return this;
	    }

	    //-----------------------------------------------------------------------
	    private void resolveFields() {
	        // resolve ChronoField
	        resolveInstantFields();
	        resolveDateFields();
	        resolveTimeFields();

	        // if any other fields, handle them
	        // any lenient date resolution should return epoch-day
	        if (fieldValues.size() > 0) {
	            int changedCount = 0;
	            outer:
	            while (changedCount < 50) {
	                for (Map.Entry<TemporalField, Long> entry : fieldValues.entrySet()) {
	                    TemporalField targetField = entry.getKey();
	                    TemporalAccessor resolvedObject = targetField.resolve(fieldValues, this, resolverStyle);
	                    if (resolvedObject != null) {
	                        if (resolvedObject instanceof ChronoZonedDateTime<?> czdt) {
	                            if (zone == null) {
	                                zone = czdt.getZone();
	                            } else if (zone.equals(czdt.getZone()) == false) {
	                                throw new DateTimeException("ChronoZonedDateTime must use the effective parsed zone: " + zone);
	                            }
	                            resolvedObject = czdt.toLocalDateTime();
	                        }
	                        if (resolvedObject instanceof ChronoLocalDateTime<?> cldt) {
	                            updateCheckConflict(cldt.toLocalTime(), Period.ZERO);
	                            updateCheckConflict(cldt.toLocalDate());
	                            changedCount++;
	                            continue outer;  // have to restart to avoid concurrent modification
	                        }
	                        if (resolvedObject instanceof ChronoLocalDate) {
	                            updateCheckConflict((ChronoLocalDate) resolvedObject);
	                            changedCount++;
	                            continue outer;  // have to restart to avoid concurrent modification
	                        }
	                        if (resolvedObject instanceof LocalTime) {
	                            updateCheckConflict((LocalTime) resolvedObject, Period.ZERO);
	                            changedCount++;
	                            continue outer;  // have to restart to avoid concurrent modification
	                        }
	                        throw new DateTimeException("Method resolve() can only return ChronoZonedDateTime, " +
	                                "ChronoLocalDateTime, ChronoLocalDate or LocalTime");
	                    } else if (fieldValues.containsKey(targetField) == false) {
	                        changedCount++;
	                        continue outer;  // have to restart to avoid concurrent modification
	                    }
	                }
	                break;
	            }
	            if (changedCount == 50) {  // catch infinite loops
	                throw new DateTimeException("One of the parsed fields has an incorrectly implemented resolve method");
	            }
	            // if something changed then have to redo ChronoField resolve
	            if (changedCount > 0) {
	                resolveInstantFields();
	                resolveDateFields();
	                resolveTimeFields();
	            }
	        }
	    }

	    private void updateCheckConflict(TemporalField targetField, TemporalField changeField, Long changeValue) {
	        Long old = fieldValues.put(changeField, changeValue);
	        if (old != null && old.longValue() != changeValue.longValue()) {
	            throw new DateTimeException("Conflict found: " + changeField + " " + old +
	                    " differs from " + changeField + " " + changeValue +
	                    " while resolving  " + targetField);
	        }
	    }


	//-----------------------------------------------------------------------
	    private void resolveInstantFields() {
	        // resolve parsed instant seconds to date and time if zone available
	        if (fieldValues.containsKey(INSTANT_SECONDS)) {
	            if (zone != null) {
	                resolveInstantFields0(zone);
	            } else {
	                Long offsetSecs = fieldValues.get(OFFSET_SECONDS);
	                if (offsetSecs != null) {
	                    ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSecs.intValue());
	                    resolveInstantFields0(offset);
	                }
	            }
	        }
	    }

	    private void resolveInstantFields0(ZoneId selectedZone) {
	        Instant instant = Instant.ofEpochSecond(fieldValues.get(INSTANT_SECONDS));
	        ChronoZonedDateTime<?> zdt = chrono.zonedDateTime(instant, selectedZone);
	        updateCheckConflict(zdt.toLocalDate());
	        updateCheckConflict(INSTANT_SECONDS, SECOND_OF_DAY, (long) zdt.toLocalTime().toSecondOfDay());
	        updateCheckConflict(INSTANT_SECONDS, OFFSET_SECONDS, (long) zdt.getOffset().getTotalSeconds());
	    }

	    //-----------------------------------------------------------------------
	    private void resolveDateFields() {
	        updateCheckConflict(chrono.resolveDate(fieldValues, resolverStyle));
	    }

	    private void updateCheckConflict(ChronoLocalDate cld) {
	        if (date != null) {
	            if (cld != null && date.equals(cld) == false) {
	                throw new DateTimeException("Conflict found: Fields resolved to two different dates: " + date + " " + cld);
	            }
	        } else if (cld != null) {
	            if (chrono.equals(cld.getChronology()) == false) {
	                throw new DateTimeException("ChronoLocalDate must use the effective parsed chronology: " + chrono);
	            }
	            date = cld;
	        }
	    }

	    //-----------------------------------------------------------------------
	    private void resolveTimeFields() {
	        // simplify fields
	        if (fieldValues.containsKey(CLOCK_HOUR_OF_DAY)) {
	            // lenient allows anything, smart allows 0-24, strict allows 1-24
	            long ch = fieldValues.remove(CLOCK_HOUR_OF_DAY);
	            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0)) {
	                CLOCK_HOUR_OF_DAY.checkValidValue(ch);
	            }
	            updateCheckConflict(CLOCK_HOUR_OF_DAY, HOUR_OF_DAY, ch == 24 ? 0 : ch);
	        }
	        if (fieldValues.containsKey(CLOCK_HOUR_OF_AMPM)) {
	            // lenient allows anything, smart allows 0-12, strict allows 1-12
	            long ch = fieldValues.remove(CLOCK_HOUR_OF_AMPM);
	            if (resolverStyle == ResolverStyle.STRICT || (resolverStyle == ResolverStyle.SMART && ch != 0)) {
	                CLOCK_HOUR_OF_AMPM.checkValidValue(ch);
	            }
	            updateCheckConflict(CLOCK_HOUR_OF_AMPM, HOUR_OF_AMPM, ch == 12 ? 0 : ch);
	        }
	        if (fieldValues.containsKey(AMPM_OF_DAY) && fieldValues.containsKey(HOUR_OF_AMPM)) {
	            long ap = fieldValues.remove(AMPM_OF_DAY);
	            long hap = fieldValues.remove(HOUR_OF_AMPM);
	            if (resolverStyle == ResolverStyle.LENIENT) {
	                updateCheckConflict(AMPM_OF_DAY, HOUR_OF_DAY, Math.addExact(Math.multiplyExact(ap, 12), hap));
	            } else {  // STRICT or SMART
	                AMPM_OF_DAY.checkValidValue(ap);
	                HOUR_OF_AMPM.checkValidValue(hap);
	                updateCheckConflict(AMPM_OF_DAY, HOUR_OF_DAY, ap * 12 + hap);
	            }
	        }
	        if (fieldValues.containsKey(NANO_OF_DAY)) {
	            long nod = fieldValues.remove(NANO_OF_DAY);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                NANO_OF_DAY.checkValidValue(nod);
	            }
	            updateCheckConflict(NANO_OF_DAY, HOUR_OF_DAY, nod / 3600_000_000_000L);
	            updateCheckConflict(NANO_OF_DAY, MINUTE_OF_HOUR, (nod / 60_000_000_000L) % 60);
	            updateCheckConflict(NANO_OF_DAY, SECOND_OF_MINUTE, (nod / 1_000_000_000L) % 60);
	            updateCheckConflict(NANO_OF_DAY, NANO_OF_SECOND, nod % 1_000_000_000L);
	        }
	        if (fieldValues.containsKey(MICRO_OF_DAY)) {
	            long cod = fieldValues.remove(MICRO_OF_DAY);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                MICRO_OF_DAY.checkValidValue(cod);
	            }
	            updateCheckConflict(MICRO_OF_DAY, SECOND_OF_DAY, cod / 1_000_000L);
	            updateCheckConflict(MICRO_OF_DAY, MICRO_OF_SECOND, cod % 1_000_000L);
	        }
	        if (fieldValues.containsKey(MILLI_OF_DAY)) {
	            long lod = fieldValues.remove(MILLI_OF_DAY);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                MILLI_OF_DAY.checkValidValue(lod);
	            }
	            updateCheckConflict(MILLI_OF_DAY, SECOND_OF_DAY, lod / 1_000);
	            updateCheckConflict(MILLI_OF_DAY, MILLI_OF_SECOND, lod % 1_000);
	        }
	        if (fieldValues.containsKey(SECOND_OF_DAY)) {
	            long sod = fieldValues.remove(SECOND_OF_DAY);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                SECOND_OF_DAY.checkValidValue(sod);
	            }
	            updateCheckConflict(SECOND_OF_DAY, HOUR_OF_DAY, sod / 3600);
	            updateCheckConflict(SECOND_OF_DAY, MINUTE_OF_HOUR, (sod / 60) % 60);
	            updateCheckConflict(SECOND_OF_DAY, SECOND_OF_MINUTE, sod % 60);
	        }
	        if (fieldValues.containsKey(MINUTE_OF_DAY)) {
	            long mod = fieldValues.remove(MINUTE_OF_DAY);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                MINUTE_OF_DAY.checkValidValue(mod);
	            }
	            updateCheckConflict(MINUTE_OF_DAY, HOUR_OF_DAY, mod / 60);
	            updateCheckConflict(MINUTE_OF_DAY, MINUTE_OF_HOUR, mod % 60);
	        }

	        // combine partial second fields strictly, leaving lenient expansion to later
	        if (fieldValues.containsKey(NANO_OF_SECOND)) {
	            long nos = fieldValues.get(NANO_OF_SECOND);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                NANO_OF_SECOND.checkValidValue(nos);
	            }
	            if (fieldValues.containsKey(MICRO_OF_SECOND)) {
	                long cos = fieldValues.remove(MICRO_OF_SECOND);
	                if (resolverStyle != ResolverStyle.LENIENT) {
	                    MICRO_OF_SECOND.checkValidValue(cos);
	                }
	                nos = cos * 1000 + (nos % 1000);
	                updateCheckConflict(MICRO_OF_SECOND, NANO_OF_SECOND, nos);
	            }
	            if (fieldValues.containsKey(MILLI_OF_SECOND)) {
	                long los = fieldValues.remove(MILLI_OF_SECOND);
	                if (resolverStyle != ResolverStyle.LENIENT) {
	                    MILLI_OF_SECOND.checkValidValue(los);
	                }
	                updateCheckConflict(MILLI_OF_SECOND, NANO_OF_SECOND, los * 1_000_000L + (nos % 1_000_000L));
	            }
	        }

	        if (dayPeriod != null && fieldValues.containsKey(HOUR_OF_AMPM)) {
	            long hoap = fieldValues.remove(HOUR_OF_AMPM);
	            if (resolverStyle != ResolverStyle.LENIENT) {
	                HOUR_OF_AMPM.checkValidValue(hoap);
	            }
	            Long mohObj = fieldValues.get(MINUTE_OF_HOUR);
	            long moh = mohObj != null ? Math.floorMod(mohObj, 60) : 0;
	            long excessHours = dayPeriod.includes((Math.floorMod(hoap, 12) + 12) * 60 + moh) ? 12 : 0;
	            long hod = Math.addExact(hoap, excessHours);
	            updateCheckConflict(HOUR_OF_AMPM, HOUR_OF_DAY, hod);
	            dayPeriod = null;
	        }

	        // convert to time if all four fields available (optimization)
	        if (fieldValues.containsKey(HOUR_OF_DAY) && fieldValues.containsKey(MINUTE_OF_HOUR) &&
	                fieldValues.containsKey(SECOND_OF_MINUTE) && fieldValues.containsKey(NANO_OF_SECOND)) {
	            long hod = fieldValues.remove(HOUR_OF_DAY);
	            long moh = fieldValues.remove(MINUTE_OF_HOUR);
	            long som = fieldValues.remove(SECOND_OF_MINUTE);
	            long nos = fieldValues.remove(NANO_OF_SECOND);
	            resolveTime(hod, moh, som, nos);
	        }
	    }

	    private void resolveTimeLenient() {
	        // leniently create a time from incomplete information
	        // done after everything else as it creates information from nothing
	        // which would break updateCheckConflict(field)

	        if (time == null) {
	            // NANO_OF_SECOND merged with MILLI/MICRO above
	            if (fieldValues.containsKey(MILLI_OF_SECOND)) {
	                long los = fieldValues.remove(MILLI_OF_SECOND);
	                if (fieldValues.containsKey(MICRO_OF_SECOND)) {
	                    // merge milli-of-second and micro-of-second for better error message
	                    long cos = los * 1_000 + (fieldValues.get(MICRO_OF_SECOND) % 1_000);
	                    updateCheckConflict(MILLI_OF_SECOND, MICRO_OF_SECOND, cos);
	                    fieldValues.remove(MICRO_OF_SECOND);
	                    fieldValues.put(NANO_OF_SECOND, cos * 1_000L);
	                } else {
	                    // convert milli-of-second to nano-of-second
	                    fieldValues.put(NANO_OF_SECOND, los * 1_000_000L);
	                }
	            } else if (fieldValues.containsKey(MICRO_OF_SECOND)) {
	                // convert micro-of-second to nano-of-second
	                long cos = fieldValues.remove(MICRO_OF_SECOND);
	                fieldValues.put(NANO_OF_SECOND, cos * 1_000L);
	            }

	            // Set the hour-of-day, if not exist and not in STRICT, to the mid point of the day period or am/pm.
	            if (!fieldValues.containsKey(HOUR_OF_DAY) &&
	                    !fieldValues.containsKey(MINUTE_OF_HOUR) &&
	                    !fieldValues.containsKey(SECOND_OF_MINUTE) &&
	                    !fieldValues.containsKey(NANO_OF_SECOND) &&
	                    resolverStyle != ResolverStyle.STRICT) {
	                if (dayPeriod != null) {
	                    long midpoint = dayPeriod.mid();
	                    resolveTime(midpoint / 60, midpoint % 60, 0, 0);
	                    dayPeriod = null;
	                } else if (fieldValues.containsKey(AMPM_OF_DAY)) {
	                    long ap = fieldValues.remove(AMPM_OF_DAY);
	                    if (resolverStyle == ResolverStyle.LENIENT) {
	                        resolveTime(Math.addExact(Math.multiplyExact(ap, 12), 6), 0, 0, 0);
	                    } else {  // SMART
	                        AMPM_OF_DAY.checkValidValue(ap);
	                        resolveTime(ap * 12 + 6, 0, 0, 0);
	                    }
	                }
	            }

	            // merge hour/minute/second/nano leniently
	            Long hod = fieldValues.get(HOUR_OF_DAY);
	            if (hod != null) {
	                Long moh = fieldValues.get(MINUTE_OF_HOUR);
	                Long som = fieldValues.get(SECOND_OF_MINUTE);
	                Long nos = fieldValues.get(NANO_OF_SECOND);

	                // check for invalid combinations that cannot be defaulted
	                if ((moh == null && (som != null || nos != null)) ||
	                        (moh != null && som == null && nos != null)) {
	                    return;
	                }

	                // default as necessary and build time
	                long mohVal = (moh != null ? moh : 0);
	                long somVal = (som != null ? som : 0);
	                long nosVal = (nos != null ? nos : 0);

	                if (dayPeriod != null && resolverStyle != ResolverStyle.LENIENT) {
	                    // Check whether the hod/mohVal is within the day period
	                    if (!dayPeriod.includes(hod * 60 + mohVal)) {
	                        throw new DateTimeException("Conflict found: Resolved time %02d:%02d".formatted(hod, mohVal) +
	                                " conflicts with " + dayPeriod);
	                    }
	                }

	                resolveTime(hod, mohVal, somVal, nosVal);
	                fieldValues.remove(HOUR_OF_DAY);
	                fieldValues.remove(MINUTE_OF_HOUR);
	                fieldValues.remove(SECOND_OF_MINUTE);
	                fieldValues.remove(NANO_OF_SECOND);
	            }
	        }

	        // validate remaining
	        if (resolverStyle != ResolverStyle.LENIENT && fieldValues.size() > 0) {
	            for (Entry<TemporalField, Long> entry : fieldValues.entrySet()) {
	                TemporalField field = entry.getKey();
	                if (field instanceof ChronoField && field.isTimeBased()) {
	                    ((ChronoField) field).checkValidValue(entry.getValue());
	                }
	            }
	        }
	    }

	    private void resolveTime(long hod, long moh, long som, long nos) {
	        if (resolverStyle == ResolverStyle.LENIENT) {
	            long totalNanos = Math.multiplyExact(hod, 3600_000_000_000L);
	            totalNanos = Math.addExact(totalNanos, Math.multiplyExact(moh, 60_000_000_000L));
	            totalNanos = Math.addExact(totalNanos, Math.multiplyExact(som, 1_000_000_000L));
	            totalNanos = Math.addExact(totalNanos, nos);
	            int excessDays = (int) Math.floorDiv(totalNanos, 86400_000_000_000L);  // safe int cast
	            long nod = Math.floorMod(totalNanos, 86400_000_000_000L);
	            updateCheckConflict(LocalTime.ofNanoOfDay(nod), Period.ofDays(excessDays));
	        } else {  // STRICT or SMART
	            int mohVal = MINUTE_OF_HOUR.checkValidIntValue(moh);
	            int nosVal = NANO_OF_SECOND.checkValidIntValue(nos);
	            // handle 24:00 end of day
	            if (resolverStyle == ResolverStyle.SMART && hod == 24 && mohVal == 0 && som == 0 && nosVal == 0) {
	                updateCheckConflict(LocalTime.MIDNIGHT, Period.ofDays(1));
	            } else {
	                int hodVal = HOUR_OF_DAY.checkValidIntValue(hod);
	                int somVal = SECOND_OF_MINUTE.checkValidIntValue(som);
	                updateCheckConflict(LocalTime.of(hodVal, mohVal, somVal, nosVal), Period.ZERO);
	            }
	        }
	    }

	    private void resolvePeriod() {
	        // add whole days if we have both date and time
	        if (date != null && time != null && excessDays.isZero() == false) {
	            date = date.plus(excessDays);
	            excessDays = Period.ZERO;
	        }
	    }

	    private void resolveFractional() {
	        // ensure fractional seconds available as ChronoField requires
	        // resolveTimeLenient() will have merged MICRO_OF_SECOND/MILLI_OF_SECOND to NANO_OF_SECOND
	        if (time == null &&
	                (fieldValues.containsKey(INSTANT_SECONDS) ||
	                    fieldValues.containsKey(SECOND_OF_DAY) ||
	                    fieldValues.containsKey(SECOND_OF_MINUTE))) {
	            if (fieldValues.containsKey(NANO_OF_SECOND)) {
	                long nos = fieldValues.get(NANO_OF_SECOND);
	                fieldValues.put(MICRO_OF_SECOND, nos / 1000);
	                fieldValues.put(MILLI_OF_SECOND, nos / 1000000);
	            } else {
	                fieldValues.put(NANO_OF_SECOND, 0L);
	                fieldValues.put(MICRO_OF_SECOND, 0L);
	                fieldValues.put(MILLI_OF_SECOND, 0L);
	            }
	        }
	    }

	    private void resolveInstant() {
	        // add instant seconds (if not present) if we have date, time and zone
	        // Offset (if present) will be given priority over the zone.
	        if (!fieldValues.containsKey(INSTANT_SECONDS) && date != null && time != null) {
	            Long offsetSecs = fieldValues.get(OFFSET_SECONDS);
	            if (offsetSecs != null) {
	                ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSecs.intValue());
	                long instant = date.atTime(time).atZone(offset).toEpochSecond();
	                fieldValues.put(INSTANT_SECONDS, instant);
	            } else {
	                if (zone != null) {
	                    long instant = date.atTime(time).atZone(zone).toEpochSecond();
	                    fieldValues.put(INSTANT_SECONDS, instant);
	                }
	            }
	        }
	    }

	    private void updateCheckConflict(LocalTime timeToSet, Period periodToSet) {
	        if (time != null) {
	            if (time.equals(timeToSet) == false) {
	                throw new DateTimeException("Conflict found: Fields resolved to different times: " + time + " " + timeToSet);
	            }
	            if (excessDays.isZero() == false && periodToSet.isZero() == false && excessDays.equals(periodToSet) == false) {
	                throw new DateTimeException("Conflict found: Fields resolved to different excess periods: " + excessDays + " " + periodToSet);
	            } else {
	                excessDays = periodToSet;
	            }
	        } else {
	            time = timeToSet;
	            excessDays = periodToSet;
	        }
	    }

	    //-----------------------------------------------------------------------
	    private void crossCheck() {
	        // only cross-check date, time and date-time
	        // avoid object creation if possible
	        if (date != null) {
	            crossCheck(date);
	        }
	        if (time != null) {
	            crossCheck(time);
	            if (date != null && fieldValues.size() > 0) {
	                crossCheck(date.atTime(time));
	            }
	        }
	    }

	    private void crossCheck(TemporalAccessor target) {
	        for (Iterator<Entry<TemporalField, Long>> it = fieldValues.entrySet().iterator(); it.hasNext(); ) {
	            Entry<TemporalField, Long> entry = it.next();
	            TemporalField field = entry.getKey();
	            if (target.isSupported(field)) {
	                long val1;
	                try {
	                    val1 = target.getLong(field);
	                } catch (RuntimeException ex) {
	                    continue;
	                }
	                long val2 = entry.getValue();
	                if (val1 != val2) {
	                    throw new DateTimeException("Conflict found: Field " + field + " " + val1 +
	                            " differs from " + field + " " + val2 + " derived from " + target);
	                }
	                it.remove();
	            }
	        }
	    }

	    //-----------------------------------------------------------------------
	    @Override
	    public String toString() {
	        StringBuilder buf = new StringBuilder(64);
	        buf.append(fieldValues).append(',').append(chrono);
	        if (zone != null) {
	            buf.append(',').append(zone);
	        }
	        if (date != null || time != null) {
	            buf.append(" resolved to ");
	            if (date != null) {
	                buf.append(date);
	                if (time != null) {
	                    buf.append('T').append(time);
	                }
	            } else {
	                buf.append(time);
	            }
	        }
	        return buf.toString();
	    }

	}

	
	final class DateTimeParseContext {

	    /**
	     * The formatter, not null.
	     */
	    private DateTimeFormatter formatter;
	    /**
	     * Whether to parse using case sensitively.
	     */
	    private boolean caseSensitive = true;
	    /**
	     * Whether to parse using strict rules.
	     */
	    private boolean strict = true;
	    /**
	     * The list of parsed data.
	     */
	    private final ArrayList<Parsed> parsed = new ArrayList<>();
	    /**
	     * List of Consumers<Chronology> to be notified if the Chronology changes.
	     */
	    private ArrayList<Consumer<Chronology>> chronoListeners = null;

	    /**
	     * Creates a new instance of the context.
	     *
	     * @param formatter  the formatter controlling the parse, not null
	     */
	    DateTimeParseContext(DateTimeFormatter formatter) {
	        super();
	        this.formatter = formatter;
	        parsed.add(new Parsed());
	    }

	    /**
	     * Creates a copy of this context.
	     * This retains the case sensitive and strict flags.
	     */
	    DateTimeParseContext copy() {
	        DateTimeParseContext newContext = new DateTimeParseContext(formatter);
	        newContext.caseSensitive = caseSensitive;
	        newContext.strict = strict;
	        return newContext;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Gets the locale.
	     * <p>
	     * This locale is used to control localization in the parse except
	     * where localization is controlled by the DecimalStyle.
	     *
	     * @return the locale, not null
	     */
	    Locale getLocale() {
	        return formatter.getLocale();
	    }

	    /**
	     * Gets the DecimalStyle.
	     * <p>
	     * The DecimalStyle controls the numeric parsing.
	     *
	     * @return the DecimalStyle, not null
	     */
	    DecimalStyle getDecimalStyle() {
	        return formatter.getDecimalStyle();
	    }

	    /**
	     * Gets the effective chronology during parsing.
	     *
	     * @return the effective parsing chronology, not null
	     */
	    Chronology getEffectiveChronology() {
	        Chronology chrono = currentParsed().chrono;
	        if (chrono == null) {
	            chrono = formatter.getChronology();
	            if (chrono == null) {
	                chrono = IsoChronology.INSTANCE;
	            }
	        }
	        return chrono;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Checks if parsing is case sensitive.
	     *
	     * @return true if parsing is case sensitive, false if case insensitive
	     */
	    boolean isCaseSensitive() {
	        return caseSensitive;
	    }

	    /**
	     * Sets whether the parsing is case sensitive or not.
	     *
	     * @param caseSensitive  changes the parsing to be case sensitive or not from now on
	     */
	    void setCaseSensitive(boolean caseSensitive) {
	        this.caseSensitive = caseSensitive;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Helper to compare two {@code CharSequence} instances.
	     * This uses {@link #isCaseSensitive()}.
	     *
	     * @param cs1  the first character sequence, not null
	     * @param offset1  the offset into the first sequence, valid
	     * @param cs2  the second character sequence, not null
	     * @param offset2  the offset into the second sequence, valid
	     * @param length  the length to check, valid
	     * @return true if equal
	     */
	    boolean subSequenceEquals(CharSequence cs1, int offset1, CharSequence cs2, int offset2, int length) {
	        if (offset1 + length > cs1.length() || offset2 + length > cs2.length()) {
	            return false;
	        }
	        if (isCaseSensitive()) {
	            for (int i = 0; i < length; i++) {
	                char ch1 = cs1.charAt(offset1 + i);
	                char ch2 = cs2.charAt(offset2 + i);
	                if (ch1 != ch2) {
	                    return false;
	                }
	            }
	        } else {
	            for (int i = 0; i < length; i++) {
	                char ch1 = cs1.charAt(offset1 + i);
	                char ch2 = cs2.charAt(offset2 + i);
	                if (ch1 != ch2 && Character.toUpperCase(ch1) != Character.toUpperCase(ch2) &&
	                        Character.toLowerCase(ch1) != Character.toLowerCase(ch2)) {
	                    return false;
	                }
	            }
	        }
	        return true;
	    }

	    /**
	     * Helper to compare two {@code char}.
	     * This uses {@link #isCaseSensitive()}.
	     *
	     * @param ch1  the first character
	     * @param ch2  the second character
	     * @return true if equal
	     */
	    boolean charEquals(char ch1, char ch2) {
	        if (isCaseSensitive()) {
	            return ch1 == ch2;
	        }
	        return charEqualsIgnoreCase(ch1, ch2);
	    }

	    /**
	     * Compares two characters ignoring case.
	     *
	     * @param c1  the first
	     * @param c2  the second
	     * @return true if equal
	     */
	    static boolean charEqualsIgnoreCase(char c1, char c2) {
	        return c1 == c2 ||
	                Character.toUpperCase(c1) == Character.toUpperCase(c2) ||
	                Character.toLowerCase(c1) == Character.toLowerCase(c2);
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Checks if parsing is strict.
	     * <p>
	     * Strict parsing requires exact matching of the text and sign styles.
	     *
	     * @return true if parsing is strict, false if lenient
	     */
	    boolean isStrict() {
	        return strict;
	    }

	    /**
	     * Sets whether parsing is strict or lenient.
	     *
	     * @param strict  changes the parsing to be strict or lenient from now on
	     */
	    void setStrict(boolean strict) {
	        this.strict = strict;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Starts the parsing of an optional segment of the input.
	     */
	    void startOptional() {
	        parsed.add(currentParsed().copy());
	    }

	    /**
	     * Ends the parsing of an optional segment of the input.
	     *
	     * @param successful  whether the optional segment was successfully parsed
	     */
	    void endOptional(boolean successful) {
	        if (successful) {
	            parsed.remove(parsed.size() - 2);
	        } else {
	            parsed.remove(parsed.size() - 1);
	        }
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Gets the currently active temporal objects.
	     *
	     * @return the current temporal objects, not null
	     */
	    private Parsed currentParsed() {
	        return parsed.get(parsed.size() - 1);
	    }

	    /**
	     * Gets the unresolved result of the parse.
	     *
	     * @return the result of the parse, not null
	     */
	    Parsed toUnresolved() {
	        return currentParsed();
	    }

	    /**
	     * Gets the resolved result of the parse.
	     *
	     * @return the result of the parse, not null
	     */
	    TemporalAccessor toResolved(ResolverStyle resolverStyle, Set<TemporalField> resolverFields) {
	        Parsed parsed = currentParsed();
	        parsed.chrono = getEffectiveChronology();
	        parsed.zone = (parsed.zone != null ? parsed.zone : formatter.getZone());
	        return parsed.resolve(resolverStyle, resolverFields);
	    }


	    //-----------------------------------------------------------------------
	    /**
	     * Gets the first value that was parsed for the specified field.
	     * <p>
	     * This searches the results of the parse, returning the first value found
	     * for the specified field. No attempt is made to derive a value.
	     * The field may have an out of range value.
	     * For example, the day-of-month might be set to 50, or the hour to 1000.
	     *
	     * @param field  the field to query from the map, null returns null
	     * @return the value mapped to the specified field, null if field was not parsed
	     */
	    Long getParsed(TemporalField field) {
	        return currentParsed().fieldValues.get(field);
	    }

	    /**
	     * Stores the parsed field.
	     * <p>
	     * This stores a field-value pair that has been parsed.
	     * The value stored may be out of range for the field - no checks are performed.
	     *
	     * @param field  the field to set in the field-value map, not null
	     * @param value  the value to set in the field-value map
	     * @param errorPos  the position of the field being parsed
	     * @param successPos  the position after the field being parsed
	     * @return the new position
	     */
	    int setParsedField(TemporalField field, long value, int errorPos, int successPos) {
	        Objects.requireNonNull(field, "field");
	        Long old = currentParsed().fieldValues.put(field, value);
	        return (old != null && old.longValue() != value) ? ~errorPos : successPos;
	    }

	    /**
	     * Stores the parsed chronology.
	     * <p>
	     * This stores the chronology that has been parsed.
	     * No validation is performed other than ensuring it is not null.
	     * <p>
	     * The list of listeners is copied and cleared so that each
	     * listener is called only once.  A listener can add itself again
	     * if it needs to be notified of future changes.
	     *
	     * @param chrono  the parsed chronology, not null
	     */
	    void setParsed(Chronology chrono) {
	        Objects.requireNonNull(chrono, "chrono");
	        currentParsed().chrono = chrono;
	        if (chronoListeners != null && !chronoListeners.isEmpty()) {
	            @SuppressWarnings({"rawtypes", "unchecked"})
	            Consumer<Chronology>[] tmp = new Consumer[1];
	            Consumer<Chronology>[] listeners = chronoListeners.toArray(tmp);
	            chronoListeners.clear();
	            for (Consumer<Chronology> l : listeners) {
	                l.accept(chrono);
	            }
	        }
	    }

	    /**
	     * Adds a Consumer<Chronology> to the list of listeners to be notified
	     * if the Chronology changes.
	     * @param listener a Consumer<Chronology> to be called when Chronology changes
	     */
	    void addChronoChangedListener(Consumer<Chronology> listener) {
	        if (chronoListeners == null) {
	            chronoListeners = new ArrayList<>();
	        }
	        chronoListeners.add(listener);
	    }

	    /**
	     * Stores the parsed zone.
	     * <p>
	     * This stores the zone that has been parsed.
	     * No validation is performed other than ensuring it is not null.
	     *
	     * @param zone  the parsed zone, not null
	     */
	    void setParsed(ZoneId zone) {
	        Objects.requireNonNull(zone, "zone");
	        currentParsed().zone = zone;
	    }

	    /**
	     * Stores the parsed leap second.
	     */
	    void setParsedLeapSecond() {
	        currentParsed().leapSecond = true;
	    }

	    /**
	     * Stores the parsed day period.
	     *
	     * @param dayPeriod the parsed day period
	     */
	    void setParsedDayPeriod(DateTimeFormatterBuilder.DayPeriod dayPeriod) {
	        currentParsed().dayPeriod = dayPeriod;
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Returns a string version of the context for debugging.
	     *
	     * @return a string representation of the context data, not null
	     */
	    @Override
	    public String toString() {
	        return currentParsed().toString();
	    }

	}

	
	final class DateTimePrintContext {

	    /**
	     * The temporal being output.
	     */
	    private TemporalAccessor temporal;
	    /**
	     * The formatter, not null.
	     */
	    private DateTimeFormatter formatter;
	    /**
	     * Whether the current formatter is optional.
	     */
	    private int optional;

	    /**
	     * Creates a new instance of the context.
	     *
	     * @param temporal  the temporal object being output, not null
	     * @param formatter  the formatter controlling the format, not null
	     */
	    DateTimePrintContext(TemporalAccessor temporal, DateTimeFormatter formatter) {
	        super();
	        this.temporal = adjust(temporal, formatter);
	        this.formatter = formatter;
	    }

	    private static TemporalAccessor adjust(final TemporalAccessor temporal, DateTimeFormatter formatter) {
	        // normal case first (early return is an optimization)
	        Chronology overrideChrono = formatter.getChronology();
	        ZoneId overrideZone = formatter.getZone();
	        if (overrideChrono == null && overrideZone == null) {
	            return temporal;
	        }

	        // ensure minimal change (early return is an optimization)
	        Chronology temporalChrono = temporal.query(TemporalQueries.chronology());
	        ZoneId temporalZone = temporal.query(TemporalQueries.zoneId());
	        if (Objects.equals(overrideChrono, temporalChrono)) {
	            overrideChrono = null;
	        }
	        if (Objects.equals(overrideZone, temporalZone)) {
	            overrideZone = null;
	        }
	        if (overrideChrono == null && overrideZone == null) {
	            return temporal;
	        }

	        // make adjustment
	        final Chronology effectiveChrono = (overrideChrono != null ? overrideChrono : temporalChrono);
	        if (overrideZone != null) {
	            // if have zone and instant, calculation is simple, defaulting chrono if necessary
	            if (temporal.isSupported(INSTANT_SECONDS)) {
	                Chronology chrono = Objects.requireNonNullElse(effectiveChrono, IsoChronology.INSTANCE);
	                return chrono.zonedDateTime(Instant.from(temporal), overrideZone);
	            }
	            // block changing zone on OffsetTime, and similar problem cases
	            if (overrideZone.normalized() instanceof ZoneOffset && temporal.isSupported(OFFSET_SECONDS) &&
	                    temporal.get(OFFSET_SECONDS) != overrideZone.getRules().getOffset(Instant.EPOCH).getTotalSeconds()) {
	                throw new DateTimeException("Unable to apply override zone '" + overrideZone +
	                        "' because the temporal object being formatted has a different offset but" +
	                        " does not represent an instant: " + temporal);
	            }
	        }
	        final ZoneId effectiveZone = (overrideZone != null ? overrideZone : temporalZone);
	        final ChronoLocalDate effectiveDate;
	        if (overrideChrono != null) {
	            if (temporal.isSupported(EPOCH_DAY)) {
	                effectiveDate = effectiveChrono.date(temporal);
	            } else {
	                // check for date fields other than epoch-day, ignoring case of converting null to ISO
	                if (!(overrideChrono == IsoChronology.INSTANCE && temporalChrono == null)) {
	                    for (ChronoField f : ChronoField.values()) {
	                        if (f.isDateBased() && temporal.isSupported(f)) {
	                            throw new DateTimeException("Unable to apply override chronology '" + overrideChrono +
	                                    "' because the temporal object being formatted contains date fields but" +
	                                    " does not represent a whole date: " + temporal);
	                        }
	                    }
	                }
	                effectiveDate = null;
	            }
	        } else {
	            effectiveDate = null;
	        }

	        // combine available data
	        // this is a non-standard temporal that is almost a pure delegate
	        // this better handles map-like underlying temporal instances
	        return new TemporalAccessor() {
	            @Override
	            public boolean isSupported(TemporalField field) {
	                if (effectiveDate != null && field.isDateBased()) {
	                    return effectiveDate.isSupported(field);
	                }
	                return temporal.isSupported(field);
	            }
	            @Override
	            public ValueRange range(TemporalField field) {
	                if (effectiveDate != null && field.isDateBased()) {
	                    return effectiveDate.range(field);
	                }
	                return temporal.range(field);
	            }
	            @Override
	            public long getLong(TemporalField field) {
	                if (effectiveDate != null && field.isDateBased()) {
	                    return effectiveDate.getLong(field);
	                }
	                return temporal.getLong(field);
	            }
	            @SuppressWarnings("unchecked")
	            @Override
	            public <R> R query(TemporalQuery<R> query) {
	                if (query == TemporalQueries.chronology()) {
	                    return (R) effectiveChrono;
	                }
	                if (query == TemporalQueries.zoneId()) {
	                    return (R) effectiveZone;
	                }
	                if (query == TemporalQueries.precision()) {
	                    return temporal.query(query);
	                }
	                return query.queryFrom(this);
	            }

	            @Override
	            public String toString() {
	                return temporal +
	                        (effectiveChrono != null ? " with chronology " + effectiveChrono : "") +
	                        (effectiveZone != null ? " with zone " + effectiveZone : "");
	            }
	        };
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Gets the temporal object being output.
	     *
	     * @return the temporal object, not null
	     */
	    TemporalAccessor getTemporal() {
	        return temporal;
	    }

	    /**
	     * Gets the locale.
	     * <p>
	     * This locale is used to control localization in the format output except
	     * where localization is controlled by the DecimalStyle.
	     *
	     * @return the locale, not null
	     */
	    Locale getLocale() {
	        return formatter.getLocale();
	    }

	    /**
	     * Gets the DecimalStyle.
	     * <p>
	     * The DecimalStyle controls the localization of numeric output.
	     *
	     * @return the DecimalStyle, not null
	     */
	    DecimalStyle getDecimalStyle() {
	        return formatter.getDecimalStyle();
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Starts the printing of an optional segment of the input.
	     */
	    void startOptional() {
	        this.optional++;
	    }

	    /**
	     * Ends the printing of an optional segment of the input.
	     */
	    void endOptional() {
	        this.optional--;
	    }

	    /**
	     * Gets a value using a query.
	     *
	     * @param query  the query to use, not null
	     * @return the result, null if not found and optional is true
	     * @throws DateTimeException if the type is not available and the section is not optional
	     */
	    <R> R getValue(TemporalQuery<R> query) {
	        R result = temporal.query(query);
	        if (result == null && optional == 0) {
	            throw new DateTimeException("Unable to extract " +
	                    query + " from temporal " + temporal);
	        }
	        return result;
	    }

	    /**
	     * Gets the value of the specified field.
	     * <p>
	     * This will return the value for the specified field.
	     *
	     * @param field  the field to find, not null
	     * @return the value, null if not found and optional is true
	     * @throws DateTimeException if the field is not available and the section is not optional
	     */
	    Long getValue(TemporalField field) {
	        if (optional > 0 && !temporal.isSupported(field)) {
	            return null;
	        }
	        return temporal.getLong(field);
	    }

	    //-----------------------------------------------------------------------
	    /**
	     * Returns a string version of the context for debugging.
	     *
	     * @return a string representation of the context, not null
	     */
	    @Override
	    public String toString() {
	        return temporal.toString();
	    }

	}
	
	interface DateTimePrinterParser {

        /**
         * Prints the date-time object to the buffer.
         * <p>
         * The context holds information to use during the format.
         * It also contains the date-time information to be printed.
         * <p>
         * The buffer must not be mutated beyond the content controlled by the implementation.
         *
         * @param context  the context to format using, not null
         * @param buf  the buffer to append to, not null
         * @return false if unable to query the value from the date-time, true otherwise
         * @throws DateTimeException if the date-time cannot be printed successfully
         */
        boolean format(DateTimePrintContext context, StringBuilder buf);

        /**
         * Parses text into date-time information.
         * <p>
         * The context holds information to use during the parse.
         * It is also used to store the parsed date-time information.
         *
         * @param context  the context to use and parse into, not null
         * @param text  the input text to parse, not null
         * @param position  the position to start parsing at, from 0 to the text length
         * @return the new parse position, where negative means an error with the
         *  error position encoded using the complement ~ operator
         * @throws NullPointerException if the context or text is null
         * @throws IndexOutOfBoundsException if the position is invalid
         */
        int parse(DateTimeParseContext context, CharSequence text, int position);
    }
	
	static final class CompositePrinterParser implements DateTimePrinterParser {
        private final DateTimePrinterParser[] printerParsers;
        private final boolean optional;

        CompositePrinterParser(List<DateTimePrinterParser> printerParsers, boolean optional) {
            this(printerParsers.toArray(new DateTimePrinterParser[0]), optional);
        }

        CompositePrinterParser(DateTimePrinterParser[] printerParsers, boolean optional) {
            this.printerParsers = printerParsers;
            this.optional = optional;
        }

        /**
         * Returns a copy of this printer-parser with the optional flag changed.
         *
         * @param optional  the optional flag to set in the copy
         * @return the new printer-parser, not null
         */
        public CompositePrinterParser withOptional(boolean optional) {
            if (optional == this.optional) {
                return this;
            }
            return new CompositePrinterParser(printerParsers, optional);
        }

        @Override
        public boolean format(DateTimePrintContext context, StringBuilder buf) {
            int length = buf.length();
            if (optional) {
                context.startOptional();
            }
            try {
                for (DateTimePrinterParser pp : printerParsers) {
                    if (pp.format(context, buf) == false) {
                        buf.setLength(length);  // reset buffer
                        return true;
                    }
                }
            } finally {
                if (optional) {
                    context.endOptional();
                }
            }
            return true;
        }

        @Override
        public int parse(DateTimeParseContext context, CharSequence text, int position) {
            if (optional) {
                context.startOptional();
                int pos = position;
                for (DateTimePrinterParser pp : printerParsers) {
                    pos = pp.parse(context, text, pos);
                    if (pos < 0) {
                        context.endOptional(false);
                        return position;  // return original position
                    }
                }
                context.endOptional(true);
                return pos;
            } else {
                for (DateTimePrinterParser pp : printerParsers) {
                    position = pp.parse(context, text, position);
                    if (position < 0) {
                        break;
                    }
                }
                return position;
            }
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            if (printerParsers != null) {
                buf.append(optional ? "[" : "(");
                for (DateTimePrinterParser pp : printerParsers) {
                    buf.append(pp);
                }
                buf.append(optional ? "]" : ")");
            }
            return buf.toString();
        }
    }
	
	
	private static DateTimeFormatter toFormatter(DateTimeFormatterBuilder e, Locale locale, ResolverStyle resolverStyle, Chronology chrono) {
        Objects.requireNonNull(locale, "locale");
        while (e.active.parent != null) {
            optionalEnd();
        }
        
        DateTimeFormatCompositePrinterParser pp = new CompositePrinterParser(printerParsers, false);
        return new DateTimeFormatter(pp, locale, DecimalStyle.STANDARD,
                resolverStyle, null, chrono, null);
    }
	
	private static DateTimeFormatter toFormatter(DateTimeFormatterBuilder e, ResolverStyle resolverStyle, Chronology chrono) {
        return toFormatter(e, Locale.getDefault(Locale.Category.FORMAT), resolverStyle, chrono);
    }
	
	private static final DateTimeFormatter ISO_LOCAL_TIME_EDIT;
    static {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':');
        
        ISO_LOCAL_TIME_EDIT = toFormatter(builder, ResolverStyle.STRICT, null);
    }
	
	private static final DateTimeFormatter HUMAN_TIMESTAMP_FORMAT; // = DateTimeFormatter.ofPattern("EEE, dd LLL yyyy HH:mm z");
    static {
    	
    	DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_LOCAL_TIME);
    	HUMAN_TIMESTAMP_FORMAT = toFormatter(builder, ResolverStyle.STRICT, IsoChronology.INSTANCE);
    }

	private GraphiteGuild guild;
	private String id;

	private String channelID;
	private String message;
	private ReminderRepetitionEnum repeatMs;
	private LocalDateTime date;

	private LocalDateTime latestPossibleDate;
	private ScheduledFuture<?> finishFuture;

	public GuildReminder(GraphiteGuild guild, LocalDateTime date, String message, ReminderRepetitionEnum repeatMs,
			GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = date;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = GraphiteUtil.randomShortID();
	}

	public GuildReminder(GraphiteGuild guild, String id, LocalDateTime date, LocalDateTime latestPossibleDate,
			String message, ReminderRepetitionEnum repeatMs, GraphiteGuildMessageChannel channel) {
		this.guild = guild;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
		this.message = message;
		this.repeatMs = repeatMs;
		this.channelID = channel.getID();
		this.id = id;
	}

	public GuildReminder(GraphiteGuild guild, String id, String channelID, String message,
			ReminderRepetitionEnum repeatMs, LocalDateTime date, LocalDateTime latestPossibleDate) {
		this.guild = guild;
		this.id = id;
		this.channelID = channelID;
		this.message = message;
		this.repeatMs = repeatMs;
		this.date = date;
		this.latestPossibleDate = latestPossibleDate;
	}

	public String getMessage() {
		return message;
	}

	public GraphiteGuild getGuild() {
		return guild;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getId() {
		return id;
	}

	public ReminderRepetitionEnum getRepeatMs() {
		return repeatMs;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public LocalDateTime getLatestPossibleDate() {
		return latestPossibleDate;
	}

	public ScheduledFuture<?> getFinishFuture() {
		return finishFuture;
	}

	public void remove() {
		try {
			guild.getRemindersConfig().removeReminder(id);
			if (finishFuture != null)
				finishFuture.cancel(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage() {
		Graphite.withBot(Graphite.getGraphiteBot(), () -> {
			GraphiteGuildMessageChannel messageChannel = guild.getGuildMessageChannelByID(channelID);
			if (messageChannel == null) {
				this.remove();
				return;
			}
			try {
				if (repeatMs != null) {
					messageChannel.sendMessageComplete(repeatMs.getFriendlyName() + " Reminder: " + message);
				} else {
					messageChannel.sendMessageComplete("Simple Reminder: " + message);
				}
			} catch (Exception e) {
				throw e;
			}
		});
	}

	private void calculateNextPossibleReminderDate() {
		LocalDateTime now = LocalDateTime.now(guild.getConfig().getTimezone());// Current time with correct UTC for
																				// guild
		while (!latestPossibleDate.isAfter(now)) {
			// maybe
			latestPossibleDate = latestPossibleDate.plusYears(repeatMs.getYearsDisplacement());
			latestPossibleDate = latestPossibleDate.plusMonths(repeatMs.getMonthsDisplacement());
			latestPossibleDate = latestPossibleDate.plusWeeks(repeatMs.getWeeksDisplacement());
			latestPossibleDate = latestPossibleDate.plusDays(repeatMs.getDaysDisplacement());
		}
	}

	public void enqueue() {
		finishFuture = Graphite.getScheduler().getExecutorService().schedule(() -> {
			sendMessage();

			if (repeatMs != null) {
				// Todo reenqueue
				calculateNextPossibleReminderDate();
				enqueue();
			} else {
				// Todo remove from db
				this.remove();
			}
		}, latestPossibleDate.atZone(guild.getConfig().getTimezone()).toEpochSecond() - Instant.now().getEpochSecond(),
				TimeUnit.SECONDS);
	}

	public boolean load() {
		if (this.repeatMs == null)
			return false;
		calculateNextPossibleReminderDate();
		enqueue();
		return true;
	}

	@Override
	public void preSerializeWI(JSONObject object) {
		object.put("guild", getGuild());
		object.put("id", getId());

		object.put("channelID", getChannelID());
		object.put("message", getMessage());
		object.put("repeatMs", getRepeatMs());
		object.put("date", getDate());

		object.put("latestPossibleDate", getLatestPossibleDate());
		object.put("finishFuture", getFinishFuture());
	}

	@JavaScriptFunction(calling = "getReminders", returning = "reminders", withGuild = true)
	public static void getReminders() {
	};

	@JavaScriptFunction(calling = "finishReminder", withGuild = true)
	public static void finishReminder(@JavaScriptParameter(name = "id") String id) {
	}

}
