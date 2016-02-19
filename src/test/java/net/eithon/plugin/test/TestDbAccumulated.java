package net.eithon.plugin.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.mysql.MySql;
import net.eithon.plugin.stats.db.Accumulated;

import org.junit.Assert;
import org.junit.Test;


public class TestDbAccumulated {
	
	@Test
	public void testGetConnection() {
		Connection connection = getConnection();
		Assert.assertNotNull(connection);
	}

	private Connection getConnection() {
		MySql mySql = new MySql("mc.eithon.net", "3306", "DEV_eithon_stats", "DEV_eithon_stats", "WrSqjVMEpst9aqnm");
		try {
			return mySql.openConnection();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		return null;
	}

	@Test
	public void testCreate() {
		Connection connection = getConnection();
		String playerName = "test";
		Accumulated row = createRow(connection, playerName);
		Assert.assertNotNull(row);
	}

	private Accumulated createRow(Connection connection, String playerName) {
		Accumulated row = null;
		try {
			row = Accumulated.create(connection, UUID.randomUUID(), playerName);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerName, row.get_playerName());
		return row;
	}

	@Test
	public void testUpdate() {
		Connection connection = getConnection();
		String playerName = "test";
		Accumulated row = createRow(connection, playerName);
		Assert.assertNotNull(row);
		try {
			update(connection, row);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
	}

	private void update(Connection connection, Accumulated row)
			throws SQLException {
		String playerName;
		playerName = "new name";
		LocalDateTime timeCounter = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		LocalDateTime firstStartTime = timeCounter.plusSeconds(60);
		LocalDateTime lastStopTime = timeCounter.plusSeconds(60);
		long counter = 1;
		long totalPlayTimeInSeconds = counter++;
		long intervals = counter++;
		long longestIntervalInSeconds = counter++;
		long playTimeTodayInSeconds = counter++;
		LocalDateTime today = timeCounter.plusSeconds(60);
		long chatActivities = counter++;
		LocalDateTime lastChatActivity = timeCounter.plusSeconds(60);
		long blocksCreated = counter++;
		long blocksBroken = counter++;
		long consecutiveDays = counter++;
		LocalDateTime lastConsecutiveDay = timeCounter.plusSeconds(60);
		row.update(playerName, firstStartTime , lastStopTime , totalPlayTimeInSeconds , intervals , longestIntervalInSeconds,
				playTimeTodayInSeconds = counter++, today, chatActivities, lastChatActivity, blocksCreated, blocksBroken, 
				consecutiveDays, lastConsecutiveDay);
		Accumulated updated = Accumulated.getByPlayerId(connection, row.get_playerId()); 
		Assert.assertEquals(playerName, updated.get_playerName());
		Assert.assertEquals(firstStartTime, updated.get_firstStartTime());
		Assert.assertEquals(lastStopTime, updated.get_lastStopTime());
		Assert.assertEquals(totalPlayTimeInSeconds, updated.get_totalPlayTimeInSeconds());
		Assert.assertEquals(intervals, updated.get_intervals());
		Assert.assertEquals(longestIntervalInSeconds, updated.get_longestIntervalInSeconds());
		Assert.assertEquals(playTimeTodayInSeconds, updated.get_playTimeTodayInSeconds());
		Assert.assertEquals(today, updated.get_today());
		Assert.assertEquals(chatActivities, updated.get_chatActivities());
		Assert.assertEquals(lastChatActivity, updated.get_lastChatActivity());
		Assert.assertEquals(blocksCreated, updated.get_blocksCreated());
		Assert.assertEquals(blocksBroken, updated.get_blocksBroken());
		Assert.assertEquals(consecutiveDays, updated.get_consecutiveDays());
		Assert.assertEquals(lastConsecutiveDay, updated.get_lastConsecutiveDay());
	}

}
