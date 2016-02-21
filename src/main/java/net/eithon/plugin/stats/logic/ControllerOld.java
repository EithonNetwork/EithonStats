package net.eithon.plugin.stats.logic;

import java.io.File;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

import org.bukkit.entity.Player;

public class ControllerOld {
	private PlayerCollection<PlayerStatisticsOld> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;
	ControllerOld(EithonPlugin eithonPlugin, Controller newController){
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = null;
		this._eithonLogger = this._eithonPlugin.getEithonLogger();
		PlayerStatisticsOld.initialize(this._eithonLogger);
		saveDeltaAndConsolidate(null);
		transfer(newController);
	}
	
	private void transfer(Controller newController) {
		for (PlayerStatisticsOld oldStatistics : this._allPlayerTimes) {
			PlayerStatistics newStatistics = newController.getOrCreatePlayerTime(oldStatistics.getEithonPlayer().getOfflinePlayer());
			newStatistics.update(oldStatistics);
		}
	}

	private void saveDeltaAndConsolidate(File archiveFile) {
		if (this._allPlayerTimes == null)  {
			this._allPlayerTimes = new PlayerCollection<PlayerStatisticsOld>(new PlayerStatisticsOld(), this._eithonPlugin.getDataFile("playerTimeDeltas"));
		} else  {
			saveDelta();
		}
		consolidateDelta(archiveFile);
	}

	private void consolidateDelta(File archiveFile) {
		synchronized(this._allPlayerTimes) {
			this._allPlayerTimes.consolidateDelta(this._eithonPlugin, "PlayerStatisticsOld", 1, archiveFile);
		}
	}

	private void saveDelta() {
			saveDeltaPrimary();
	}
	
	private void saveDeltaPrimary() {
		synchronized(this._allPlayerTimes) {
			this._allPlayerTimes.saveDelta(this._eithonPlugin, "PlayerStatisticsOld", 1);
		}
	}

	PlayerStatisticsOld getOrCreatePlayerTime(Player player) {
		PlayerStatisticsOld time = this._allPlayerTimes.get(player);
		if (time == null) {
			this._eithonLogger.debug(DebugPrintLevel.MINOR, "New player statistics for player %s.",
					player.getName());
			time = new PlayerStatisticsOld(player);
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}
}
