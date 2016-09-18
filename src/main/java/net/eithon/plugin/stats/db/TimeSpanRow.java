package net.eithon.plugin.stats.db;

import java.sql.Timestamp;

import net.eithon.library.mysql.Row;

public class TimeSpanRow extends Row {
	public TimeSpanRow() {
		super("timespan");
	}
	
	public Timestamp hour_utc;
	public String player_id;
	public long play_time_in_seconds;
	public long chat_messages;
	public long blocks_created;
	public long blocks_broken;
}
