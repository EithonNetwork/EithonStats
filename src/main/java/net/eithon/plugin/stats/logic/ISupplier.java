package net.eithon.plugin.stats.logic;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.PlayerException;
import net.eithon.library.exceptions.TryAgainException;

public interface ISupplier<T> {
	public T doIt() throws TryAgainException, FatalException, PlayerException;
}
