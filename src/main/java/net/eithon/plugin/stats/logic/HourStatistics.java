package net.eithon.plugin.stats.logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.exceptions.FatalException;
import net.eithon.library.exceptions.TryAgainException;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;
import net.eithon.plugin.stats.db.TimeSpanSummaryRow;
import net.eithon.plugin.stats.db.TimeSpanSummaryTable;
import net.eithon.plugin.stats.db.TimeSpanTable;
import net.eithon.plugin.stats.db.TimeSpanRow;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class HourStatistics implements IUuidAndName {

	private static TimeSpanTable timeSpanController;
	private static TimeSpanSummaryTable timeSpanSummaryController;
	// Saved variables
	private UUID _playerId;
	private long _blocksBroken;
	private long _blocksCreated;
	private long _chatActivities;
	private long _playtimeInSeconds;
	private LocalDateTime _hour;
	
	public static void initialize(Database database) throws FatalException {
		timeSpanController = new TimeSpanTable(database);
		timeSpanSummaryController = new TimeSpanSummaryTable(database);
	}

	public static HourStatistics save(HourStatistics earlier, PlayerStatistics now) throws FatalException, TryAgainException {
		LocalDateTime laterHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
		UUID playerId = earlier._playerId;
		LocalDateTime earlierHour = earlier._hour;
		long blocksBroken = now.getBlocksBroken()- earlier._blocksBroken;
		long blocksCreated = now.getBlocksCreated() - earlier._blocksCreated;
		long chatActivities = now.getChatMessages() - earlier._chatActivities;
		long playtimeInSeconds = now.getTotalTimeInSeconds() - earlier._playtimeInSeconds;
		TimeSpanRow timeSpan = timeSpanController.getByPlayerIdHour(earlier._playerId, earlierHour);
		if (timeSpan == null) {
			timeSpan = timeSpanController.insert(playerId, earlierHour, playtimeInSeconds, 
					chatActivities, blocksCreated, blocksBroken);
		} else {
			timeSpan.play_time_in_seconds = playtimeInSeconds;
			timeSpan.chat_messages = chatActivities;
			timeSpan.blocks_created = blocksCreated;
			timeSpan.blocks_broken = blocksBroken;
			timeSpanController.update(timeSpan);
		}
		if (earlierHour.equals(laterHour)) return earlier;
		return new HourStatistics(now, laterHour);
	}

	public HourStatistics(EithonPlayer player, LocalDateTime fromTime, LocalDateTime toTime) throws FatalException, TryAgainException
	{
		final UUID playerId = player.getUniqueId();
		TimeSpanSummaryRow timeSpan = timeSpanSummaryController.sumPlayer(playerId, fromTime, toTime);
		fromDb(playerId, timeSpan);
		this._playerId = playerId;
		this._hour = fromTime.truncatedTo(ChronoUnit.HOURS);
		return;
	}

	HourStatistics(PlayerStatistics playerStatistics, LocalDateTime time) throws FatalException, TryAgainException
	{
		useCurrentPlayerStatisticsAsBaseline(playerStatistics, time);
		try {
			substractAnyExistingHourValues();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	private void useCurrentPlayerStatisticsAsBaseline(
			PlayerStatistics playerStatistics, LocalDateTime time) {
		this._playerId = playerStatistics.getUniqueId();
		this._hour = time.truncatedTo(ChronoUnit.HOURS);
		this._blocksBroken = playerStatistics.getBlocksBroken();
		this._blocksCreated = playerStatistics.getBlocksCreated();
		this._chatActivities = playerStatistics.getChatMessages();
		this._playtimeInSeconds = playerStatistics.getTotalTimeInSeconds();
	}

	private void substractAnyExistingHourValues() throws ClassNotFoundException, SQLException, FatalException, TryAgainException {
		TimeSpanRow timeSpan = timeSpanController.getByPlayerIdHour(this._playerId, this._hour);
		if (timeSpan == null) return;
		this._blocksBroken -= timeSpan.blocks_broken;
		this._blocksCreated -= timeSpan.blocks_created;
		this._chatActivities -= timeSpan.chat_messages;
		this._playtimeInSeconds -= timeSpan.play_time_in_seconds;
	}

	public HourStatistics() {
		initialize();
	}

	private void initialize() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._playtimeInSeconds = 0;
	}

	private HourStatistics fromDb(TimeSpanRow timeSpan)  {
		UUID playerId = UUID.fromString(timeSpan.player_id);
		this._hour = TimeMisc.toLocalDateTime(timeSpan.hour_utc);
		fromDb(playerId, timeSpan);
		return this;
	}

	private HourStatistics fromDb(UUID playerId, TimeSpanSummaryRow timeSpan)  {
		this._playerId = playerId;
		this._chatActivities = timeSpan.chat_messages;
		this._blocksCreated = timeSpan.blocks_created;
		this._blocksBroken = timeSpan.blocks_broken;
		this._playtimeInSeconds = timeSpan.play_time_in_seconds;
		return this;
	}

	public String getName() {
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(this._playerId);
		if (player == null) return "Unknown";
		return player.getName();
	}

	public UUID getUniqueId() { 
		return this._playerId;
	}

	public void sendDiffStats(CommandSender sender) {
		Config.M.diffStats.sendMessage(sender, getNamedArguments());
	}

	private HashMap<String,String> getNamedArguments() {
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", this.getName());
		namedArguments.put("BLOCKS_BROKEN", String.format("%d", this._blocksBroken));
		namedArguments.put("BLOCKS_CREATED", String.format("%d", this._blocksCreated));
		namedArguments.put("BLOCKS_CREATED_OR_BROKEN", String.format("%d", this._blocksCreated + this._blocksBroken));
		namedArguments.put("CHAT_ACTIVITIES", String.format("%d", this._chatActivities));
		namedArguments.put("TOTAL_PLAY_TIME", TimeMisc.secondsToString(this._playtimeInSeconds));
		return namedArguments;
	}

	public long getPlayTimeInSeconds() { return this._playtimeInSeconds; }
}
