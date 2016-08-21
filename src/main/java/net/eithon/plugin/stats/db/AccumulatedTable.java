package net.eithon.plugin.stats.db;

import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.DbTable;

public class AccumulatedTable extends DbTable<AccumulatedRow> {

	public AccumulatedTable(final Database database) throws FatalException {
		super(AccumulatedRow.class, database);
	}
	
	public AccumulatedRow create(final UUID playerId) throws FatalException, TryAgainException {
		AccumulatedRow accumulated = new AccumulatedRow();
		accumulated.player_id = playerId.toString();
		long id = this.jDapper.createOne(accumulated);
		return get(id);
	}

	public AccumulatedRow getByPlayerId(final UUID playerId) throws FatalException, TryAgainException {
		return this.jDapper.readTheOnlyOneWhere("player_id=?", playerId.toString());
	}

	public AccumulatedRow readOrCreate(final UUID playerId) throws FatalException, TryAgainException {
		AccumulatedRow accumulated = getByPlayerId(playerId);
		if (accumulated != null) return accumulated;
		return create(playerId);
	}
}
