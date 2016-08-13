package net.eithon.plugin.stats.db;

import java.time.LocalDateTime;
import java.util.UUID;

public class TimeSpanPojo {
	public long id;
	public LocalDateTime hour;
	public UUID player_id;
	public long play_time_in_seconds;
	public long chat_messages;
	public long blocks_created;
	public long blocks_broken;
}
