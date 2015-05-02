package net.eithon.plugin.stats.logic;

import java.util.Arrays;
import java.util.Comparator;

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
		MoveEventHandler.addBlockMover(this);
	}

	public void saveDelta() {
		this._allPlayerTimes.saveDelta(this._eithonPlugin);
		this._eithonLogger.debug(DebugPrintLevel.MAJOR, "Saved delta.");
	}

	public void startPlayer(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.start();
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Started player %s (%d items).", 
				player.getName(), this._allPlayerTimes.size());
	}

	public void stopPlayer(Player player, String description) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.stop(description);
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Stopped player %s (%d items).",
				player.getName(), this._allPlayerTimes.size());
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
		time.lap();
		sender.sendMessage(time.toString());
	}

	public void showTimeStats(CommandSender sender, boolean ascending) {
		for (PlayerTime time : sortPlayerTimesByTotalTime(ascending)) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.timeStats()));			
		}
	}
	
	public PlayerTime[] sortPlayerTimesByTotalTime(boolean ascending) {
		int factor = ascending ? 1 : -1;
		PlayerTime[] playerTimes = this._allPlayerTimes.toArray(new PlayerTime[0]);
		Arrays.sort(playerTimes, new Comparator<PlayerTime>(){
			public int compare(PlayerTime f1, PlayerTime f2)
			{
				return factor*Long.valueOf(f1.getTotalTimeInSeconds()).compareTo(f2.getTotalTimeInSeconds());
			} });
		return playerTimes;
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

	public void addChatActivity(Player player) {
		PlayerTime time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addChatActivity();
	}
}
