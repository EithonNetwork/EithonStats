package net.eithon.plugin.test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.stats.db.TimeSpanTable;
import net.eithon.plugin.stats.db.TimeSpanRow;

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
		TimeSpanTable controller = getController(database);
		UUID playerId = UUID.randomUUID();
		TimeSpanRow row = createRow(controller, playerId);
		Assert.assertNotNull(row);
		TimeSpanRow created = null;
		try {
			created = controller.getByPlayerIdHour(playerId, row.hour_utc.toLocalDateTime());
		} catch (FatalException | TryAgainException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(created);
		Assert.assertEquals(row.play_time_in_seconds, created.play_time_in_seconds);
		Assert.assertEquals(row.chat_messages, created.chat_messages);
		Assert.assertEquals(row.blocks_broken, created.blocks_broken);
		Assert.assertEquals(row.blocks_created, created.blocks_created);
	}

	@Test
	public void testUpdate() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		TimeSpanTable controller = getController(database);
		UUID playerId = UUID.randomUUID();
		TimeSpanRow row = createRow(controller, playerId);
		Assert.assertNotNull(row);
		TimeSpanRow created = null;
		try {
			created = controller.getByPlayerIdHour(playerId, row.hour_utc.toLocalDateTime());
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		created.play_time_in_seconds+=11;
		created.chat_messages+=21;
		created.blocks_broken+=31;
		created.blocks_created+=41;
		try {
			controller.update(created);
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		TimeSpanRow updated = null;
		try {
			updated = controller.getByPlayerIdHour(playerId, row.hour_utc.toLocalDateTime());
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertEquals(updated.play_time_in_seconds, created.play_time_in_seconds);
		Assert.assertEquals(updated.chat_messages, created.chat_messages);
		Assert.assertEquals(updated.blocks_broken, created.blocks_broken);
		Assert.assertEquals(updated.blocks_created, created.blocks_created);
	}

	private TimeSpanTable getController(Database database) {
		TimeSpanTable controller = null;
		try {
			controller = new TimeSpanTable(database);
		} catch (FatalException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(controller);
		return controller;
	}

	private TimeSpanRow createRow(TimeSpanTable controller, UUID playerId) {
		TimeSpanRow row = null;
		long counter = 1;
		LocalDateTime hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

		long totalPlayTimeInSeconds = counter++;
		long chatActivities = counter++;
		long blocksCreated = counter++;
		long blocksBroken = counter++;
		try {
			row = controller.insert(playerId, hour, 
					totalPlayTimeInSeconds, 
					chatActivities, blocksCreated, blocksBroken);
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerId.toString(), row.player_id);
		return row;
	}

}
