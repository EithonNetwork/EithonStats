package net.eithon.plugin.stats.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class TimeSpanTable extends DbTable<TimeSpanRow> {

	public TimeSpanTable(final Database database) throws FatalException {
		super(TimeSpanRow.class, database);
	}

	public TimeSpanRow insert(final UUID playerId, LocalDateTime hour, long playtimeInSeconds, long chatActivities, long blocksCreated, long blocksBroken) throws FatalException, TryAgainException {
		TimeSpanRow timeSpan = new TimeSpanRow();
		timeSpan.player_id = playerId.toString();
		timeSpan.hour_utc = Timestamp.valueOf(hour);
		long id = this.jDapper.createOne(timeSpan);
		return this.jDapper.read(id);
	}

	public TimeSpanRow getByPlayerIdHour(final UUID playerId, LocalDateTime hour) throws FatalException, TryAgainException {
		final Timestamp hour_utc = Timestamp.valueOf(hour);
		return this.jDapper.readTheOnlyOneWhere("player_id=? AND hour_utc=?", playerId.toString(), hour_utc);
	}

	public TimeSpanRow sumPlayer(UUID playerId, LocalDateTime fromTime, LocalDateTime toTime) throws FatalException, TryAgainException {
		String sql = "SELECT" +
				" SUM(play_time_in_seconds) AS play_time_in_seconds" + 
				", SUM(chat_messages) AS chat_messages " +
				", SUM(blocks_created) AS blocks_created " +
				", SUM(blocks_broken) AS blocks_broken " +
				" FROM timespan WHERE player_id=? AND hour_utc>=? AND hour_utc<=?'";
		return this.jDapper.readTheOnlyOne(sql, playerId, fromTime, toTime);
	}
}
