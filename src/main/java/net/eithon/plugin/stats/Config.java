package net.eithon.plugin.stats;

import java.time.LocalTime;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static long allowedInactivityInSeconds;
		public static long secondsBetweenSave;
		public static LocalTime archiveAtTimeOfDay;
		public static long secondsPerDayForConsecutiveDays;
		public static String databaseHostname;
		public static String databasePort;
		public static String databaseName;
		public static String databaseUsername;
		public static String databasePassword;
		
		static void load(Configuration config) {
			allowedInactivityInSeconds = config.getSeconds("AllowedInactivityTimeSpan", "5m");
			secondsBetweenSave = config.getSeconds("TimeSpanBetweenSave", "5m");
			archiveAtTimeOfDay = config.getLocalTime("ArchiveAtTimeOfDay", LocalTime.of(0, 0));
			secondsPerDayForConsecutiveDays = config.getSeconds("TimeSpanPerDayForConsecutiveDays", "1h");
			databaseHostname = config.getString("database.Hostname", null);
			databasePort = config.getString("database.Port", null);
			databaseName = config.getString("database.Name", null);
			databaseUsername = config.getString("database.Username", null);
			databasePassword = config.getString("database.Password", null);
		}

	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {

		public static ConfigurableMessage playerStarted;
		public static ConfigurableMessage playerStopped;
		public static ConfigurableMessage playTimeAdded;
		public static ConfigurableMessage consecutiveDaysAdded;
		public static ConfigurableMessage placedBlocksAdded;
		public static ConfigurableMessage brokenBlocksAdded;
		public static ConfigurableMessage playTimeRemoved;
		public static ConfigurableMessage consecutiveDaysRemoved;
		public static ConfigurableMessage placedBlocksRemoved;
		public static ConfigurableMessage brokenBlocksRemoved;
		public static ConfigurableMessage playTimeReset;
		public static ConfigurableMessage saved;
		public static ConfigurableMessage statusOnline;
		public static ConfigurableMessage statusOffline;
		public static ConfigurableMessage statusAfk;
		public static ConfigurableMessage inactivityDetected;
		public static ConfigurableMessage defaultAfkDescription;
		public static ConfigurableMessage playerIdle;
		public static ConfigurableMessage toAfkBroadcast;
		public static ConfigurableMessage fromAfkBroadcast;
		public static ConfigurableMessage timeStats;
		public static ConfigurableMessage chatStats;
		public static ConfigurableMessage blockStats;
		public static ConfigurableMessage playerStats;
		public static ConfigurableMessage diffStats;

		static void load(Configuration config) {
			playerStarted = config.getConfigurableMessage(
					"messages.PlayerStarted", 1, 
					"Started recording play time for player %s.");
			playerStopped = config.getConfigurableMessage(
					"messages.PlayerStopped", 1, 
					"Stopped recording play time for player %s.");
			playTimeAdded = config.getConfigurableMessage(
					"messages.PlayTimeAdded", 3, 
					"Added %s play time to player %s, resulting in a total play time of %s.");
			consecutiveDaysAdded = config.getConfigurableMessage(
					"messages.ConsecutiveDaysAdded", 3, 
					"Added %d consecutive days to player %s, resulting in a total of %d consecutive days.");
			placedBlocksAdded = config.getConfigurableMessage(
					"messages.PlacedBlocksAdded", 3, 
					"Added %d placed blocks to player %s, resulting in a total of %d placed blocks.");
			brokenBlocksAdded = config.getConfigurableMessage(
					"messages.BrokenBlocksAdded", 3, 
					"Added %d broken blocks to player %s, resulting in a total of %d broken blocks.");
			playTimeRemoved = config.getConfigurableMessage(
					"messages.PlayTimeRemoved", 3, 
					"Removed %s play time from player %s, resulting in a total play time of %s.");
			consecutiveDaysRemoved = config.getConfigurableMessage(
					"messages.ConsecutiveDaysRemoved", 3, 
					"Removed %d consecutive days from player %s, resulting in a total of %d consecutive days.");
			placedBlocksRemoved = config.getConfigurableMessage(
					"messages.PlacedBlocksRemoved", 3, 
					"Removed %d placed blocks from player %s, resulting in a total of %d placed blocks.");
			brokenBlocksRemoved = config.getConfigurableMessage(
					"messages.BrokenBlocksRemoved", 3, 
					"Removed %d broken blocks from player %s, resulting in a total of %d broken blocks.");
			playTimeReset = config.getConfigurableMessage(
					"messages.PlayTimeReset", 1, 
					"Took away all play time from player %s.");
			saved = config.getConfigurableMessage(
					"messages.Saved", 0, 
					"Saved play times for all logged in players.");
			statusOnline = config.getConfigurableMessage(
					"messages.StatusOnline", 0, 
					"Online");
			statusOffline = config.getConfigurableMessage(
					"messages.StatusOffline", 0, 
					"Offline");
			statusAfk = config.getConfigurableMessage(
					"messages.StatusAfk", 1, 
					"AFK (%s)");
			inactivityDetected = config.getConfigurableMessage(
					"messages.InactivityDetected", 0, 
					"Inactivity detected");
			defaultAfkDescription = config.getConfigurableMessage(
					"messages.DefaultAfkDescription", 0, 
					"BRB");
			playerIdle = config.getConfigurableMessage(
					"messages.PlayerIdle", 0, 
					"Idle");
			toAfkBroadcast = config.getConfigurableMessage(
					"messages.ToAfkBroadcast", 2, 
					"%s AFK: %s");
			fromAfkBroadcast = config.getConfigurableMessage(
					"messages.FromAfkBroadcast", 1, 
					"%s is back");
			timeStats = config.getConfigurableMessage(
					"messages.TimeStats", 0, 
					"%TOTAL_PLAY_TIME% in %INTERVALS% intervals (longest %LONGEST_INTERVAL%, latest %LATEST_INTERVAL%) AFK: %AFK_DESCRIPTION%");
			chatStats = config.getConfigurableMessage(
					"messages.ChatStats", 0, 
					"%CHAT_ACTIVITIES% chats");
			blockStats = config.getConfigurableMessage(
					"messages.BlockStats", 0, 
					"%BLOCKS_CREATED% blocks created (broken %BLOCKS_BROKEN%)");
			playerStats = config.getConfigurableMessage(
					"messages.PlayerStats", 0, 
					"%PLAYER_NAME%: Playtime %TOTAL_PLAY_TIME%, chats %CHAT_ACTIVITIES%, blocks %BLOCKS_CREATED_OR_BROKEN% AFK: %AFK_DESCRIPTION%");
			diffStats = config.getConfigurableMessage(
					"messages.DiffStats", 0, 
					"%TOTAL_PLAY_TIME% in %INTERVALS% intervals, %CHAT_ACTIVITIES% chats, %BLOCKS_CREATED_OR_BROKEN% blocks created or broken)");
		}		
	}

}
