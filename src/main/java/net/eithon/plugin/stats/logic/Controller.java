package net.eithon.plugin.stats.logic;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.file.FileMisc;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.move.IBlockMoverFollower;
import net.eithon.library.move.MoveEventHandler;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.stats.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.io.Files;

public class Controller implements IBlockMoverFollower {

	private PlayerCollection<PlayerStatistics> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = null;
		consolidateDelta(null);
		this._eithonLogger = this._eithonPlugin.getEithonLogger();
		MoveEventHandler.addBlockMover(this);
	}

	private void consolidateDelta(File archiveFile) {
		if (this._allPlayerTimes != null) saveDelta();
		this._allPlayerTimes = new PlayerCollection<PlayerStatistics>(new PlayerStatistics(), this._eithonPlugin.getDataFile("playerTimeDeltas"));
		this._allPlayerTimes.consolidateDelta(this._eithonPlugin, "PlayerStatistics", 1, archiveFile);
	}

	public void saveDelta() {
		this._allPlayerTimes.saveDelta(this._eithonPlugin, "PlayerStatistics", 1);
	}

	public void startPlayer(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.start();
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Started player %s.", 
				player.getName(), this._allPlayerTimes.size());
	}

	public void stopPlayer(Player player, String description) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.stop(description);
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Stopped player %s.",
				player.getName());
		if (time.isAfk()) {
			Config.M.toAfkBroadcast.broadcastMessage(player.getName(), description);
		}	
	}

	private PlayerStatistics getOrCreatePlayerTime(Player player) {
		PlayerStatistics time = this._allPlayerTimes.get(player);
		if (time == null) {
			time = new PlayerStatistics(player);
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}

	public void showStats(CommandSender sender, Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.lap();
		sender.sendMessage(time.toString());
	}

	public void showTimeStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByTotalTime(ascending, maxItems)) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.timeStats()));			
		}
	}
	
	private PlayerStatistics[] sortPlayerTimesByTotalTime(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		List<PlayerStatistics> statistics = new ArrayList<PlayerStatistics>(this._allPlayerTimes.values());
		statistics.sort(new Comparator<PlayerStatistics>(){
			public int compare(PlayerStatistics f1, PlayerStatistics f2)
			{
				return factor*Long.valueOf(f1.getTotalTimeInSeconds()).compareTo(f2.getTotalTimeInSeconds());
			} });
		if (maxItems > 0) statistics = statistics.subList(0,  maxItems-1);
		return statistics.toArray(new PlayerStatistics[0]);
	}

	public void showBlocksStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByBlocksCreated(ascending, maxItems)) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.blockStats()));			
		}
	}
	
	private PlayerStatistics[] sortPlayerTimesByBlocksCreated(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		List<PlayerStatistics> statistics = new ArrayList<PlayerStatistics>(this._allPlayerTimes.values());
		statistics.sort(new Comparator<PlayerStatistics>(){
			public int compare(PlayerStatistics f1, PlayerStatistics f2)
			{
				return factor*Long.valueOf(f1.getBlocksCreated()).compareTo(f2.getBlocksCreated());
			} });
		if (maxItems > 0) statistics = statistics.subList(0,  maxItems-1);
		return statistics.toArray(new PlayerStatistics[0]);
	}

	public void showChatStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByChats(ascending, maxItems)) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.chatStats()));			
		}
	}
	
	private PlayerStatistics[] sortPlayerTimesByChats(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		List<PlayerStatistics> statistics = new ArrayList<PlayerStatistics>(this._allPlayerTimes.values());
		statistics.sort(new Comparator<PlayerStatistics>(){
			public int compare(PlayerStatistics f1, PlayerStatistics f2)
			{
				return factor*Long.valueOf(f1.getChats()).compareTo(f2.getChats());
			} });
		if (maxItems > 0) statistics = statistics.subList(0,  maxItems-1);
		return statistics.toArray(new PlayerStatistics[0]);
	}

	public void showAfkStatus(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByAfkTime(ascending, maxItems)) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.getAfkDescription()));
		}
	}
	
	private PlayerStatistics[] sortPlayerTimesByAfkTime(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		List<PlayerStatistics> afk = new LinkedList<PlayerStatistics>();
		for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
			if (playerStatistics.isAfk()) afk.add(playerStatistics);
		}
		afk.sort(new Comparator<PlayerStatistics>(){
			public int compare(PlayerStatistics f1, PlayerStatistics f2)
			{
				return factor*f1.getAfkTime().compareTo(f2.getAfkTime());
			} });
		if (maxItems > 0) afk = afk.subList(0,  maxItems-1);
		return afk.toArray(new PlayerStatistics[0]);
	}

	@Override
	public void moveEventHandler(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		playerMoved(event.getPlayer());
	}

	@Override
	public String getName() {
		return this._eithonPlugin.getName();
	}

	public void playerMoved(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s moved.", player.getName());
	}

	public void addChatActivity(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		if (time.isAfk()) Config.M.fromAfkBroadcast.broadcastMessage(player.getName());
		time.updateAlive();
		time.addChatActivity();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s chatted.", player.getName());
	}

	public void addBlocksCreated(Player player, long blocks) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		if (time.isAfk()) Config.M.fromAfkBroadcast.broadcastMessage(player.getName());
		time.updateAlive();
		time.addBlocksCreated(blocks);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s created a block.", player.getName());
	}

	public void addBlocksBroken(Player player, long blocks) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		if (time.isAfk()) Config.M.fromAfkBroadcast.broadcastMessage(player.getName());
		time.updateAlive();
		time.addBlocksDestroyed(blocks);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s broke a block.", player.getName());
	}

	public void archive() {
		File targetFile = getArchiveFileForDayFromNow(1);
		consolidateDelta(targetFile);
	}

	private File getArchiveFileForDayFromNow(int daysBack) {
		File targetFile = new File(
				this._eithonPlugin.getDataFile("playerTimeArchive"), 
				String.format("%s.json", LocalDate.now().minusDays(daysBack)));
		return targetFile;
	}
}
