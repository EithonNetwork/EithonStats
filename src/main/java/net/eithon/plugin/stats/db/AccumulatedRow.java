package net.eithon.plugin.stats.db;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;

import net.eithon.library.mysql.IRow;
import net.eithon.library.mysql.Row;

public class AccumulatedRow extends Row {
	public AccumulatedRow() {
		super("accumulated");
	}
	
	public String player_id;
	public Timestamp first_start_utc;
	public Timestamp last_stop_utc;
	public long play_time_in_seconds;
	public long joins;
	public long longest_interval_in_seconds;
	public long play_time_today_in_seconds;
	public Date today;
	public long chat_messages;
	public Timestamp last_chat_message_utc;
	public BigInteger blocks_created;
	public BigInteger blocks_broken;
	public long consecutive_days;
	public Date last_consecutive_day;
}
