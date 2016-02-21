package net.eithon.plugin.test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.mysql.Database;
import net.eithon.plugin.stats.db.TimeSpan;

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
		TimeSpan row = createRow(database, playerId);
		Assert.assertNotNull(row);
		TimeSpan created = null;
		try {
			created = TimeSpan.getByPlayerIdHour(database, row.get_playerId(), row.get_hour());
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

	private TimeSpan createRow(Database database, UUID playerId) {
		TimeSpan row = null;
		long counter = 1;
		LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

		long totalPlayTimeInSeconds = counter++;
		long chatActivities = counter++;
		long blocksCreated = counter++;
		long blocksBroken = counter++;
		try {
			row = TimeSpan.create(database, playerId, hour, 
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
