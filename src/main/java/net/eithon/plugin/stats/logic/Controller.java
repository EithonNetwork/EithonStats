package net.eithon.plugin.stats.logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import net.eithon.library.core.PlayerCollection;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.mysql.Database;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.plugin.cop.EithonCopApi;
import net.eithon.plugin.stats.Config;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Controller {
	public static final String EITHON_STATS_BUNGEE_TRANSFER = "EithonStatsPlayerStatistics";
	private PlayerCollection<PlayerStatistics> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Plugin _eithonCopPlugin;
	
	public Controller(EithonPlugin eithonPlugin) throws FatalException{
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = new PlayerCollection<PlayerStatistics>();
		Database database = new Database(Config.V.databaseUrl, Config.V.databaseUsername, Config.V.databasePassword);
		connectToEithonCop(this._eithonPlugin);
		PlayerStatistics.initialize(this._eithonPlugin, database);
	}

	private void connectToEithonCop(EithonPlugin eithonPlugin) {
		this._eithonCopPlugin = PluginMisc.getPlugin("EithonCop");
		if (this._eithonCopPlugin != null && this._eithonCopPlugin.isEnabled()) {
			eithonPlugin.logInfo("Succesfully hooked into the EithonCop plugin!");
		} else {
			this._eithonCopPlugin = null;
			eithonPlugin.logWarn("EithonStats can't censor AFK messages without the EithonCop plugin.");			
		}
	}

	public void playerMoved(final Player player) throws FatalException, TryAgainException {
		getOrCreatePlayerTime(player).updateAlive();
	}

	public void playerCommand(Player player) throws FatalException, TryAgainException {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		verbose("playerCommand", "Player %s invoked estats command.", player.getName());
	}

	public void startPlayer(Player player) throws FatalException, TryAgainException {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.start();
		this._eithonPlugin.dbgMinor("Started player %s.", 
				player.getName(), this._allPlayerTimes.size());
	}

	public void stopPlayer(CommandSender sender, Player player, String description) throws FatalException, TryAgainException {
		PlayerStatistics time = getStatisticsOrInformSender(sender, player);
		if ((this._eithonCopPlugin != null && (description != null))) {
			description = EithonCopApi.censorMessage(player, description);
		}
		time.stop(description);
		time.save(false);
		this._eithonPlugin.dbgMinor("Stopped player %s.",
				player.getName());
	}

	public void removePlayer(CommandSender sender, Player player) {
		try {
			stopPlayer(sender, player, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this._allPlayerTimes.remove(player);
	}

	public boolean showStats(CommandSender sender, EithonPlayer eithonPlayer) throws FatalException, TryAgainException {
		PlayerStatistics time = getStatisticsOrInformSender(sender, eithonPlayer.getOfflinePlayer());
		if (time == null) return false;
		upateAliveIfSenderIsPlayer(sender, eithonPlayer, time);
		time.lap();
		time.sendPlayerStatistics(sender);
		return true;
	}

	private void upateAliveIfSenderIsPlayer(CommandSender sender,
			EithonPlayer eithonPlayer, PlayerStatistics time) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (player.getUniqueId() == eithonPlayer.getUniqueId()) {
				time.updateAlive();
			}
		}
	}

	public PlayerStatistics getPlayerStatistics(Player player) {
		return this._allPlayerTimes.get(player);
	}

	private PlayerStatistics getStatisticsOrInformSender(CommandSender sender, OfflinePlayer player) throws FatalException, TryAgainException {
		PlayerStatistics time = this._allPlayerTimes.get(player);
		if (time != null) return time;

		time = PlayerStatistics.get(player);
		if (time != null) return time;
		if (sender != null) {
			sender.sendMessage(String.format("No stats recorded for player %s", player.getName()));
		}
		return null;
	}

	PlayerStatistics getOrCreatePlayerTime(OfflinePlayer player) throws FatalException, TryAgainException {
		PlayerStatistics time = this._allPlayerTimes.get(player);
		if (time != null) return time;

		time = PlayerStatistics.getOrCreate(player);
		if (time == null) return null;
		this._eithonPlugin.dbgMinor("New player statistics for player %s.",
				player.getName());
		this._allPlayerTimes.put(player, time);
		return time;
	}

	private PlayerStatistics getOrCreatePlayerTime(EithonPlayer eithonPlayer) throws FatalException, TryAgainException {
		return getOrCreatePlayerTime(eithonPlayer.getOfflinePlayer());
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
			diff = new HourStatistics(player, then, now);
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
				HourStatistics diff = new HourStatistics(player, then, now);
				hourStatistics.put(player, diff);
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}
		for (HourStatistics diff : sortDiffsByTotalTime(hourStatistics, ascending, maxItems)) {
			if (diff == null) this._eithonPlugin.logError("showDiffStats: Unexpected null");
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

	public void addChatActivity(Player player) throws FatalException, TryAgainException {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addChatActivity();
		verbose("addChatActivity", "Player %s chatted.", player.getName());
	}

	public void addBlocksCreated(Player player, long blocks) throws FatalException, TryAgainException {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addBlocksCreated(blocks);
		verbose("addBlocksCreated", "Player %s created a block.", player.getName());
	}

	public void addBlocksBroken(Player player, long blocks) throws FatalException, TryAgainException {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		time.addBlocksBroken(blocks);
		verbose("addBlocksBroken", "Player %s broke a block.", player.getName());
	}

	public Long addPlayTime(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long playTimeInSeconds) throws FatalException, TryAgainException {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToTotalPlayTime(playTimeInSeconds);
	}

	public long addConsecutiveDays(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long consecutiveDays) throws FatalException, TryAgainException {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToConsecutiveDays(consecutiveDays);
	}

	public long addPlacedBlocks(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long blocksCreated) throws FatalException, TryAgainException {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToBlocksCreated(blocksCreated);
	}

	public long addBrokenBlocks(
			CommandSender sender, 
			EithonPlayer eithonPlayer,
			long blocksBroken) throws FatalException, TryAgainException {
		PlayerStatistics statistics = getOrCreatePlayerTime(eithonPlayer);
		return statistics.addToBlocksBroken(blocksBroken);
	}

	public boolean resetPlayTime(
			CommandSender sender, 
			EithonPlayer eithonPlayer) throws FatalException, TryAgainException {
		PlayerStatistics statistics = getStatisticsOrInformSender(sender, eithonPlayer.getOfflinePlayer());
		if (statistics == null) return false;
		statistics.resetTotalPlayTime();
		return true;
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

	public void save() throws FatalException, TryAgainException {
		for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
			playerStatistics.save(true);
		}
	}

	public void timespanSave() throws FatalException, TryAgainException {
		for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
			playerStatistics.saveTimeSpan();
		}
	}
	
	private void verbose(String method, String format, Object... args)
	{
		this._eithonPlugin.dbgVerbose("Controller", method, format, args);
	}
}
