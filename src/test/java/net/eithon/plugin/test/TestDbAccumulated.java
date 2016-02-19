package net.eithon.plugin.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import net.eithon.library.mysql.MySql;
import net.eithon.plugin.stats.db.Accumulated;

import org.junit.Assert;
import org.junit.Test;


public class TestDbAccumulated {
	
	private Connection _connection;

	@Test
	public void getConnection() {
		MySql mySql = new MySql("mc.eithon.net", "3306", "DEV_eithon_stats", "DEV_eithon_stats", "WrSqjVMEpst9aqnm");
		try {
			this._connection = mySql.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(this._connection);
	}

	@Test
	public void create() {
		getConnection();
		String playerName = "test";
		Accumulated row = null;
		try {
			row = Accumulated.create(this._connection, UUID.randomUUID(), playerName);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerName, row.get_playerName());
	}

}
