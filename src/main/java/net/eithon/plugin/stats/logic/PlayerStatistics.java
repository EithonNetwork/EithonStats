package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.JsonObjectDelta;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerStatistics extends JsonObjectDelta<PlayerStatistics> implements IUuidAndName {
	private static Logger eithonLogger;

	// Saved variables
	private EithonPlayer _eithonPlayer;
	private long _blocksBroken;
	private long _blocksCreated;
	private long _chatActivities;
	private LocalDateTime _lastChatActivity;
	private long _consecutiveDays;
	private LocalDateTime _lastConsecutiveDay;
	private TimeStatistics _timeInfo;

	// Non-saved, internal variables
	private LocalDateTime _startTime;
	private LocalDateTime _lastAliveTime;
	private UUID _alarmId;
	private boolean _hasBeenUpdated;
	private String _afkDescription;

	public static void initialize(Logger logger) {
		eithonLogger = logger;
	}

	public PlayerStatistics(Player player)
	{
		this(new EithonPlayer(player));
	}

	public PlayerStatistics(EithonPlayer eithonPlayer)
	{
		this();
		this._eithonPlayer = eithonPlayer;
	}

	PlayerStatistics() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._lastChatActivity = null;
		resetConsecutiveDays();
		this._timeInfo = new TimeStatistics();
		this._startTime = null;
		this._hasBeenUpdated = false;
		this._afkDescription = null;
		this._lastAliveTime = LocalDateTime.now();
	}

	void resetConsecutiveDays() {
		this._consecutiveDays = 0;
		this._lastConsecutiveDay = null;
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

	public static PlayerStatistics getDifference(PlayerStatistics now,
			PlayerStatistics then) {
		PlayerStatistics diff = new PlayerStatistics();
		diff._afkDescription = now._afkDescription;
		diff._blocksCreated = now._blocksCreated - ((then == null) ? 0 : then._blocksCreated);
		diff._blocksBroken = now._blocksBroken - ((then == null) ? 0 : then._blocksBroken);
		diff._chatActivities = now._chatActivities - ((then == null) ? 0 : then._chatActivities);
		long thenConsecutiveDays = (then == null) ? 0 : then._consecutiveDays;
		if (now._consecutiveDays > thenConsecutiveDays) {
			diff._consecutiveDays = now._consecutiveDays - thenConsecutiveDays;
		} else diff._consecutiveDays = now._consecutiveDays;
		diff._eithonPlayer = now._eithonPlayer;
		diff._hasBeenUpdated = now._hasBeenUpdated;
		diff._lastAliveTime = now._lastAliveTime;
		diff._lastChatActivity = now._lastChatActivity;
		diff._lastConsecutiveDay = now._lastConsecutiveDay;
		diff._startTime = now._startTime;
		diff._timeInfo = TimeStatistics.getDifference(now._timeInfo, (then == null) ? null : then._timeInfo);
		return diff;
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
		return stopTime;
	}

	public long addToTotalPlayTime(long playTimeInSeconds) {
		return this._timeInfo.addToTotalPlayTime(playTimeInSeconds);
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
		this._chatActivities++;
		this._lastChatActivity = LocalDateTime.now(); 
	}

	public void addBlocksCreated(long blocks) { this._blocksCreated += blocks; }
	public void addBlocksBroken(long blocks) { this._blocksBroken += blocks; }

	@Override
	public PlayerStatistics factory() { return new PlayerStatistics(); }

	@Override
	public PlayerStatistics fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJson(jsonObject.get("player"));
		this._timeInfo = TimeStatistics.getFromJson(jsonObject.get("timeInfo"));
		this._chatActivities = (long)jsonObject.get("chatActivities");
		this._lastChatActivity = TimeMisc.toLocalDateTime(jsonObject.get("lastChatActivity"));
		this._blocksCreated = (long)jsonObject.get("blocksCreated");
		this._blocksBroken = (long)jsonObject.get("blocksBroken");
		Object days = jsonObject.get("consecutiveDays");
		if (days == null) this._consecutiveDays = 0;
		else this._consecutiveDays = (long)days;
		this._lastConsecutiveDay = TimeMisc.toLocalDateTime(jsonObject.get("lastConsecutiveDay"));
		return this;
	}
	
	public static PlayerStatistics getFromJson(Object json) { return new PlayerStatistics().fromJson(json); }

	@SuppressWarnings("unchecked")
	private JSONObject toJsonDelta(boolean saveAll, boolean doLap) {
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Enter for player %s", this.getName());
		if (!saveAll && !this._hasBeenUpdated) {
			eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Player %s has not been updated", this.getName());
			eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Leave");
			return null;
		}

		if (doLap) {
			eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Player %s calls lap()", this.getName());
			lap();
		}
		JSONObject json = new JSONObject();
		json.put("player", this._eithonPlayer.toJson());
		json.put("timeInfo", this._timeInfo.toJson());
		json.put("chatActivities", this._chatActivities);
		json.put("lastChatActivity", TimeMisc.fromLocalDateTime(this._lastChatActivity));
		json.put("blocksCreated", this._blocksCreated);
		json.put("blocksBroken", this._blocksBroken);
		json.put("consecutiveDays", this._consecutiveDays);
		json.put("lastConsecutiveDay", TimeMisc.fromLocalDateTime(this._lastConsecutiveDay));
		this._hasBeenUpdated = false;
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Player %s result: %s", this.getName(), json.toString());
		eithonLogger.debug(DebugPrintLevel.VERBOSE, "PlayerStatistics.toJsonDelta: Leave");
		return json;
	}

	@Override
	public JSONObject toJson() {
		return toJsonDelta(true, true);
	}

	@Override
	public Object toJsonDelta(boolean saveAll) {
		return toJsonDelta(saveAll, true);
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

	public void sendDiffStats(CommandSender sender) {
		Config.M.diffStats.sendMessage(sender, getNamedArguments());
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

	public long getChats() { return this._chatActivities; }

	public LocalDateTime getAfkTime() { return this._lastAliveTime; }

	public Object getAfkDescription() { return this._afkDescription; }

	private HashMap<String,String> getNamedArguments() {
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
		namedArguments.put("CHAT_ACTIVITIES", String.format("%d", this._chatActivities));
		namedArguments.put("INTERVALS", String.format("%d", this._timeInfo.getIntervals()));
		namedArguments.put("TOTAL_PLAY_TIME", TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()));
		namedArguments.put("LONGEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getLongestIntervalInSeconds()));
		namedArguments.put("LATEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getPreviousIntervalInSeconds()));
		namedArguments.put("CONSECUTIVE_DAYS", String.format("%d", getConsecutiveDays()));
		final LocalDateTime lastConsecutiveDay = getLastConsecutiveDay();
		namedArguments.put("LAST_CONSECUTIVE_DAY", lastConsecutiveDay == null ? "-" : lastConsecutiveDay.toString());

		return namedArguments;
	}
}
