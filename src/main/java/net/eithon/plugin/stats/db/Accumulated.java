package net.eithon.plugin.stats.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.time.TimeMisc;

public class Accumulated {
	private Connection _connection;
	private long _dbId;
	private UUID _playerId;
	private String _playerName;
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private long _totalPlayTimeInSeconds;
	private long _intervals;
	private long _longestIntervalInSeconds;
	private long _playTimeTodayInSeconds;
	private LocalDateTime _today;
	private long _chatActivities;
	private LocalDateTime _lastChatActivity;
	private long _blocksCreated;
	private long _blocksBroken;
	private long _consecutiveDays;
	private LocalDateTime _lastConsecutiveDay;
	
	private Accumulated(Connection connection) {
		this._connection = connection;
	}
	
	private Accumulated(Connection connection, UUID playerId, String playerName) throws SQLException {
		this(connection);
		String sql = String.format("INSERT INTO `accumulated`" +
				" (`player_id`, `player_name`) VALUES ('%s', '%s')",
				playerId.toString(), playerName);
		Statement statement = connection.createStatement();
		statement.executeUpdate(sql);
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		this._dbId = generatedKeys.getLong(1);
	}

	public static Accumulated getByPlayerId(final Connection connection, final UUID playerId) throws SQLException {
		Accumulated accumulated = new Accumulated(connection);
		String sql = String.format("SELECT * FROM accumulated WHERE player_id='%s'", playerId.toString());
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		return accumulated.fromDb(resultSet );
	}
	
	public static Accumulated create(final Connection connection, final UUID playerId, final String playerName) throws SQLException {
		Accumulated row = new Accumulated(connection, playerId, playerName);
		return getByPlayerId(connection, playerId);
	}
	
	public void update(final String playerName, final LocalDateTime firstStartTime,
			final LocalDateTime lastStopTime, final long totalPlayTimeInSeconds,
			final long intervals, final long longestIntervalInSeconds,
			final long playTimeTodayInSeconds, final LocalDateTime today,
			final long chatActivities, final LocalDateTime lastChatActivity,
			final long blocksCreated, final long blocksBroken, final long consecutiveDays,
			final LocalDateTime lastConsecutiveDay) throws SQLException {
		this._playerName = playerName;
		this._firstStartTime = firstStartTime;
		this._lastStopTime = lastStopTime;
		this._totalPlayTimeInSeconds = totalPlayTimeInSeconds;
		this._intervals = intervals;
		this._longestIntervalInSeconds = longestIntervalInSeconds;
		this._playTimeTodayInSeconds = playTimeTodayInSeconds;
		this._today = today;
		this._chatActivities = chatActivities;
		this._lastChatActivity = lastChatActivity;
		this._blocksCreated = blocksCreated;
		this._blocksBroken = blocksBroken;
		this._consecutiveDays = consecutiveDays;
		this._lastConsecutiveDay = lastConsecutiveDay;
		toDb();
	}
	
	public long get_dbId() {
		return this._dbId;
	}

	public UUID get_playerId() {
		return this._playerId;
	}

	public String get_playerName() {
		return this._playerName;
	}

	public LocalDateTime get_firstStartTime() {
		return this._firstStartTime;
	}

	public LocalDateTime get_lastStopTime() {
		return this._lastStopTime;
	}

	public long get_totalPlayTimeInSeconds() {
		return this._totalPlayTimeInSeconds;
	}

	public long get_intervals() {
		return this._intervals;
	}

	public long get_longestIntervalInSeconds() {
		return this._longestIntervalInSeconds;
	}

	public long get_playTimeTodayInSeconds() {
		return this._playTimeTodayInSeconds;
	}

	public LocalDateTime get_today() {
		return this._today;
	}

	public long get_chatActivities() {
		return this._chatActivities;
	}

	public LocalDateTime get_lastChatActivity() {
		return this._lastChatActivity;
	}

	public long get_blocksCreated() {
		return this._blocksCreated;
	}

	public long get_blocksBroken() {
		return this._blocksBroken;
	}

	public long get_consecutiveDays() {
		return this._consecutiveDays;
	}

	public LocalDateTime get_lastConsecutiveDay() {
		return this._lastConsecutiveDay;
	}

	private Accumulated fromDb(final ResultSet resultSet) throws SQLException {
		this._dbId = resultSet.getLong("id");
		this._playerId = UUID.fromString(resultSet.getString("player_id"));
		this._playerName = resultSet.getString("player_name");
		this._firstStartTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("first_start_utc"));
		this._lastStopTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_stop_utc"));
		this._totalPlayTimeInSeconds = resultSet.getLong("play_time_in_seconds");
		this._intervals = resultSet.getLong("intervals");
		this._longestIntervalInSeconds = resultSet.getLong("longest_interval_in_seconds");
		this._playTimeTodayInSeconds = resultSet.getLong("play_time_today_in_seconds");
		this._today = TimeMisc.toLocalDateTime(resultSet.getTimestamp("today"));
		this._chatActivities = resultSet.getLong("chat_messages");
		this._lastChatActivity = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_chat_message_utc"));
		this._blocksCreated = resultSet.getLong("blocks_created");
		this._blocksBroken = resultSet.getLong("blocks_broken");
		this._consecutiveDays = resultSet.getLong("consecutive_days");
		this._lastConsecutiveDay = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_consecutive_day"));
		return this;
	}

	public void toDb() throws SQLException {
		String updates = getDbUpdates();
		String update = String.format("UPDATE accumulated SET %s WHERE id=%d", updates, this._dbId);
		Statement statement = this._connection.createStatement();
		statement.executeUpdate(update);
	}

	private String getDbUpdates() {
		String updates = String.format("player_id='%s'", this._playerId.toString()) +
				String.format(", player_name='%s'", this._playerName) +
				String.format(", chat_messages=%d", this._chatActivities) +
				String.format(", blocks_created=%d", this._blocksCreated) +
				String.format(", blocks_broken=%d", this._blocksBroken) +
				String.format(", consecutive_days=%d", this._consecutiveDays) +
				String.format(", last_consecutive_day='%s'", TimeMisc.toDbUtc(this._lastConsecutiveDay)) +
				String.format(", last_chat_message='%s'", TimeMisc.toDbUtc(this._lastChatActivity)) + 
				String.format("player_id='%s'", this._playerId) +
				String.format(", player_name='%s'", this._playerName) +
				String.format(", chat_messages=%d", this._chatActivities) +
				String.format(", blocks_created=%d", this._blocksCreated) +
				String.format(", blocks_broken=%d", this._blocksBroken) +
				String.format(", playtime_in_seconds=%d", this._totalPlayTimeInSeconds) +
				String.format("first_start_time='%s'", TimeMisc.toDbUtc(this._firstStartTime)) +
				String.format(", last_stop_time='%s'", TimeMisc.toDbUtc(this._lastStopTime)) +
				String.format(", today='%s'", TimeMisc.toDbUtc(this._today)) +
				String.format(", play_time_in_seconds=%d", this._totalPlayTimeInSeconds) +
				String.format(", intervals=%d", this._intervals) +
				String.format(", longest_interval_in_seconds=%d", this._longestIntervalInSeconds) +
				String.format(", play_time_today_in_seconds=%d", this._playTimeTodayInSeconds);
		return updates;
	}
}
