package net.eithon.plugin.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;

import org.junit.Assert;

public class TestSupport {
	public static Database getDatabaseAndTruncateTables() {
		MySql mySql = new MySql("mc.eithon.net", "3306", "DEV_eithon_stats", "DEV_eithon_stats", "WrSqjVMEpst9aqnm");
		try {
			Connection connection = mySql.getOrOpenConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate("DELETE FROM `accumulated`");
			statement.executeUpdate("DELETE FROM `timespan`");
			return mySql;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}
}
