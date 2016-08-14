package net.eithon.plugin.stats.db;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import net.eithon.library.mysql.ITable;

public class AccumulatedPojo implements ITable{
	public long id;
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

	public long getId() { return this.id; }
	public String getTableName() { return "accumulated"; }
}
