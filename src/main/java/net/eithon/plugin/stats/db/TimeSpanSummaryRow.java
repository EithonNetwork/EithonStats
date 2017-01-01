package net.eithon.plugin.stats.db;

import net.eithon.library.mysql.Row;

public class TimeSpanSummaryRow extends Row {
	public TimeSpanSummaryRow() {
		super("timespan");
	}
	
	public long play_time_in_seconds;
	public long chat_messages;
	public long blocks_created;
	public long blocks_broken;
}
