package net.eithon.plugin.stats.db;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccumulatedPojo {
	public long id;
	public UUID player_id;
	public LocalDateTime first_start_time_utc;
	public LocalDateTime last_stop_time_utc;
	public long play_time_in_seconds;
	public long joins;
	public long longest_interval_in_seconds;
	public long play_time_today_in_seconds;
	public LocalDateTime today;
	public long chat_messages;
	public LocalDateTime last_chat_message;
	public long blocks_created;
	public long blocks_broken;
	public long consecutive_days;
	public LocalDateTime last_consecutive_day;
}
