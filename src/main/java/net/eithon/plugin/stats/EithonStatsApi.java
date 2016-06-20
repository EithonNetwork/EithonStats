package net.eithon.plugin.stats;

import net.eithon.plugin.stats.logic.Controller;
import net.eithon.plugin.stats.logic.PlayerStatistics;

import org.bukkit.entity.Player;

public class EithonStatsApi {
	private static Controller controller;

	public void test() {}

	public static long getPlaytimeHours(Player player) {
		return getPlaytimeSeconds(player)/3600;
	}

	public static long getPlaytimeSeconds(Player player) {
		PlayerStatistics playerStatistics = getPlayerStatistics(player);
		if (playerStatistics == null) return 0;
		return playerStatistics.getTotalTimeInSeconds();
	}

	static void initialize(Controller _controller) {
		controller = _controller;
	}

	public static boolean isActive(Player player) {
		PlayerStatistics playerStatistics = getPlayerStatistics(player);
		if (playerStatistics == null) return false;
		return playerStatistics.isActive();
	}

	private static PlayerStatistics getPlayerStatistics(Player player) {
		if (controller == null) return null;
		return controller.getPlayerStatistics(player);
	}

	public static boolean isFirstIntervalToday(Player player) {
		PlayerStatistics playerStatistics = getPlayerStatistics(player);
		if (playerStatistics == null) return false;
		return playerStatistics.isFirstIntervalToday();
	}
	
	
}
