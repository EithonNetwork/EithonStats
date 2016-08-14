package net.eithon.plugin.test;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		Database database = new Database("rookgaard.eithon.net", "3307", "DEV_e_stats", "DEV_e_plugin", "J5FE9EFCD1GX8tjg");
		try {
			database.executeUpdate("DELETE FROM `accumulated`");
			database.executeUpdate("DELETE FROM `timespan`");
			return database;
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
