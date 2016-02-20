package net.eithon.plugin.stats.logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.mysql.MySql;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.plugin.cop.EithonCopApi;
import net.eithon.plugin.stats.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Controller {
	public static final String EITHON_STATS_BUNGEE_TRANSFER = "EithonStatsPlayerStatistics";
	private PlayerCollection<PlayerStatistics> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;
	private Plugin _eithonCopPlugin;
	private Database _database;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = new PlayerCollection<PlayerStatistics>();
		this._eithonLogger = this._eithonPlugin.getEithonLogger();
		this._database = new MySql(Config.V.databaseHostname, Config.V.databasePort, Config.V.databaseName,
				Config.V.databaseUsername, Config.V.databasePassword);
		PlayerStatistics.initialize(this._eithonLogger);
		connectToEithonCop(this._eithonPlugin);
	}

	private void connectToEithonCop(EithonPlugin eithonPlugin) {
		this._eithonCopPlugin = PluginMisc.getPlugin("EithonCop");
		if (this._eithonCopPlugin != null && this._eithonCopPlugin.isEnabled()) {
			eithonPlugin.getEithonLogger().info("Succesfully hooked into the EithonCop plugin!");
		} else {
			this._eithonCopPlugin = null;
			eithonPlugin.getEithonLogger().warning("EithonStats can't censor AFK messages without the EithonCop plugin.");			
		}
	}

	public void playerMoved(final Player player) {
		getOrCreatePlayerTime(player).updateAlive();
	}

	public void playerCommand(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s invoked estats command.", player.getName());
	}

	public void startPlayer(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.start();
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Started player %s.", 
				player.getName(), this._allPlayerTimes.size());
	}

	public void stopPlayer(Player player, String description) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		if ((this._eithonCopPlugin != null && (description != null))) {
			description = EithonCopApi.censorMessage(player, description);
		}
		time.stop(description);
		this._eithonLogger.debug(DebugPrintLevel.MINOR, "Stopped player %s.",
				player.getName());
	}
	
	public void removePlayer(Player player) {
		stopPlayer(player, null);
		this._allPlayerTimes.remove(player);
	}

	public PlayerStatistics getPlayerStatistics(Player player) {
		return this._allPlayerTimes.get(player);
	}

	public void showStats(CommandSender sender, EithonPlayer eithonPlayer) {
		PlayerStatistics time = getOrCreatePlayerTime(eithonPlayer);
		time.lap();
		time.sendPlayerStatistics(sender);
	}

	PlayerStatistics getOrCreatePlayerTime(Player player) {
		PlayerStatistics time = this._allPlayerTimes.get(player);
		if (time == null) {
			this._eithonLogger.debug(DebugPrintLevel.MINOR, "New player statistics for player %s.",
					player.getName());
			try {
				time = new PlayerStatistics(this._database, player);
			} catch (SQLException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}

	private PlayerStatistics getOrCreatePlayerTime(EithonPlayer eithonPlayer) {
		return getOrCreatePlayerTime(eithonPlayer.getPlayer());
	}

	public void showTimeStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByTotalTime(ascending, maxItems)) {
			time.lap();
			time.sendTimeStats(sender);	
		}
	}

	private List<PlayerStatistics> sortPlayerTimesByTotalTime(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		return this._allPlayerTimes.sort(
				maxItems,
				new Comparator<PlayerStatistics>(){
					public int compare(PlayerStatistics f1, PlayerStatistics f2)
					{
						return factor*Long.valueOf(f1.getTotalTimeInSeconds()).compareTo(f2.getTotalTimeInSeconds());
					} });
	}

	public void showBlocksStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByBlocksCreated(ascending, maxItems)) {
			time.lap();
			time.sendBlockStats(sender);	
		}
	}

	private List<PlayerStatistics> sortPlayerTimesByBlocksCreated(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		return this._allPlayerTimes.sort(
				maxItems,
				new Comparator<PlayerStatistics>(){
					public int compare(PlayerStatistics f1, PlayerStatistics f2)
					{
						return factor*Long.valueOf(f1.getBlocksCreated()).compareTo(f2.getBlocksCreated());
					} });
	}

	public void showChatStats(CommandSender sender, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortPlayerTimesByChats(ascending, maxItems)) {
			time.lap();
			time.sendChatStats(sender);		
		}
	}

	private List<PlayerStatistics> sortPlayerTimesByChats(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		return this._allPlayerTimes.sort(
				maxItems,
				new Comparator<PlayerStatistics>(){
					public int compare(PlayerStatistics f1, PlayerStatistics f2)
					{
						return factor*Long.valueOf(f1.getChatMessages()).compareTo(f2.getChatMessages());
					} });
	}

	public void showDiffStats(CommandSender sender, EithonPlayer player, int daysBack) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime then = now.minusDays(daysBack);
		HourStatistics diff;
		try {
			diff = new HourStatistics(this._database, player, then, now);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		diff.sendDiffStats(sender);
		return;
	}

	public void showDiffStats(CommandSender sender, int daysBack, boolean ascending, int maxItems) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime then = now.minusDays(daysBack);
		PlayerCollection<HourStatistics> hourStatistics = new PlayerCollection<HourStatistics>();
		for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
			try {
				EithonPlayer player = playerStatistics.getEithonPlayer();
				HourStatistics diff = new HourStatistics(this._database, player, then, now);
				hourStatistics.put(player, diff);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		for (HourStatistics diff : sortDiffsByTotalTime(hourStatistics, ascending, maxItems)) {
			if (diff == null) this._eithonLogger.error("showDiffStats: Unexpected null");
			diff.sendDiffStats(sender);			
		}
	}

	private List<HourStatistics> sortDiffsByTotalTime(PlayerCollection<HourStatistics> hourStatistics, boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		return hourStatistics.sort(
				maxItems,
				new Comparator<HourStatistics>(){
					public int compare(HourStatistics f1, HourStatistics f2)
					{
						return factor*Long.valueOf(f1.getPlayTimeInSeconds()).compareTo(f2.getPlayTimeInSeconds());
					} });
	}

	public void showAfkStatus(CommandSender sender, boolean ascending, int maxItems) {
		final List<PlayerStatistics> sortedStatistics = sortPlayerTimesByAfkTime(ascending, maxItems);
		if ((sortedStatistics == null) || (sortedStatistics.size() == 0)) {
			sender.sendMessage("There is currently nobody afk");
			return;
		}
		for (PlayerStatistics time : sortedStatistics) {
			time.lap();
			sender.sendMessage(String.format("%s: %s", time.getName(), time.getAfkDescription()));
		}
	}

	private List<PlayerStatistics> sortPlayerTimesByAfkTime(boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		return this._allPlayerTimes.sort(
				maxItems, 
				new Predicate<PlayerStatistics>() {
					public boolean test(PlayerStatistics t) {
						return !t.isAfk();
					}
				},
				new Comparator<PlayerStatistics>(){
					public int compare(PlayerStatistics f1, PlayerStatistics f2)
					{
						return factor*f1.getAfkTime().compareTo(f2.getAfkTime());
					} });
	}

	public void addChatActivity(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addChatActivity();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s chatted.", player.getName());
	}

	public void addBlocksCreated(Player player, long blocks) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addBlocksCreated(blocks);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s created a block.", player.getName());
	}

	public void addBlocksBroken(Player player, long blocks) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addBlocksBroken(blocks);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s broke a block.", player.getName());
	}
	
	public long addPlayTime(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long playTimeInSeconds) {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToTotalPlayTime(playTimeInSeconds);
	}

	public long addConsecutiveDays(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long consecutiveDays) {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToConsecutiveDays(consecutiveDays);
	}

	public long addPlacedBlocks(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long blocksCreated) {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToBlocksCreated(blocksCreated);
	}

	public long addBrokenBlocks(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long blocksBroken) {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToBlocksBroken(blocksBroken);
	}

	public void resetPlayTime(
			CommandSender sender, 
			EithonPlayer eithonPlayer) {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		statistics.resetTotalPlayTime();
	}

	public void who(CommandSender sender) {
		ArrayList<String> active = new ArrayList<String>();
		ArrayList<String> afk = new ArrayList<String>();
		for (PlayerStatistics statistics : this._allPlayerTimes) {
			if (!statistics.isOnline()) continue;
			if (statistics.isAfk()) afk.add(statistics.getName());
			else active.add(statistics.getName());
		}

		String activePlayers = String.join(", ", active);
		String afkPlayers = String.join(", ", afk);
		sender.sendMessage(String.format("Active: %s", activePlayers));
		sender.sendMessage(String.format("AFK: %s", afkPlayers));
	}

	public void save() {
		try {
			for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
				playerStatistics.save(true);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void timespanSave() {
		try {
			for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
				playerStatistics.timespanSave(this._database);
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
