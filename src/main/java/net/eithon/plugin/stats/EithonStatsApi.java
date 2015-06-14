package net.eithon.plugin.stats;

import net.eithon.plugin.stats.logic.Controller;
import net.eithon.plugin.stats.logic.PlayerStatistics;

import org.bukkit.entity.Player;

public class EithonStatsApi {
	private static Controller controller;

	public void test() {}

	public static long getPlaytimeHours(Player player) {
		if (controller == null) return 0;
		PlayerStatistics playerStatistics = controller.getPlayerStatistics(player);
		if (playerStatistics == null) return 0;
		return playerStatistics.getTotalTimeInSeconds()/3600;
	}

	static void initialize(Controller _controller) {
		controller = _controller;
	}
}
