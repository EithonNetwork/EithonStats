package net.eithon.plugin.stats.logic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;
import net.eithon.plugin.stats.db.TimeSpan;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HourStatistics implements IUuidAndName {
	private static Logger eithonLogger;

	// Saved variables
	private UUID _playerId;
	private String _playerName;
	private long _blocksBroken;
	private long _blocksCreated;
	private long _chatActivities;
	private long _playtimeInSeconds;
	private LocalDateTime _hour;
	private long _dbId;

	public HourStatistics(Connection connection, HourStatistics earlier, PlayerStatistics now, LocalDateTime time) throws SQLException {
		HourStatistics later = new HourStatistics(now, time);
		this._playerId = earlier._playerId;
		this._playerName = earlier._playerName;
		this._hour = later._hour;
		this._blocksBroken = later._blocksBroken - earlier._blocksBroken;
		this._blocksCreated = later._blocksCreated - earlier._blocksCreated;
		this._chatActivities = later._chatActivities - earlier._chatActivities;
		this._playtimeInSeconds = later._playtimeInSeconds - earlier._playtimeInSeconds;
		insertTimeSpan(connection, this._hour);
		toDb(connection);
	}

	public HourStatistics(Connection connection, EithonPlayer player, LocalDateTime fromTime, LocalDateTime toTime) throws SQLException
	{
		TimeSpan timeSpan = TimeSpan.sumPlayer(connection, player.getUniqueId(), fromTime, toTime);
		this._playerId = player.getUniqueId();
		this._playerName = player.getName();
		this._hour = fromTime.truncatedTo(ChronoUnit.HOURS);
		fromDb(timeSpan);
		return;
	}

	private HourStatistics(PlayerStatistics playerStatistics, LocalDateTime time)
	{
		this._playerId = playerStatistics.getUniqueId();
		this._playerName = playerStatistics.getName();
		this._hour = time.truncatedTo(ChronoUnit.HOURS);
		this._blocksBroken = playerStatistics.getBlocksBroken();
		this._blocksCreated = playerStatistics.getBlocksCreated();
		this._chatActivities = playerStatistics.getChats();
		this._playtimeInSeconds = playerStatistics.getTotalTimeInSeconds();
		this._dbId = -1;
	}

	public HourStatistics() {
		initialize();
	}

	private void insertTimeSpan(Connection connection, LocalDateTime hour)
			throws SQLException {
		TimeSpan timeSpan = TimeSpan.create(connection, this._playerId, hour, this._playtimeInSeconds, 
				this._chatActivities, this._blocksCreated, this._blocksBroken);
		this._dbId = timeSpan.get_dbId();
	}

	private void initialize() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._playtimeInSeconds = 0;
	}

	private HourStatistics fromDb(TimeSpan timeSpan)  {
		this._playerId = timeSpan.get_playerId();
		this._playerName = Bukkit.getServer().getOfflinePlayer(this._playerId).getName();
		this._dbId = timeSpan.get_dbId();
		this._chatActivities = timeSpan.get_chatActivities();
		this._blocksCreated = timeSpan.get_blocksCreated();
		this._blocksBroken = timeSpan.get_blocksBroken();
		this._playtimeInSeconds = timeSpan.get_playTimeInSeconds();
		return this;
	}

	public void toDb(Connection connection) throws SQLException {
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "HourStatistics.toDB: Enter for player %s", this.getName());
		String updates = getDbUpdates();
		String update = String.format("UPDATE accumulated SET %s WHERE id=%d", updates, this._dbId);
		Statement statement = connection.createStatement();
		statement.executeUpdate(update);
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerSHourStatisticstatistics.toDB: Leave");
	}

	private String getDbUpdates() {
		String updates = String.format("player_id='%s'", this._playerId) +
				String.format(", player_name='%s'", this._playerName) +
				String.format(", chat_messages=%d", this._chatActivities) +
				String.format(", blocks_created=%d", this._blocksCreated) +
				String.format(", blocks_broken=%d", this._blocksBroken) +
				String.format(", playtime_in_seconds=%d", this._playtimeInSeconds);
		return updates;
	}

	public String getName() {
		return this._playerName;
	}

	public UUID getUniqueId() { 
		return this._playerId;
	}

	public void sendDiffStats(CommandSender sender) {
		Config.M.diffStats.sendMessage(sender, getNamedArguments());
	}

	private HashMap<String,String> getNamedArguments() {
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", this._playerName);
		namedArguments.put("BLOCKS_BROKEN", String.format("%d", this._blocksBroken));
		namedArguments.put("BLOCKS_CREATED", String.format("%d", this._blocksCreated));
		namedArguments.put("BLOCKS_CREATED_OR_BROKEN", String.format("%d", this._blocksCreated + this._blocksBroken));
		namedArguments.put("CHAT_ACTIVITIES", String.format("%d", this._chatActivities));
		namedArguments.put("TOTAL_PLAY_TIME", TimeMisc.secondsToString(this._playtimeInSeconds));
		return namedArguments;
	}

	public long getPlayTimeInSeconds() { return this._playtimeInSeconds; }
}
