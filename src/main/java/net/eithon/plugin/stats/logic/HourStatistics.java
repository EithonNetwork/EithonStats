package net.eithon.plugin.stats.logic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;

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
		insertHourly(connection, this._hour);
		toDb(connection);
	}

	public HourStatistics(Connection connection, Player player, LocalDateTime time) throws SQLException
	{
		this._hour = time.truncatedTo(ChronoUnit.HOURS);

		String sql = String.format("SELECT * FROM hourly WHERE player_id='%s' AND hour_utc='%s'",
				player.getUniqueId(), TimeMisc.toDbUtc(this._hour));
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		resultSet.next();
		if (resultSet != null) {
			fromDb(resultSet);
			return;
		}
	}

	public HourStatistics(PlayerStatistics playerStatistics, LocalDateTime time)
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

	private void insertHourly(Connection connection, LocalDateTime hour)
			throws SQLException {
		String sql = String.format("INSERT INTO `accumulated`" +
				" (`hour_utc`, `player_id`, `player_name`) VALUES ('%s', '%s', '%s')",
				TimeMisc.toDbUtc(this._hour), this._playerId.toString(), this._playerName);
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		this._dbId = generatedKeys.getLong(1);
	}

	private void initialize() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._playtimeInSeconds = 0;
	}

	private HourStatistics fromDb(ResultSet resultSet) throws SQLException {
		String playerIdAsString = resultSet.getString("player_id");
		if (playerIdAsString == null) throw new IllegalArgumentException("Could not find player_id in resultSet.");
		try {
			this._playerId = UUID.fromString(playerIdAsString);
		} catch (Exception e) {
			throw new IllegalArgumentException("player_id was not a UUID", e);
		}
		this._playerName = resultSet.getString("playerName");
		this._dbId = resultSet.getLong("id");
		this._chatActivities = resultSet.getLong("chat_messages");
		this._blocksCreated = resultSet.getLong("blocks_created");
		this._blocksBroken = resultSet.getLong("blocks_broken");
		this._playtimeInSeconds = resultSet.getLong("playtime_in_seconds");
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
}
