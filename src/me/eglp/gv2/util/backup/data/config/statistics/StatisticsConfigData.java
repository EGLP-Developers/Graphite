package me.eglp.gv2.util.backup.data.config.statistics;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import me.eglp.gv2.util.backup.IDMappings;
import me.eglp.gv2.util.backup.RestoreSelector;
import me.eglp.gv2.util.base.guild.GraphiteGuild;
import me.mrletsplay.mrcore.json.converter.JSONComplexListType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class StatisticsConfigData implements JSONConvertible {
	
	@JSONValue
	@JSONComplexListType(BackupStatisticsElement.class)
	private List<BackupStatisticsElement> elements;
	
	@JSONConstructor
	private StatisticsConfigData() {}
	
	public StatisticsConfigData(GraphiteGuild guild) {
		this.elements = guild.getStatisticsConfig().getStatisticsElements().stream()
				.map(BackupStatisticsElement::new)
				.collect(Collectors.toList());
	}
	
	public void restore(GraphiteGuild guild, EnumSet<RestoreSelector> selectors, IDMappings mappings) {
		if(!selectors.contains(RestoreSelector.STATISTICS)) return;
		
		guild.getStatisticsConfig().removeAllStatisticsElements();
		
		for(BackupStatisticsElement el : elements) {
			el.restore(guild, mappings);
		}
	}

}
