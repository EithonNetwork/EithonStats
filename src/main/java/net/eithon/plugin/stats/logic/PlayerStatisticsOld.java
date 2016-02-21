package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.JsonObjectDelta;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.AlarmTrigger;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerStatisticsOld extends JsonObjectDelta<PlayerStatisticsOld> implements IUuidAndName {
	private static Logger eithonLogger;

	// Saved variables
	EithonPlayer _eithonPlayer;
	long _blocksBroken;
	 long _blocksCreated;
	 long _chatActivities;
	 LocalDateTime _lastChatActivity;
	 long _consecutiveDays;
	 LocalDateTime _lastConsecutiveDay;
	 TimeStatisticsOld _timeInfo;

	// Non-saved, internal variables
	private LocalDateTime _startTime;
	private LocalDateTime _lastAliveTime;
	private UUID _alarmId;
	private boolean _hasBeenUpdated;
	private String _afkDescription;

	static void initialize(Logger logger) {
		eithonLogger = logger;
	}

	PlayerStatisticsOld(Player player)
	{
		this(new EithonPlayer(player));
	}

	private PlayerStatisticsOld(EithonPlayer eithonPlayer)
	{
		this();
		this._eithonPlayer = eithonPlayer;
	}

	PlayerStatisticsOld() {
		this._blocksBroken = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._lastChatActivity = null;
		this._timeInfo = new TimeStatisticsOld();
		resetConsecutiveDays();
		this._startTime = null;
		this._hasBeenUpdated = false;
		this._afkDescription = null;
		this._lastAliveTime = LocalDateTime.now();
	}

	private void resetConsecutiveDays() {
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

	private boolean isOnline() {
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

	private LocalDateTime stop(String description) {
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

	private void lap() {
		if (this._startTime == null) return;
		LocalDateTime stopTime = stop(null);
		start(stopTime);
	}

	@Override
	public PlayerStatisticsOld factory() { return new PlayerStatisticsOld(); }

	@Override
	public PlayerStatisticsOld fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJson(jsonObject.get("player"));
		this._timeInfo = TimeStatisticsOld.getFromJson(jsonObject.get("timeInfo"));
		this._chatActivities = (long)jsonObject.get("chatActivities");
		this._lastChatActivity = TimeMisc.toLocalDateTime((String)jsonObject.get("lastChatActivity"));
		this._blocksCreated = (long)jsonObject.get("blocksCreated");
		this._blocksBroken = (long)jsonObject.get("blocksBroken");
		Object days = jsonObject.get("consecutiveDays");
		if (days == null) this._consecutiveDays = 0;
		else this._consecutiveDays = (long)days;
		this._lastConsecutiveDay = TimeMisc.toLocalDateTime((String)jsonObject.get("lastConsecutiveDay"));
		return this;
	}
	
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

	private boolean isAfk() {
		return isOnline() && (this._afkDescription != null);
	}

	EithonPlayer getEithonPlayer() { return this._eithonPlayer; }

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

	long getChats() { return this._chatActivities; }

	LocalDateTime getAfkTime() { return this._lastAliveTime; }

	Object getAfkDescription() { return this._afkDescription; }
}
