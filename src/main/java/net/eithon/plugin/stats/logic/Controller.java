package net.eithon.plugin.stats.logic;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import net.eithon.library.bungee.EithonBungeeEvent;
import net.eithon.library.core.CoreMisc;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.plugin.cop.EithonCopApi;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class Controller {
	public static final String EITHON_STATS_BUNGEE_TRANSFER = "EithonStatsPlayerStatistics";
	private PlayerCollection<PlayerStatistics> _allPlayerTimes;
	private EithonPlugin _eithonPlugin;
	private Logger _eithonLogger;
	private Plugin _eithonCopPlugin;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._allPlayerTimes = null;
		this._eithonLogger = this._eithonPlugin.getEithonLogger();
		PlayerStatistics.initialize(this._eithonLogger);
		saveDeltaAndConsolidate(null);
		connectToStats(this._eithonPlugin);
	}

	private void connectToStats(EithonPlugin eithonPlugin) {
		this._eithonCopPlugin = PluginMisc.getPlugin("EithonStats");
		if (this._eithonCopPlugin != null && this._eithonCopPlugin.isEnabled()) {
			eithonPlugin.getEithonLogger().info("Succesfully hooked into the EithonCop plugin!");
		} else {
			this._eithonCopPlugin = null;
			eithonPlugin.getEithonLogger().warning("EithonStats can't censor AFK messages without the EithonCop plugin.");			
		}
	}

	public void playerMoved(final Player player) {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.runTaskAsynchronously(this._eithonPlugin, new Runnable() {
			public void run() {
				getOrCreatePlayerTime(player).updateAlive();
			}
		});
	}

	public void playerCommand(Player player) {
		PlayerStatistics time = getOrCreatePlayerTime(player);
		time.updateAlive();
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE, "Player %s invoked estats command.", player.getName());
	}

	private void saveDeltaAndConsolidate(File archiveFile) {
		if (this._allPlayerTimes == null)  {
			this._allPlayerTimes = new PlayerCollection<PlayerStatistics>(new PlayerStatistics(), this._eithonPlugin.getDataFile("playerTimeDeltas"));
		} else  {
			saveDelta();
		}
		consolidateDelta(archiveFile);
	}

	private void consolidateDelta(File archiveFile) {
		if (!this.isPrimaryBungeeServer()) return;
		synchronized(this._allPlayerTimes) {
			this._allPlayerTimes.consolidateDelta(this._eithonPlugin, "PlayerStatistics", 1, archiveFile);
		}
	}

	public void saveDelta() {
		if (this.isPrimaryBungeeServer()) {
			saveDeltaPrimary();
		} else {
			saveDeltaSlave();			
		}
	}
	
	private void saveDeltaPrimary() {
		synchronized(this._allPlayerTimes) {
			this._allPlayerTimes.saveDelta(this._eithonPlugin, "PlayerStatistics", 1);
		}
	}

	private void saveDeltaSlave() {
		synchronized(this._allPlayerTimes) {
			for (PlayerStatistics playerStatistics : this._allPlayerTimes) {
				if (playerStatistics.isActive()) {
					transferPlayerStatsToPrimaryServer(playerStatistics.getEithonPlayer(), false);
				}
			}
		}
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
			time = new PlayerStatistics(player);
			this._allPlayerTimes.put(player, time);
		}
		return time;
	}

	private PlayerStatistics getOrCreatePlayerTime(EithonPlayer eithonPlayer) {
		PlayerStatistics time = this._allPlayerTimes.get(eithonPlayer);
		if (time == null) {
			this._eithonLogger.debug(DebugPrintLevel.MINOR, "New player statistics for player %s.",
					eithonPlayer.getName());
			time = new PlayerStatistics(eithonPlayer);
			this._allPlayerTimes.put(eithonPlayer, time);
		}
		return time;
	}

	private void setPlayerStatistics(PlayerStatistics statistics) {
		this._allPlayerTimes.put(statistics.getEithonPlayer(), statistics);
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
						return factor*Long.valueOf(f1.getChats()).compareTo(f2.getChats());
					} });
	}

	public void showDiffStats(CommandSender sender, EithonPlayer player, int daysBack) {
		PlayerCollection<PlayerStatistics> diff = diffWithArchive(daysBack);
		PlayerStatistics statistics = diff.get(player);
		if (statistics == null) return;
		statistics.sendDiffStats(sender);
		return;
	}

	public void showDiffStats(CommandSender sender, int daysBack, boolean ascending, int maxItems) {
		for (PlayerStatistics time : sortDiffsByTotalTime(daysBack, ascending, maxItems)) {
			if (time == null) this._eithonLogger.error("showDiffStats: Unexpected null");
			time.sendDiffStats(sender);			
		}
	}

	private List<PlayerStatistics> sortDiffsByTotalTime(int daysBack, boolean ascending, int maxItems) {
		int factor = ascending ? 1 : -1;
		PlayerCollection<PlayerStatistics> diff = diffWithArchive(daysBack);
		return diff.sort(
				maxItems,
				new Comparator<PlayerStatistics>(){
					public int compare(PlayerStatistics f1, PlayerStatistics f2)
					{
						return factor*Long.valueOf(f1.getTotalTimeInSeconds()).compareTo(f2.getTotalTimeInSeconds());
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

	public void archive() {
		File targetFile = getArchiveFileForDayFromNow(1);
		saveDeltaAndConsolidate(targetFile);
	}

	public PlayerCollection<PlayerStatistics> diffWithArchive(int daysBack) {
		PlayerCollection<PlayerStatistics> differences = new PlayerCollection<PlayerStatistics>(new PlayerStatistics());
		if (!isPrimaryBungeeServer()) return differences;
		PlayerCollection<PlayerStatistics> archive = getFromArchive(daysBack);		
		for (PlayerStatistics now : this._allPlayerTimes) {
			now.lap();
			PlayerStatistics then = (archive==null) ? null : archive.get(now.getUniqueId());
			PlayerStatistics diff = PlayerStatistics.getDifference(now, then);
			differences.put(now.getUniqueId(), diff);
		}
		return differences;
	}

	private PlayerCollection<PlayerStatistics> getFromArchive(int daysBack)
	{
		File archive = getArchiveFileForDayFromNow(daysBack);
		this._eithonLogger.debug(DebugPrintLevel.VERBOSE,
				"Will try to read from file \"%s\".", archive.getAbsolutePath());
		if (!archive.exists()) {
			this._eithonLogger.warning("Archive file \"%s\" not found.", archive.getAbsolutePath());
			return null;
		}

		FileContent fileContent = FileContent.loadFromFile(archive);
		return new PlayerCollection<PlayerStatistics>(new PlayerStatistics()).fromJson(fileContent.getPayload());
	}

	private File getArchiveFileForDayFromNow(int daysBack) {
		File targetFile = new File(
				this._eithonPlugin.getDataFile("playerTimeArchive"), 
				String.format("%s.json", LocalDate.now().minusDays(daysBack)));
		return targetFile;
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

	public boolean isPrimaryBungeeServer() {
		return this._eithonPlugin.getApi().isPrimaryBungeeServer();
	}

	public boolean isPrimaryBungeeServer(String serverName) {
		return this._eithonPlugin.getApi().isPrimaryBungeeServer(serverName);
	}

	public void transferPlayerStatsToSlaveServer(EithonPlayer player, String slaveServerName) {
		verbose("transferPlayerStatsToSlaveServer", "Enter; slaveServerName=%s, player=%s", 
				slaveServerName, player == null ? "NULL" : player.getName());
		transferPlayerStats(slaveServerName, player, true);
		verbose("transferPlayerStatsToSlaveServer", "Leave");
	}

	public void transferPlayerStatsToPrimaryServer(EithonPlayer player, boolean move) {
		verbose("transferPlayerStatsToPrimaryServer", "Enter; player=%s, move=%s", 
				player == null ? "NULL" : player.getName(), move ? "TRUE" : "FALSE");
		String primaryBungeeServerName = this._eithonPlugin.getApi().getPrimaryBungeeServerName();
		verbose("transferPlayerStatsToPrimaryServer", "primaryBungeeServerName=%s", primaryBungeeServerName);
		transferPlayerStats(primaryBungeeServerName, player, move);
		verbose("transferPlayerStatsToPrimaryServer", "Leave");
	}

	private void transferPlayerStats(String targetServerName, EithonPlayer player, boolean move) {
		verbose("transferPlayerStats", "Enter; targetServerName=%s, player=%s, move=%s", 
				targetServerName, player == null ? "NULL" : player.getName(), move ? "TRUE" : "FALSE");
		//PlayerStatistics statistics = getOrCreatePlayerTime(player);
		//BungeeTransfer info = new BungeeTransfer(statistics, true);
		//this._eithonPlugin.getApi().bungeeSendDataToServer(targetServerName, EITHON_STATS_BUNGEE_TRANSFER, info, true);
		verbose("transferPlayerStats", "Leave");
	}

	public void handleEithonBungeeEvent(EithonBungeeEvent event) {
		verbose("handleEithonBungeeEvent", "Enter; event.name=%s, event.data=%s",
				event.getName(), event.getData().toJSONString());
		if (!event.getName().equals(EITHON_STATS_BUNGEE_TRANSFER)) return;
		BungeeTransfer info = BungeeTransfer.getFromJson(event.getData());
		verbose("handleEithonBungeeEvent", "Received statistics for player %s", info.getStatistics().getName());
		PlayerStatistics statistics = info.getStatistics();
		setPlayerStatistics(statistics);
		if (info.getMove()) {
			verbose("handleEithonBungeeEvent", "Player %s statistics is started.", statistics.getName());
			statistics.start();
		}
		verbose("handleEithonBungeeEvent", "Leave");
	}

	private void verbose(String method, String format, Object... args) {
		String message = CoreMisc.safeFormat(format, args);
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "Controller.%s: %s", method, message);
	}
}
