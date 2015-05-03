package net.eithon.plugin.stats;

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
		
		static void load(Configuration config) {
			allowedInactivityInSeconds = config.getInt("AllowedInactivityInSeconds", 300);
			secondsBetweenSave = config.getInt("SecondsBetweenSave", 300);
		}

	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {

		public static ConfigurableMessage playerStarted;
		public static ConfigurableMessage playerStopped;
		public static ConfigurableMessage saved;
		public static ConfigurableMessage inactivityDetected;
		public static ConfigurableMessage defaultAfkDescription;
		public static ConfigurableMessage playerAwayFromKeyboard;
		public static ConfigurableMessage toAfkBroadcast;
		public static ConfigurableMessage fromAfkBroadcast;

		static void load(Configuration config) {
			playerStarted = config.getConfigurableMessage(
					"messages.PlayerStarted", 1, 
					"Started recording play time for player %s.");
			playerStopped = config.getConfigurableMessage(
					"messages.PlayerStopped", 1, 
					"Stopped recording play time for player %s.");
			saved = config.getConfigurableMessage(
					"messages.Saved", 0, 
					"Saved play times for all logged in players.");
			inactivityDetected = config.getConfigurableMessage(
					"messages.InactivityDetected", 0, 
					"Inactivity detected");
			defaultAfkDescription = config.getConfigurableMessage(
					"messages.DefaultAfkDescription", 0, 
					"BRB");
			playerAwayFromKeyboard = config.getConfigurableMessage(
					"messages.PlayerAwayFromKeyboard", 1, 
					"AFK with description \"%s\".");
			toAfkBroadcast = config.getConfigurableMessage(
					"messages.ToAfkBroadcast", 2, 
					"%s AFK: %s");
			fromAfkBroadcast = config.getConfigurableMessage(
					"messages.FromAfkBroadcast", 1, 
					"%s is back");
		}		
	}

}
