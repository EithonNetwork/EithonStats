package net.eithon.plugin.stats.db;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.JDapper;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;

public class TimeSpanController {
	private JDapper<TimeSpanPojo> jDapper;

	public TimeSpanController(final Database database) throws FatalException {
		this.jDapper = new JDapper<TimeSpanPojo>(TimeSpanPojo.class, database);
	}
	
	public TimeSpanPojo insert(final UUID playerId, LocalDateTime hour, long playtimeInSeconds, long chatActivities, long blocksCreated, long blocksBroken) throws FatalException, TryAgainException {
		String sql = String.format("INSERT INTO time_span (player_id, hour) VALUES (?, ?)");
		int id = this.jDapper.insert(sql, playerId, hour);
		return this.jDapper.readTheOnlyOne("SELECT FROM time_span WHERE id = ?", id);
	}

	public TimeSpanPojo getByPlayerIdHour(final UUID playerId, LocalDateTime hour) throws FatalException, TryAgainException {
		String sql = String.format("SELECT * FROM time_span WHERE player_id=? AND hour=?");
		return this.jDapper.readTheOnlyOne(sql, playerId, hour);
	}

	public TimeSpanPojo sumPlayer(UUID playerId, LocalDateTime fromTime, LocalDateTime toTime) {
		String sql = String.format("SELECT SUM() FROM time_span WHERE player_id=? AND hour=?");
		return this.jDapper.readTheOnlyOne(sql, playerId, hour);
	}
	
	public void update(TimeSpanPojo data) throws FatalException, TryAgainException {
		this.jDapper.update("time_span", data, "id = ?", data.id);
	}
}
