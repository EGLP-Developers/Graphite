package me.eglp.gv2.util.backup.data.bans;

import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class BansData implements JSONConvertible {
	
	@JSONValue
	@JSONComplexListType(BackupBan.class)
	private List<BackupBan> bans;
	
	@JSONConstructor
	private BansData() {}
	
	public BansData(GraphiteGuild guild) {
		if(guild.getJDAGuild() == null) throw new IllegalStateException("Unknown guild or invalid context");
		
		this.bans = guild.getJDAGuild().retrieveBanList().complete().stream()
				.map(BackupBan::new)
				.collect(Collectors.toList());
	}
	
	public List<BackupBan> getBans() {
		return bans;
	}
	
	public void restore(GraphiteGuild guild) {
		//Maybe ne option ob alte unbannned werden sollen
		guild.getJDAGuild().retrieveBanList().complete().forEach(b -> {
			guild.getJDAGuild().unban(b.getUser()).complete();
		});
		
		bans.stream().forEach(b -> b.restore(guild));
	}
	
	public static BansData load(String json) {
		return JSONConverter.decodeObject(new JSONObject(json), BansData.class);
	}
	
}
