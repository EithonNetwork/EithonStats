package net.eithon.plugin.stats.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.time.TimeMisc;

public class TimeSpan {
	private Connection _connection;
	private long _dbId;
	private LocalDateTime _hour;
	private UUID _playerId;
	private long _playTimeInSeconds;
	private long _chatActivities;
	private long _blocksCreated;
	private long _blocksBroken;

	private TimeSpan(Connection connection) {
		this._connection = connection;
	}

	private TimeSpan(Connection connection, UUID playerId, LocalDateTime hour) {
		this(connection);
		this._playerId = playerId;
		this._hour = hour.truncatedTo(ChronoUnit.HOURS);
	}

	public static TimeSpan getByPlayerIdHour(final Connection connection, final UUID playerId, LocalDateTime hour) throws SQLException {
		hour = hour.truncatedTo(ChronoUnit.HOURS);
		TimeSpan timespan = new TimeSpan(connection);
		String sql = String.format("SELECT * FROM timespan WHERE player_id='%s' AND hour_utc='%s'",
				playerId.toString(), hour.toString());
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		timespan.readMeta(resultSet);
		timespan.readData(resultSet);
		return timespan;
	}

	public static TimeSpan sumPlayer(final Connection connection, final UUID playerId, 
			LocalDateTime from, LocalDateTime to) throws SQLException {
		TimeSpan timespan = new TimeSpan(connection);
		String sql = String.format("SELECT" +
				"SUM(play_time_in_seconds) AS play_time_in_seconds" + 
				", SUM(chat_messages) AS chat_messages, " +
				", SUM(blocks_created) AS blocks_created, " +
				", SUM(blocks_broken) AS blocks_broken, " +
				", SUM(joins) AS joins, " +
				" FROM timespan WHERE player_id='%s' AND hour_utc>='%s' AND hour_utc<='%s'",
				playerId.toString(), from.toString(), to.toString());
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		timespan._dbId = 0;
		timespan._hour = from.truncatedTo(ChronoUnit.HOURS);
		timespan._playerId = playerId;
		return timespan.readData(resultSet);
	}

	public static TimeSpan create(final Connection connection, 
			final UUID playerId,
			final LocalDateTime hour,
			final long totalPlayTimeInSeconds,
			final long chatActivities,
			final long blocksCreated, final long blocksBroken) throws SQLException {
		TimeSpan timespan = new TimeSpan(connection, playerId, hour);
		timespan._playTimeInSeconds = totalPlayTimeInSeconds;
		timespan._chatActivities = chatActivities;
		timespan._blocksCreated = blocksCreated;
		timespan._blocksBroken = blocksBroken;
		timespan.insert();
		return timespan;
	}

	public long get_dbId() {
		return this._dbId;
	}

	public UUID get_playerId() {
		return this._playerId;
	}

	public LocalDateTime get_hour() {
		return this._hour;
	}

	public long get_playTimeInSeconds() {
		return this._playTimeInSeconds;
	}

	public long get_chatActivities() {
		return this._chatActivities;
	}

	public long get_blocksCreated() {
		return this._blocksCreated;
	}

	public long get_blocksBroken() {
		return this._blocksBroken;
	}

	private void insert() throws SQLException {
		String sql = String.format("INSERT INTO timespan" +
				" (" + 
				"player_id, hour_utc" +
				", play_time_in_seconds, chat_messages" + 
				", blocks_created, blocks_broken" +
				") VALUES (" +
				"'%s', '%s'" + 
				", %d, %d" + 
				", %d, %d" + 
				")",
				this._playerId.toString(), this._hour.toString(),
				this._playTimeInSeconds, this._chatActivities, this._blocksCreated,
				this._blocksBroken);
		Statement statement = this._connection.createStatement();
		statement.executeUpdate(sql);
		ResultSet generatedKeys = statement.getGeneratedKeys();
		generatedKeys.next();
		this._dbId = generatedKeys.getLong(1);
	}

	private TimeSpan readMeta(final ResultSet resultSet) throws SQLException {
		this._dbId = resultSet.getLong("id");
		this._playerId = UUID.fromString(resultSet.getString("player_id"));
		this._hour = TimeMisc.toLocalDateTime(resultSet.getTimestamp("hour_utc"));
		return this;
	}

	private TimeSpan readData(final ResultSet resultSet) throws SQLException {
		this._playTimeInSeconds = resultSet.getLong("play_time_in_seconds");
		this._chatActivities = resultSet.getLong("chat_messages");
		this._blocksCreated = resultSet.getLong("blocks_created");
		this._blocksBroken = resultSet.getLong("blocks_broken");
		return this;
	}
}
