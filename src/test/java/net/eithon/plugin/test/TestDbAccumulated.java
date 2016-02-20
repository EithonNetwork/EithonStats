package net.eithon.plugin.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.plugin.stats.db.Accumulated;

import org.junit.Assert;
import org.junit.Test;


public class TestDbAccumulated {
	
	
	@Test
	public void testGetConnection() {
		Connection connection = TestSupport.getConnectionAndTruncateTables();
		Assert.assertNotNull(connection);
	}

	@Test
	public void testCreate() {
		Connection connection = TestSupport.getConnectionAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		Accumulated row = createRow(connection, playerId);
		Assert.assertNotNull(row);
	}

	private Accumulated createRow(Connection connection, UUID playerId) {
		Accumulated row = null;
		try {
			row = Accumulated.create(connection, playerId);
		} catch (SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerId, row.get_playerId());
		return row;
	}

	@Test
	public void testUpdate() {
		Connection connection = TestSupport.getConnectionAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		Accumulated row = createRow(connection, playerId);
		Assert.assertNotNull(row);
		try {
			update(connection, row);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
		Accumulated updated = null;
		try {
			updated = Accumulated.getByPlayerId(connection, row.get_playerId());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} 
		Assert.assertEquals(row.get_firstStartTime(), updated.get_firstStartTime());
		Assert.assertEquals(row.get_lastStopTime(), updated.get_lastStopTime());
		Assert.assertEquals(row.get_totalPlayTimeInSeconds(), updated.get_totalPlayTimeInSeconds());
		Assert.assertEquals(row.get_joins(), updated.get_joins());
		Assert.assertEquals(row.get_longestIntervalInSeconds(), updated.get_longestIntervalInSeconds());
		Assert.assertEquals(row.get_playTimeTodayInSeconds(), updated.get_playTimeTodayInSeconds());
		Assert.assertEquals(row.get_today().truncatedTo(ChronoUnit.DAYS), updated.get_today());
		Assert.assertEquals(row.get_chatActivities(), updated.get_chatActivities());
		Assert.assertEquals(row.get_lastChatActivity(), updated.get_lastChatActivity());
		Assert.assertEquals(row.get_blocksCreated(), updated.get_blocksCreated());
		Assert.assertEquals(row.get_blocksBroken(), updated.get_blocksBroken());
		Assert.assertEquals(row.get_consecutiveDays(), updated.get_consecutiveDays());
		Assert.assertEquals(row.get_lastConsecutiveDay().truncatedTo(ChronoUnit.DAYS), updated.get_lastConsecutiveDay());
	}

	private void update(Connection connection, Accumulated row)
			throws SQLException {
		String playerName;
		playerName = "new name";
		LocalDateTime timeCounter = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		LocalDateTime firstStartTime = timeCounter;
		timeCounter = timeCounter.plusDays(1);
		LocalDateTime lastStopTime = timeCounter;
		timeCounter = timeCounter.plusDays(1);
		long counter = 1;
		long totalPlayTimeInSeconds = counter++;
		long intervals = counter++;
		long longestIntervalInSeconds = counter++;
		long playTimeTodayInSeconds = counter++;
		LocalDateTime today = timeCounter;
		timeCounter = timeCounter.plusDays(1);
		long chatActivities = counter++;
		LocalDateTime lastChatActivity = timeCounter;
		timeCounter = timeCounter.plusDays(1);
		long blocksCreated = counter++;
		long blocksBroken = counter++;
		long consecutiveDays = counter++;
		LocalDateTime lastConsecutiveDay = timeCounter;
		timeCounter = timeCounter.plusDays(1);
		row.update(playerName, firstStartTime , lastStopTime , totalPlayTimeInSeconds , intervals , longestIntervalInSeconds,
				playTimeTodayInSeconds = counter++, today, chatActivities, lastChatActivity, blocksCreated, blocksBroken, 
				consecutiveDays, lastConsecutiveDay);
	}

}
