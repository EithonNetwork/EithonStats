package net.eithon.plugin.stats.logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.mysql.Database;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;
import net.eithon.plugin.stats.db.TimeSpan;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class HourStatistics implements IUuidAndName {

	// Saved variables
	private UUID _playerId;
	private long _blocksBroken;
	private long _blocksCreated;
	private long _chatActivities;
	private long _playtimeInSeconds;
	private LocalDateTime _hour;

	// Non-saved variables
	private Database _database;

	public static HourStatistics save(Database database, HourStatistics earlier, PlayerStatistics now) throws SQLException, ClassNotFoundException {
		LocalDateTime laterHour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
		UUID playerId = earlier._playerId;
		LocalDateTime earlierHour = earlier._hour;
		long blocksBroken = now.getBlocksBroken()- earlier._blocksCreated;
		long blocksCreated = now.getBlocksCreated() - earlier._blocksCreated;
		long chatActivities = now.getChatMessages() - earlier._chatActivities;
		long playtimeInSeconds = now.getTotalTimeInSeconds() - earlier._playtimeInSeconds;
		TimeSpan timeSpan = TimeSpan.getByPlayerIdHour(database, earlier._playerId, earlierHour);
		if (timeSpan == null) {
			timeSpan = TimeSpan.create(database, playerId, earlierHour, playtimeInSeconds, 
					chatActivities, blocksCreated, blocksBroken);
		} else {
			timeSpan.update(playtimeInSeconds, chatActivities, blocksCreated, blocksBroken);
		}
		if (earlierHour.equals(laterHour)) return earlier;
		return new HourStatistics(database, now, laterHour);
	}

	public HourStatistics(Database database, EithonPlayer player, LocalDateTime fromTime, LocalDateTime toTime) throws SQLException, ClassNotFoundException
	{
		this._database = database;
		TimeSpan timeSpan = TimeSpan.sumPlayer(database, player.getUniqueId(), fromTime, toTime);
		fromDb(timeSpan);
		this._playerId = player.getUniqueId();
		this._hour = fromTime.truncatedTo(ChronoUnit.HOURS);
		return;
	}

	HourStatistics(Database database, PlayerStatistics playerStatistics, LocalDateTime time)
	{
		this._database = database;
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

	private void substractAnyExistingHourValues() throws ClassNotFoundException, SQLException {
		TimeSpan timeSpan = TimeSpan.getByPlayerIdHour(this._database, this._playerId, this._hour);
		if (timeSpan == null) return;
		this._blocksBroken -= timeSpan.get_blocksBroken();
		this._blocksCreated -= timeSpan.get_blocksCreated();
		this._chatActivities -= timeSpan.get_chatMessages();
		this._playtimeInSeconds -= timeSpan.get_playTimeInSeconds();
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

	private HourStatistics fromDb(TimeSpan timeSpan)  {
		this._playerId = timeSpan.get_playerId();
		this._chatActivities = timeSpan.get_chatMessages();
		this._blocksCreated = timeSpan.get_blocksCreated();
		this._blocksBroken = timeSpan.get_blocksBroken();
		this._playtimeInSeconds = timeSpan.get_playTimeInSeconds();
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
