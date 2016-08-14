package net.eithon.plugin.stats.db;

import java.sql.Timestamp;

import net.eithon.library.mysql.ITable;

public class TimeSpanPojo implements ITable {
	public long id;
	public Timestamp hour_utc;
	public String player_id;
	public long play_time_in_seconds;
	public long chat_messages;
	public long blocks_created;
	public long blocks_broken;

	public long getId() { return this.id; }
	public String getTableName() { return "timespan"; }
}
