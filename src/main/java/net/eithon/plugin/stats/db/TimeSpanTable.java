package net.eithon.plugin.stats.db;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;
import net.eithon.library.mysql.EithonSqlConvert;

public class TimeSpanTable extends DbTable<TimeSpanRow> {

	public TimeSpanTable(final Database database) throws FatalException {
		super(TimeSpanRow.class, database);
	}

	public TimeSpanRow insert(final UUID playerId, LocalDateTime hour, long playtimeInSeconds, long chatActivities, long blocksCreated, long blocksBroken) throws FatalException, TryAgainException {
		TimeSpanRow timeSpan = new TimeSpanRow();
		timeSpan.player_id = playerId.toString();
		timeSpan.hour_utc = EithonSqlConvert.toSqlTimestamp(hour);
		long id = this.jDapper.createOne(timeSpan);
		return this.jDapper.read(id);
	}

	public TimeSpanRow getByPlayerIdHour(final UUID playerId, LocalDateTime hour) throws FatalException, TryAgainException {
		final Timestamp hour_utc = EithonSqlConvert.toSqlTimestamp(hour);
		return this.jDapper.readTheOnlyOneWhere("player_id=? AND hour_utc=?", playerId.toString(), hour_utc);
	}
}
