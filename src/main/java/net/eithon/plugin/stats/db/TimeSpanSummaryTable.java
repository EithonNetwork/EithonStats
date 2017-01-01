package net.eithon.plugin.stats.db;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class TimeSpanSummaryTable extends DbTable<TimeSpanSummaryRow> {

	public TimeSpanSummaryTable(final Database database) throws FatalException {
		super(TimeSpanSummaryRow.class, database);
	}

	public TimeSpanSummaryRow sumPlayer(UUID playerId, LocalDateTime fromTime, LocalDateTime toTime) throws FatalException, TryAgainException {
		String sql = "SELECT" +
				" SUM(play_time_in_seconds) AS play_time_in_seconds" + 
				", SUM(chat_messages) AS chat_messages " +
				", SUM(blocks_created) AS blocks_created " +
				", SUM(blocks_broken) AS blocks_broken " +
				" FROM timespan WHERE player_id=? AND hour_utc>=? AND hour_utc<=?";
		return this.jDapper.readTheOnlyOne(sql, playerId, fromTime, toTime);
	}
}
