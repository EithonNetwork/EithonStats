package net.eithon.plugin.stats.db;

import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.JDapper;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;

public class AccumulatedController {
	private JDapper<AccumulatedPojo> jDapper;

	public AccumulatedController(final Database database) throws FatalException {
		this.jDapper = new JDapper<AccumulatedPojo>(AccumulatedPojo.class, database);
	}
	
	public AccumulatedPojo insert(final UUID playerId) throws FatalException, TryAgainException {
		String sql = String.format("INSERT INTO accumulated (player_id) VALUES (?)");
		int id = this.jDapper.insert(sql, playerId);
		return this.jDapper.readTheOnlyOne("SELECT FROM accumulated WHERE id = ?", id);
	}

	public AccumulatedPojo getByPlayerId(final UUID playerId) throws FatalException, TryAgainException {
		String sql = String.format("SELECT * FROM accumulated WHERE player_id=?");
		return this.jDapper.readTheOnlyOne(sql, playerId);
	}

	public AccumulatedPojo getByPlayerIdOrInsert(final UUID playerId) throws FatalException, TryAgainException {
		AccumulatedPojo accumulated = getByPlayerId(playerId);
		if (accumulated != null) return accumulated;
		return insert(playerId);
	}
	
	public void update(AccumulatedPojo data) throws FatalException, TryAgainException {
		this.jDapper.update("accumulated", data, "id = ?", data.id);
	}
}
