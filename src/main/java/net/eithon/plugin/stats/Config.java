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
		public static int inactivityMinutes;
		
		static void load(Configuration config) {
			inactivityMinutes = config.getInt("InactivityMinutes", 5);
		}

	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {

		static void load(Configuration config) {
		}		
	}

}
