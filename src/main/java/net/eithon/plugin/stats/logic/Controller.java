package net.eithon.plugin.stats.logic;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.move.IBlockMoverFollower;
import net.eithon.library.move.MoveEventHandler;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public class Controller implements IBlockMoverFollower {

	private PlayerCollection<PlayerTime> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = new PlayerCollection<PlayerTime>(new PlayerTime(), this._eithonPlugin.getDataFile("playerTimeDeltas"));
		this._allPlayerTimes.consolidateDelta(this._eithonPlugin);
		this._eithonLogger = this._eithonPlugin.getEithonLogger();
	}

	public void saveDelta() {
		this._allPlayerTimes.saveDelta(this._eithonPlugin);
		this._eithonLogger.debug(DebugPrintLevel.MAJOR, "Saved delta.");
	}

	public void startPlayer(Player player) {
		MoveEventHandler.addBlockMover(player, this);
		PlayerTime time = getOrCreatePlayerTime(player);
		time.start();
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Started player %s.", player.getName());
	}

	public void stopPlayer(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.stop();
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Stopped player %s.", player.getName());
	}

	private PlayerTime getOrCreatePlayerTime(Player player) {
		PlayerTime time = this._allPlayerTimes.get(player);
		if (time == null) {
			time = new PlayerTime(player);
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}

	public void showStats(CommandSender sender, Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		sender.sendMessage(time.toString());
	}

	@Override
	public void moveEventHandler(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		playerIsAlive(event.getPlayer());
	}

	@Override
	public String getName() {
		return this._eithonPlugin.getName();
	}

	public void playerIsAlive(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.updateAlive();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s is alive.", player.getName());
	}
}
