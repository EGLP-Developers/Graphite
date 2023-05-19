package me.eglp.gv2.commands.fun;

import java.util.Collections;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import me.eglp.gv2.guild.GraphiteModule;
import me.eglp.gv2.main.Graphite;
import me.eglp.gv2.util.command.Command;
import me.eglp.gv2.util.command.CommandCategory;
import me.eglp.gv2.util.command.CommandInvokedEvent;
import me.eglp.gv2.util.command.slash.DeferredReply;
import me.eglp.gv2.util.lang.DefaultLocaleString;
import me.eglp.gv2.util.selfcheck.SpecialSelfcheck;
import me.mrletsplay.mrcore.http.HttpRequest;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.JSONParseException;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class CommandChuckNorris extends Command{

	public CommandChuckNorris() {
		super(GraphiteModule.FUN, CommandCategory.FUN, "chucknorris");
		setDescription(DefaultLocaleString.COMMAND_CHUCKNORRIS_DESCRIPTION);
		setUsage(DefaultLocaleString.COMMAND_CHUCKNORRIS_USAGE);
	}

	@SpecialSelfcheck(needsPermission = false)
	@Override
	public void action(CommandInvokedEvent event) {
		DeferredReply d = event.deferReply();
		try {
			JSONObject o = HttpRequest.createGet("http://api.icndb.com/jokes/random/").execute().asJSONObject();
			String joke = o.getJSONObject("value").getString("joke");
			d.editOriginal(StringEscapeUtils.unescapeHtml4(joke));
		} catch (JSONParseException e) {
			Graphite.log("Error at chucknorris -> " + e.getMessage());
		}
	}

	@Override
	public List<OptionData> getOptions() {
		return Collections.emptyList();
	}

}
