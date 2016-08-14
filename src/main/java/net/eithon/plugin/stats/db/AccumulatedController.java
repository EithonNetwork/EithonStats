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
	
	public AccumulatedPojo create(final UUID playerId) throws FatalException, TryAgainException {
		AccumulatedPojo accumulated = new AccumulatedPojo();
		accumulated.player_id = playerId.toString();
		long id = this.jDapper.createOne(accumulated);
		return get(id);
	}

	public AccumulatedPojo get(final long id) throws FatalException, TryAgainException {
		return this.jDapper.read(id);
	}

	public AccumulatedPojo getByPlayerId(final UUID playerId) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("player_id=?", playerId);
	}

	public AccumulatedPojo readOrCreate(final UUID playerId) throws FatalException, TryAgainException {
		AccumulatedPojo accumulated = getByPlayerId(playerId);
		if (accumulated != null) return accumulated;
		return create(playerId);
	}
	
	public void update(AccumulatedPojo data) throws FatalException, TryAgainException {
		this.jDapper.updateWhere(data, "id = ?", data.id);
	}
}
