package net.eithon.plugin.test;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.mysql.Database;
import net.eithon.plugin.stats.db.AccumulatedController;
import net.eithon.plugin.stats.db.AccumulatedPojo;

import org.junit.Assert;
import org.junit.Test;


public class TestDbAccumulated {
	
	
	@Test
	public void testGetConnection() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		Assert.assertNotNull(database);
	}

	@Test
	public void testCreate() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		AccumulatedPojo row = createRow(database, playerId);
		Assert.assertNotNull(row);
	}

	private AccumulatedPojo createRow(Database database, UUID playerId) {
		AccumulatedController controller = getController(database);
		AccumulatedPojo row = null;
		try {
			row = controller.create(playerId);
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(row);
		Assert.assertEquals(playerId, UUID.fromString(row.player_id));
		return row;
	}

	private AccumulatedController getController(Database database) {
		AccumulatedController controller = null;
		try {
			controller = new AccumulatedController(database);
		} catch (FatalException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(controller);
		return controller;
	}

	@Test
	public void testUpdate() {
		Database database = TestSupport.getDatabaseAndTruncateTables();
		UUID playerId = UUID.randomUUID();
		AccumulatedController controller = getController(database);
		AccumulatedPojo row = createRow(database, playerId);
		Assert.assertNotNull(row);
		AccumulatedPojo updated = updateAndRead(row.id, controller);
	}

	private AccumulatedPojo updateAndRead(long id, AccumulatedController controller) {
		AccumulatedPojo newValues = new AccumulatedPojo();
		newValues.id = id;
		LocalDateTime timeCounter = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
		newValues.player_id = UUID.randomUUID().toString();
		newValues.first_start_utc = Timestamp.valueOf(timeCounter);
		timeCounter = timeCounter.plusDays(1);
		newValues.last_stop_utc = Timestamp.valueOf(timeCounter);
		timeCounter = timeCounter.plusDays(1);
		long counter = 1;
		newValues.play_time_in_seconds = counter++;
		newValues.joins = counter++;
		newValues.longest_interval_in_seconds = counter++;
		newValues.today = Date.valueOf(timeCounter.toLocalDate());
		timeCounter = timeCounter.plusDays(1);
		newValues.chat_messages = counter++;
		newValues.last_chat_message_utc = Timestamp.valueOf(timeCounter);
		timeCounter = timeCounter.plusDays(1);
		newValues.blocks_broken = BigInteger.valueOf(counter++);
		newValues.blocks_created = BigInteger.valueOf(counter++);
		newValues.consecutive_days = counter++;
		newValues.last_consecutive_day = Date.valueOf(timeCounter.toLocalDate());
		timeCounter = timeCounter.plusDays(1);
		
		AccumulatedPojo updated = null;
		try {
			controller.update(newValues);
			updated = controller.get(id);
		} catch (FatalException | TryAgainException e) {
			e.printStackTrace();
			Assert.fail();
			return null;
		}
		
		Assert.assertNotNull(updated);
		Assert.assertEquals(newValues.first_start_utc, updated.first_start_utc);
		Assert.assertEquals(newValues.last_stop_utc, updated.last_stop_utc);
		Assert.assertEquals(newValues.play_time_in_seconds, updated.play_time_in_seconds);
		Assert.assertEquals(newValues.joins, updated.joins);
		Assert.assertEquals(newValues.longest_interval_in_seconds, updated.longest_interval_in_seconds);
		Assert.assertEquals(newValues.play_time_today_in_seconds, updated.play_time_today_in_seconds);
		Assert.assertEquals(newValues.today, updated.today);
		Assert.assertEquals(newValues.chat_messages, updated.chat_messages);
		Assert.assertEquals(newValues.last_chat_message_utc, updated.last_chat_message_utc);
		Assert.assertEquals(newValues.blocks_broken, updated.blocks_broken);
		Assert.assertEquals(newValues.blocks_created, updated.blocks_created);
		Assert.assertEquals(newValues.consecutive_days, updated.consecutive_days);
		Assert.assertEquals(newValues.last_consecutive_day, updated.last_consecutive_day);
		
		return updated;
	}

}
