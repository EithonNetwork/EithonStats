package net.eithon.plugin.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.eithon.library.mysql.MySql;

import org.junit.Assert;

public class TestSupport {
	public static Connection getConnectionAndTruncateTables() {
		MySql mySql = new MySql("mc.eithon.net", "3306", "DEV_eithon_stats", "DEV_eithon_stats", "WrSqjVMEpst9aqnm");
		try {
			Connection connection = mySql.openConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `accumulated`");
			statement.executeUpdate("DELETE FROM `timespan`");
			return connection;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
