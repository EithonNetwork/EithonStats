package net.eithon.plugin.stats.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;

public class Accumulated {
	private Database _database;
	private long _dbId;
	private UUID _playerId;
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private long _totalPlayTimeInSeconds;
	private long _joins;
	private long _longestIntervalInSeconds;
	private long _playTimeTodayInSeconds;
	private LocalDateTime _today;
	private long _chatMessages;
	private LocalDateTime _lastChatMessage;
	private long _blocksCreated;
	private long _blocksBroken;
	private long _consecutiveDays;
	private LocalDateTime _lastConsecutiveDay;
	
	private Accumulated(final Database database) {
		this._database = database;
	}
	
	private Accumulated(final Database database, final UUID playerId) throws SQLException, ClassNotFoundException {
		this(database);
		String sql = String.format("INSERT INTO accumulated" +
				" (player_id) VALUES ('%s')",
				playerId.toString());
		Statement statement = this._database.getOrOpenConnection().createStatement();
		statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		this._dbId = generatedKeys.getLong(1);
	}

	public static Accumulated getByPlayerId(final Database database, final UUID playerId) throws SQLException, ClassNotFoundException {
		Accumulated accumulated = new Accumulated(database);
		String sql = String.format("SELECT * FROM accumulated WHERE player_id='%s'", playerId.toString());
		Statement statement = accumulated._database.getOrOpenConnection().createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		return accumulated.fromDb(resultSet );
	}
	
	public static Accumulated create(final Database database, final UUID playerId) throws SQLException, ClassNotFoundException {
		new Accumulated(database, playerId);
		return getByPlayerId(database, playerId);
	}
	
	public void update(final String playerName, final LocalDateTime firstStartTime,
			final LocalDateTime lastStopTime, final long totalPlayTimeInSeconds,
			final long joins, final long longestIntervalInSeconds,
			final long playTimeTodayInSeconds, final LocalDateTime today,
			final long chatActivities, final LocalDateTime lastChatActivity,
			final long blocksCreated, final long blocksBroken, final long consecutiveDays,
			final LocalDateTime lastConsecutiveDay) throws SQLException, ClassNotFoundException {
		this._firstStartTime = firstStartTime;
		this._lastStopTime = lastStopTime;
		this._totalPlayTimeInSeconds = totalPlayTimeInSeconds;
		this._joins = joins;
		this._longestIntervalInSeconds = longestIntervalInSeconds;
		this._playTimeTodayInSeconds = playTimeTodayInSeconds;
		this._today = today;
		this._chatMessages = chatActivities;
		this._lastChatMessage = lastChatActivity;
		this._blocksCreated = blocksCreated;
		this._blocksBroken = blocksBroken;
		this._consecutiveDays = consecutiveDays;
		this._lastConsecutiveDay = lastConsecutiveDay;
		toDb();
	}

	public Database getDatabase() { return this._database; }
	public long get_dbId() { return this._dbId; }
	public UUID get_playerId() { return this._playerId; }
	public LocalDateTime get_firstStartTime() { return this._firstStartTime; }
	public LocalDateTime get_lastStopTime() { return this._lastStopTime; }
	public long get_totalPlayTimeInSeconds() { return this._totalPlayTimeInSeconds; }
	public long get_joins() { return this._joins; }
	public long get_longestIntervalInSeconds() { return this._longestIntervalInSeconds; }
	public long get_playTimeTodayInSeconds() { return this._playTimeTodayInSeconds; }
	public LocalDateTime get_today() { return this._today; }
	public long get_chatMessages() { return this._chatMessages; }
	public LocalDateTime get_lastChatMessage() { return this._lastChatMessage; }
	public long get_blocksCreated() { return this._blocksCreated; }
	public long get_blocksBroken() { return this._blocksBroken; }
	public long get_consecutiveDays() { return this._consecutiveDays; }
	public LocalDateTime get_lastConsecutiveDay() { return this._lastConsecutiveDay; }

	private Accumulated fromDb(final ResultSet resultSet) throws SQLException {
		this._dbId = resultSet.getLong("id");
		this._playerId = UUID.fromString(resultSet.getString("player_id"));
		this._firstStartTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("first_start_utc"));
		this._lastStopTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_stop_utc"));
		this._totalPlayTimeInSeconds = resultSet.getLong("play_time_in_seconds");
		this._joins = resultSet.getLong("joins");
		this._longestIntervalInSeconds = resultSet.getLong("longest_interval_in_seconds");
		this._playTimeTodayInSeconds = resultSet.getLong("play_time_today_in_seconds");
		this._today = TimeMisc.toLocalDateTime(resultSet.getTimestamp("today"));
		this._chatMessages = resultSet.getLong("chat_messages");
		this._lastChatMessage = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_chat_message_utc"));
		this._blocksCreated = resultSet.getLong("blocks_created");
		this._blocksBroken = resultSet.getLong("blocks_broken");
		this._consecutiveDays = resultSet.getLong("consecutive_days");
		this._lastConsecutiveDay = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_consecutive_day"));
		return this;
	}

	public void toDb() throws SQLException, ClassNotFoundException {
		String updates = getDbUpdates();
		String update = String.format("UPDATE accumulated SET %s WHERE id=%d", updates, this._dbId);
		Statement statement = this._database.getOrOpenConnection().createStatement();
		statement.executeUpdate(update);
	}

	private String getDbUpdates() {
		String updates = String.format("player_id='%s'", this._playerId.toString()) +
				String.format(", chat_messages=%d", this._chatMessages) +
				String.format(", blocks_created=%d", this._blocksCreated) +
				String.format(", blocks_broken=%d", this._blocksBroken) +
				String.format(", consecutive_days=%d", this._consecutiveDays) +
				String.format(", last_consecutive_day=%s", TimeMisc.toDbUtc(this._lastConsecutiveDay)) +
				String.format(", last_chat_message_utc=%s", TimeMisc.toDbUtc(this._lastChatMessage)) + 
				String.format(", player_id='%s'", this._playerId) +
				String.format(", chat_messages=%d", this._chatMessages) +
				String.format(", blocks_created=%d", this._blocksCreated) +
				String.format(", blocks_broken=%d", this._blocksBroken) +
				String.format(", play_time_in_seconds=%d", this._totalPlayTimeInSeconds) +
				String.format(", first_start_utc=%s", TimeMisc.toDbUtc(this._firstStartTime)) +
				String.format(", last_stop_utc=%s", TimeMisc.toDbUtc(this._lastStopTime)) +
				String.format(", today=%s", TimeMisc.toDbUtc(this._today)) +
				String.format(", play_time_in_seconds=%d", this._totalPlayTimeInSeconds) +
				String.format(", joins=%d", this._joins) +
				String.format(", longest_interval_in_seconds=%d", this._longestIntervalInSeconds) +
				String.format(", play_time_today_in_seconds=%d", this._playTimeTodayInSeconds);
		return updates;
	}
}
