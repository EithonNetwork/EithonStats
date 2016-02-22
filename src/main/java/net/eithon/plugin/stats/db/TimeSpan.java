package net.eithon.plugin.stats.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;

public class TimeSpan {
	private Database _database;
	private long _dbId;
	private LocalDateTime _hour;
	private UUID _playerId;
	private long _playTimeInSeconds;
	private long _chatMessages;
	private long _blocksCreated;
	private long _blocksBroken;

	private TimeSpan(final Database database) {
		this._database = database;
	}

	private TimeSpan(final Database database, final UUID playerId, final LocalDateTime hour) {
		this(database);
		this._playerId = playerId;
		this._hour = hour.truncatedTo(ChronoUnit.HOURS);
	}

	public static TimeSpan getByPlayerIdHour(final Database database, final UUID playerId, LocalDateTime hour) throws SQLException, ClassNotFoundException {
		hour = hour.truncatedTo(ChronoUnit.HOURS);
		TimeSpan timespan = new TimeSpan(database);
		String sql = String.format("SELECT * FROM timespan WHERE player_id='%s' AND hour_utc='%s'",
				playerId.toString(), hour.toString());
		Statement statement = database.getOrOpenConnection().createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		timespan.readMeta(resultSet);
		timespan.readData(resultSet);
		return timespan;
	}

	public static TimeSpan sumPlayer(final Database database, final UUID playerId, 
			LocalDateTime from, LocalDateTime to) throws SQLException, ClassNotFoundException {
		TimeSpan timespan = new TimeSpan(database);
		String sql = String.format("SELECT" +
				" SUM(play_time_in_seconds) AS play_time_in_seconds" + 
				", SUM(chat_messages) AS chat_messages " +
				", SUM(blocks_created) AS blocks_created " +
				", SUM(blocks_broken) AS blocks_broken " +
				" FROM timespan WHERE player_id='%s' AND hour_utc>='%s' AND hour_utc<='%s'",
				playerId.toString(), from.toString(), to.toString());
		Statement statement = database.getOrOpenConnection().createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		if (!resultSet.next()) return null;
		timespan._dbId = 0;
		timespan._hour = from.truncatedTo(ChronoUnit.HOURS);
		timespan._playerId = playerId;
		return timespan.readData(resultSet);
	}

	public void update(
			final long totalPlayTimeInSeconds,
			final long chatMessages,
			final long blocksCreated, 
			final long blocksBroken) throws SQLException, ClassNotFoundException {
		updateLocalData(totalPlayTimeInSeconds, chatMessages, blocksCreated, blocksBroken);
		String sql = String.format("UPDATE timespan SET" +
				" play_time_in_seconds = %d" + 
				", chat_messages = %d" + 
				", blocks_created = %d" + 
				", blocks_broken = %d" +
				" WHERE id=%d",
				totalPlayTimeInSeconds, chatMessages, blocksCreated, blocksBroken, this._dbId);
		Statement statement = this._database.getOrOpenConnection().createStatement();
		statement.executeUpdate(sql);
	}

	public static TimeSpan create(final Database database, 
			final UUID playerId,
			final LocalDateTime hour,
			final long totalPlayTimeInSeconds,
			final long chatMessages,
			final long blocksCreated, final long blocksBroken) throws SQLException, ClassNotFoundException {
		TimeSpan timespan = new TimeSpan(database, playerId, hour);
		timespan.updateLocalData(totalPlayTimeInSeconds, chatMessages, blocksCreated, blocksBroken);
		timespan.insert();
		return timespan;
	}

	private void updateLocalData(final long totalPlayTimeInSeconds,
			final long chatMessages, final long blocksCreated,
			final long blocksBroken) {
		this._playTimeInSeconds = totalPlayTimeInSeconds;
		this._chatMessages = chatMessages;
		this._blocksCreated = blocksCreated;
		this._blocksBroken = blocksBroken;
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

	public long get_chatMessages() {
		return this._chatMessages;
	}

	public long get_blocksCreated() {
		return this._blocksCreated;
	}

	public long get_blocksBroken() {
		return this._blocksBroken;
	}

	private void insert() throws SQLException, ClassNotFoundException {
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
				this._playTimeInSeconds, this._chatMessages, this._blocksCreated,
				this._blocksBroken);
		Statement statement = this._database.getOrOpenConnection().createStatement();
		statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
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
		this._chatMessages = resultSet.getLong("chat_messages");
		this._blocksCreated = resultSet.getLong("blocks_created");
		this._blocksBroken = resultSet.getLong("blocks_broken");
		return this;
	}
}
