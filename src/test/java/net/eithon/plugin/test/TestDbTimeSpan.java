package net.eithon.plugin.test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.db.Database;
import net.eithon.plugin.stats.db.TimeSpanPojo;

import org.junit.Assert;
import org.junit.Test;


public class TestDbTimeSpan {
	
	
	@Test
	public void testGetDatabase() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Assert.assertNotNull(database);
	}

	@Test
	public void testCreate() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		TimeSpanPojo row = createRow(database, playerId);
		Assert.assertNotNull(row);
		TimeSpanPojo created = null;
		try {
			created = TimeSpanPojo.getByPlayerIdHour(database, row.get_playerId(), row.get_hour());
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertEquals(row.get_playTimeInSeconds(), created.get_playTimeInSeconds());
		Assert.assertEquals(row.get_chatMessages(), created.get_chatMessages());
		Assert.assertEquals(row.get_blocksCreated(), created.get_blocksCreated());
		Assert.assertEquals(row.get_blocksBroken(), created.get_blocksBroken());
	}

	@Test
	public void testUpdate() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		TimeSpanPojo row = createRow(database, playerId);
		Assert.assertNotNull(row);
		TimeSpanPojo created = null;
		try {
			created = TimeSpanPojo.getByPlayerIdHour(database, row.get_playerId(), row.get_hour());
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
		long totalPlayTimeInSeconds = created.get_playTimeInSeconds()+11;
		long chatMessages = created.get_chatMessages()+21;
		long blocksCreated = created.get_blocksCreated()+31;
		long blocksBroken = created.get_blocksBroken()+41;
		try {
			created.update(
					totalPlayTimeInSeconds, 
					chatMessages,
					blocksCreated,
					blocksBroken);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			Assert.fail();
		}
		TimeSpanPojo updated = null;
		try {
			updated = TimeSpanPojo.getByPlayerIdHour(database, row.get_playerId(), row.get_hour());
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertEquals(totalPlayTimeInSeconds, updated.get_playTimeInSeconds());
		Assert.assertEquals(chatMessages, updated.get_chatMessages());
		Assert.assertEquals(blocksCreated, updated.get_blocksCreated());
		Assert.assertEquals(blocksBroken, updated.get_blocksBroken());
	}

	private TimeSpanPojo createRow(Database database, UUID playerId) {
		TimeSpanPojo row = null;
		long counter = 1;
		LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

		long totalPlayTimeInSeconds = counter++;
		long chatActivities = counter++;
		long blocksCreated = counter++;
		long blocksBroken = counter++;
		try {
			row = TimeSpanPojo.create(database, playerId, hour, 
					totalPlayTimeInSeconds, 
					chatActivities, blocksCreated, blocksBroken);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerId, row.get_playerId());
		return row;
	}

}
