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
		public static int allowedInactivityInSeconds;
		public static int secondsBetweenSave;
		public static LocalTime archiveAtTimeOfDay;
		
		static void load(Configuration config) {
			allowedInactivityInSeconds = config.getInt("AllowedInactivityInSeconds", 300);
			secondsBetweenSave = config.getInt("SecondsBetweenSave", 300);
			archiveAtTimeOfDay = config.getLocalTime("ArchiveAtTimeOfDay", LocalTime.of(0, 0));
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
		public static ConfigurableMessage playTimeTaken;
		public static ConfigurableMessage playTimeReset;
		public static ConfigurableMessage saved;
		public static ConfigurableMessage afkYes;
		public static ConfigurableMessage afkNo;
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
			playTimeTaken = config.getConfigurableMessage(
					"messages.PlayTimeTaken", 3, 
					"Took %s play time from player %s, resulting in a total play time of %s.");
			playTimeReset = config.getConfigurableMessage(
					"messages.PlayTimeReset", 1, 
					"Took away all play time from player %s.");
			saved = config.getConfigurableMessage(
					"messages.Saved", 0, 
					"Saved play times for all logged in players.");
			afkYes = config.getConfigurableMessage(
					"messages.AfkYes", 1, 
					"AFK: Yes (%s)");
			afkNo = config.getConfigurableMessage(
					"messages.AfkNo", 0, 
					"AFK: No");
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
