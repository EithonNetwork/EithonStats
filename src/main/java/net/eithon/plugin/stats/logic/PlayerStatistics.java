package net.eithon.plugin.stats.logic;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.mysql.Database;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;
import net.eithon.plugin.stats.db.Accumulated;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class PlayerStatistics implements IUuidAndName {
	private static Logger eithonLogger;

	private Accumulated _dbRecord;

	// Saved variables
	private EithonPlayer _eithonPlayer;
	private long _blocksBroken;
	private long _blocksCreated;
	private long _chatMessages;
	private LocalDateTime _lastChatMessage;
	private long _consecutiveDays;
	private LocalDateTime _lastConsecutiveDay;
	private TimeStatistics _timeInfo;

	// Non-saved, internal variables
	private LocalDateTime _startTime;
	private LocalDateTime _lastAliveTime;
	private UUID _alarmId;
	private boolean _hasBeenUpdated;
	private String _afkDescription;
	private HourStatistics _lastHourValues;

	public static void initialize(Logger logger) {
		eithonLogger = logger;
	}

	public PlayerStatistics(Database _database, OfflinePlayer player) throws SQLException, ClassNotFoundException
	{
		this._dbRecord = Accumulated.getByPlayerId(_database, player.getUniqueId());
		if (this._dbRecord == null) {
			this._dbRecord = Accumulated.create(_database, player.getUniqueId());
			eithonLogger.debug(DebugPrintLevel.MAJOR, "Created player %s", getName());
			initialize();
		} else {
			eithonLogger.debug(DebugPrintLevel.MAJOR, "Loaded player %s", getName());
		}
		copyFromDbRecord();
		this._lastHourValues = new HourStatistics(this, LocalDateTime.now());
	}

	private void copyFromDbRecord() {
		this._eithonPlayer = new EithonPlayer(this._dbRecord.get_playerId());
		this._blocksBroken = this._dbRecord.get_blocksBroken();
		this._blocksCreated = this._dbRecord.get_blocksCreated();
		this._chatMessages = this._dbRecord.get_chatMessages();
		this._lastChatMessage = this._dbRecord.get_lastChatMessage();
		this._consecutiveDays = this._dbRecord.get_consecutiveDays();
		this._lastConsecutiveDay = this._dbRecord.get_lastConsecutiveDay();
		this._timeInfo = new TimeStatistics();
		this._timeInfo.copyFromDbRecord(this._dbRecord);
	}

	public PlayerStatistics() {
		initialize();
	}

	private void initialize() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatMessages = 0;
		this._lastChatMessage = null;
		resetConsecutiveDays();
		this._timeInfo = new TimeStatistics();
		this._startTime = null;
		this._hasBeenUpdated = false;
		this._afkDescription = null;
		this._lastAliveTime = LocalDateTime.now();
	}

	void resetConsecutiveDays() {
		this._consecutiveDays = 0;
		this._lastConsecutiveDay = this._timeInfo.getToday();
	}

	private void resetAlarm() {
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "Reset alarm for player %s", getName());
		AlarmTrigger alarmTrigger = AlarmTrigger.get();
		if (alarmTrigger.resetAlarm(this._alarmId, Config.V.allowedInactivityInSeconds)) return;
		this._alarmId = setAlarm();	
	}

	private UUID setAlarm() {
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "Setting alarm for player %s in %d seconds",
				getName(), Config.V.allowedInactivityInSeconds);
		return AlarmTrigger.get()
				.setAlarm(String.format("%s is idle", getName()),
						Config.V.allowedInactivityInSeconds,
						new Runnable() {
					public void run() {
						setAsIdle();
					}
				});
	}

	public boolean isOnline() {
		return this._eithonPlayer.isOnline();
	}

	protected void setAsIdle() {
		if (isAfk()) return;
		eithonLogger.debug(DebugPrintLevel.MINOR, "Player %s is idle", getName());
		stop(Config.M.playerIdle.getMessage());
		this._alarmId = null;
	}

	private void start(LocalDateTime startTime) {
		if (startTime == null) {
			startTime = LocalDateTime.now();
			resetAlarm();
		}
		this._startTime = startTime;
		this._hasBeenUpdated = true;
		this._afkDescription = null;
		this._lastAliveTime = this._startTime;
		eithonLogger.debug(DebugPrintLevel.MINOR, "Start: %s", startTime.toString());
	}

	public void start() {
		start(null);
	}

	public void updateAlive() {
		if (isAfk()) Config.M.fromAfkBroadcast.broadcastMessageToAllServers(getName());
		LocalDateTime now = LocalDateTime.now();
		this._lastAliveTime = now;
		resetAlarm();
		if (this._startTime == null) start(this._lastAliveTime);
		this._hasBeenUpdated = true;
	}

	public LocalDateTime stop(String description) {
		this._afkDescription = description;
		if (this._startTime == null) return null;
		final LocalDateTime stopTime = this._lastAliveTime;
		this._timeInfo.addInterval(this._startTime, stopTime);
		if (this._timeInfo.getPlayTimeTodayInSeconds() >= Config.V.secondsPerDayForConsecutiveDays) {
			final LocalDateTime today = this._timeInfo.getToday();
			if (!lastConsecutiveDayWasToday()) {
				if (!lastConsecutiveDayWasYesterday()) {
					if (this._consecutiveDays > 0) {
						eithonLogger.debug(DebugPrintLevel.MAJOR, "Player %s was last logged in %s (today is %s), lost %d consecutive days", 
								this._eithonPlayer.getName(), this._lastConsecutiveDay.toString(), today.toString(), this._consecutiveDays);
					}
					this._consecutiveDays = 0;
				}
				this._lastConsecutiveDay = today;
				this._consecutiveDays++;
				eithonLogger.debug(DebugPrintLevel.MAJOR, "Player %s now has %d consecutive days", 
						this._eithonPlayer.getName(), this._consecutiveDays);
				ConsecutiveDaysEvent e = new ConsecutiveDaysEvent(this._eithonPlayer.getPlayer(), this._consecutiveDays);
				this._eithonPlayer.getServer().getPluginManager().callEvent(e);
			}
		}
		this._startTime = null;
		eithonLogger.debug(DebugPrintLevel.MINOR, "Stop: %s %s (%s)", getName(), stopTime.toString(), description);
		if (isAfk()) {
			Config.M.toAfkBroadcast.broadcastMessageToAllServers(getName(), description);
		}
		try {
			save(false);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return stopTime;
	}

	public long addToTotalPlayTime(long playTimeInSeconds) {
		return this._timeInfo.addToTotalPlayTime(playTimeInSeconds);
	}

	public long addToConsecutiveDays(long consecutiveDays) {
		this._consecutiveDays += consecutiveDays;
		if (this._consecutiveDays < 0) this._consecutiveDays = 0;
		if (this._consecutiveDays > 0) {
			this._lastConsecutiveDay = this._timeInfo.getToday();
		}
		return this._consecutiveDays;
	}

	public long addToBlocksCreated(long blocksCreated) {
		this._blocksCreated += blocksCreated;
		if (this._blocksCreated < 0) this._blocksCreated = 0;
		return this._blocksCreated;
	}

	public long addToBlocksBroken(long blocksBroken) {
		this._blocksBroken += blocksBroken;
		if (this._blocksBroken < 0) this._blocksBroken = 0;
		return this._blocksBroken;
	}

	public void resetTotalPlayTime() {
		this._timeInfo.resetTotalPlayTime();
	}

	public void lap() {
		if (this._startTime == null) return;
		LocalDateTime stopTime = stop(null);
		start(stopTime);
	}

	public void addChatActivity() {
		this._chatMessages++;
		this._lastChatMessage = LocalDateTime.now(); 
	}

	public void addBlocksCreated(long blocks) { this._blocksCreated += blocks; }
	public void addBlocksBroken(long blocks) { this._blocksBroken += blocks; }

	public void save(boolean doLap) throws SQLException, ClassNotFoundException {
		if (!this._hasBeenUpdated) return;
		if (doLap) {
			lap();
		}
		this._dbRecord.update(
				this._eithonPlayer.getName(),
				this._timeInfo.getFirstStartTime(),
				this._timeInfo.getLastStopTime(),
				this._timeInfo.getTotalPlayTimeInSeconds(),
				this._timeInfo.getIntervals(),
				this._timeInfo.getLongestIntervalInSeconds(),
				this._timeInfo.getPlayTimeTodayInSeconds(),
				this._timeInfo.getToday(),
				this._chatMessages,
				this._lastChatMessage,
				this._blocksCreated, 
				this._blocksBroken, 
				this._consecutiveDays, 
				this._lastConsecutiveDay);
		eithonLogger.debug(DebugPrintLevel.MAJOR, "Saved player %s", getName());
		this._hasBeenUpdated = false;
	}

	public void update(PlayerStatisticsOld oldStatistics) {
		this._chatMessages = oldStatistics._chatActivities;
		this._lastChatMessage = oldStatistics._lastChatActivity;
		this._blocksCreated = oldStatistics._blocksCreated;
		this._blocksBroken = oldStatistics._blocksBroken;
		this._consecutiveDays = oldStatistics._consecutiveDays;
		this._lastConsecutiveDay = oldStatistics._lastConsecutiveDay;
		this._timeInfo.update(oldStatistics._timeInfo);
		try {
			save(false);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getName() {
		if (this._eithonPlayer == null) return null;
		return this._eithonPlayer.getName(); }

	public UUID getUniqueId() { 
		if (this._eithonPlayer == null) return null;
		return this._eithonPlayer.getUniqueId(); 
	}

	public void sendPlayerStatistics(CommandSender sender) {
		Config.M.playerStats.sendMessage(sender, getNamedArguments());
	}

	public void sendTimeStats(CommandSender sender) {
		Config.M.timeStats.sendMessage(sender, getNamedArguments());
	}

	public void sendChatStats(CommandSender sender) {
		Config.M.chatStats.sendMessage(sender, getNamedArguments());
	}

	public void sendBlockStats(CommandSender sender) {
		Config.M.blockStats.sendMessage(sender, getNamedArguments());
	}

	public long getTotalTimeInSeconds() { return this._timeInfo.getTotalPlayTimeInSeconds(); }

	public boolean isAfk() {
		return isOnline() && (this._afkDescription != null);
	}

	public boolean isActive() {
		return isOnline() && !isAfk();
	}

	public long getBlocksCreated() { return this._blocksCreated; }

	public LocalDateTime getLastConsecutiveDay() {
		if (lastConsecutiveDayWasTooLongAgo()) {
			resetConsecutiveDays();
		}
		return this._lastConsecutiveDay; 
	}

	public long getConsecutiveDays() {
		if (lastConsecutiveDayWasTooLongAgo()) {
			resetConsecutiveDays();
		}
		return this._consecutiveDays; 
	}

	public EithonPlayer getEithonPlayer() { return this._eithonPlayer; }

	boolean lastConsecutiveDayWasTooLongAgo() {
		return !lastConsecutiveDayWasToday() && !lastConsecutiveDayWasYesterday();
	}

	private boolean lastConsecutiveDayWasToday() {
		final LocalDateTime today = this._timeInfo.getToday();
		return lastConsecutiveDayWasThisDay(today);
	}

	private boolean lastConsecutiveDayWasYesterday() {
		final LocalDateTime yesterday = this._timeInfo.getToday().minusDays(1);
		return lastConsecutiveDayWasThisDay(yesterday);
	}

	private boolean lastConsecutiveDayWasThisDay(LocalDateTime day) {
		return TimeStatistics.isSameDay(day, this._lastConsecutiveDay);
	}

	public long getChatMessages() { return this._chatMessages; }

	public LocalDateTime getAfkTime() { return this._lastAliveTime; }

	public Object getAfkDescription() { return this._afkDescription; }

	HashMap<String,String> getNamedArguments() {
		String status = "";
		if (this._eithonPlayer.isOnline()) {
			if (this._afkDescription == null) status = Config.M.statusOnline.getMessageWithColorCoding();
			else status = Config.M.statusAfk.getMessageWithColorCoding(this._afkDescription);
		} else {
			status = Config.M.statusOffline.getMessageWithColorCoding();
		}

		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", this._eithonPlayer.getName());
		namedArguments.put("STATUS_DESCRIPTION", status);
		namedArguments.put("BLOCKS_BROKEN", String.format("%d", this._blocksBroken));
		namedArguments.put("BLOCKS_CREATED", String.format("%d", this._blocksCreated));
		namedArguments.put("BLOCKS_CREATED_OR_BROKEN", String.format("%d", this._blocksCreated + this._blocksBroken));
		namedArguments.put("CHAT_ACTIVITIES", String.format("%d", this._chatMessages));
		namedArguments.put("INTERVALS", String.format("%d", this._timeInfo.getIntervals()));
		namedArguments.put("TOTAL_PLAY_TIME", TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()));
		namedArguments.put("LONGEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getLongestIntervalInSeconds()));
		namedArguments.put("LATEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getPreviousIntervalInSeconds()));
		namedArguments.put("CONSECUTIVE_DAYS", String.format("%d", getConsecutiveDays()));
		final LocalDateTime lastConsecutiveDay = getLastConsecutiveDay();
		namedArguments.put("LAST_CONSECUTIVE_DAY", lastConsecutiveDay == null ? "-" : lastConsecutiveDay.toString());

		return namedArguments;
	}

	public long getBlocksBroken() { return this._blocksBroken; }

	public void timespanSave(Database database) throws SQLException, ClassNotFoundException {
		new HourStatistics(database, this._lastHourValues, this, LocalDateTime.now());
	}
}
