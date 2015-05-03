package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.IJsonDelta;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerStatistics implements IJsonDelta<PlayerStatistics>, IUuidAndName {
	
	// Saved variables
	private EithonPlayer _eithonPlayer;
	private long _blocksDestroyed;
	private long _blocksCreated;
	private long _chatActivities;
	private LocalDateTime _lastChatActivity;
	private TimeStatistics _timeInfo;	
	
	// Non-saved, internal variables
	private LocalDateTime _startTime;
	private LocalDateTime _lastAliveTime;
	private boolean _hasBeenUpdated;
	private String _afkDescription;


	public PlayerStatistics(Player player)
	{
		this();
		this._eithonPlayer = new EithonPlayer(player);
	}

	PlayerStatistics() {
		this._blocksDestroyed = 0;
		this._blocksCreated = 0;
		this._chatActivities = 0;
		this._lastChatActivity = null;
		this._timeInfo = new TimeStatistics();
		
		this._startTime = null;
		this._lastAliveTime = LocalDateTime.now();
		this._hasBeenUpdated = false;
		this._afkDescription = null;
	}

	public static PlayerStatistics getDifference(PlayerStatistics now,
			PlayerStatistics then) {
		PlayerStatistics diff = new PlayerStatistics();
		diff._afkDescription = now._afkDescription;
		diff._blocksCreated = now._blocksCreated - then._blocksCreated;
		diff._blocksDestroyed = now._blocksDestroyed - then._blocksDestroyed;
		diff._chatActivities = now._chatActivities - then._chatActivities;
		diff._eithonPlayer = now._eithonPlayer;
		diff._hasBeenUpdated = now._hasBeenUpdated;
		diff._lastAliveTime = now._lastAliveTime;
		diff._lastChatActivity = now._lastChatActivity;
		diff._startTime = now._startTime;
		diff._timeInfo = TimeStatistics.getDifference(now._timeInfo, then._timeInfo);
		return diff;
	}

	private void start(LocalDateTime startTime) {
		if (startTime == null) startTime = LocalDateTime.now();
		this._startTime = startTime;
		this._hasBeenUpdated = true;
		this._afkDescription = null;
		this._lastAliveTime = this._startTime;
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Start: %s", startTime.toString());
	}

	public void start() {
		start(null);
	}
	
	public void updateAlive() {
		LocalDateTime now = LocalDateTime.now();
		if (tooLongInactive(now)) lap();
		this._lastAliveTime = now;
		if (this._startTime == null) start(this._lastAliveTime);
		this._hasBeenUpdated = true;
	}

	private boolean tooLongInactive(LocalDateTime time) {
		return time.minusSeconds(Config.V.allowedInactivityInSeconds).isAfter(this._lastAliveTime);
	}

	public LocalDateTime stop(String description) {
		this._afkDescription = description;
		if (this._startTime == null) return null;
		LocalDateTime stopTime = noLaterThanLastAliveTime(LocalDateTime.now());
		this._lastAliveTime = stopTime;
		this._timeInfo.addInterval(this._startTime, stopTime);
		this._startTime = null;
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Stop: %s (%s)", stopTime.toString(), description);
		return stopTime;
	}

	private LocalDateTime noLaterThanLastAliveTime(LocalDateTime time) {
		if (!tooLongInactive(time)) return time;
		return this._lastAliveTime;
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
	public void addBlocksDestroyed(long blocks) { this._blocksDestroyed += blocks; }

	@Override
	public PlayerStatistics factory() { return new PlayerStatistics(); }

	@Override
	public PlayerStatistics fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJSon(jsonObject.get("player"));
		this._timeInfo = TimeStatistics.getFromJson(jsonObject.get("timeInfo"));
		this._chatActivities = (long)jsonObject.get("chatActivities");
		this._lastChatActivity = TimeMisc.toLocalDateTime(jsonObject.get("lastChatActivity"));
		this._blocksCreated = (long)jsonObject.get("blocksCreated");
		this._blocksDestroyed = (long)jsonObject.get("blocksDestroyed");
		return this;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJsonDelta(boolean saveAll, boolean doLap) {
		if (!saveAll && !this._hasBeenUpdated) {
			return null;
		}
		if (doLap) {
			lap();
		}
		JSONObject json = new JSONObject();
		json.put("player", this._eithonPlayer.toJson());
		json.put("timeInfo", this._timeInfo.toJson());
		json.put("chatActivities", this._chatActivities);
		json.put("lastChatActivity", TimeMisc.fromLocalDateTime(this._lastChatActivity));
		json.put("blocksCreated", this._blocksCreated);
		json.put("blocksDestroyed", this._blocksDestroyed);
		this._hasBeenUpdated = false;
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

	public String getName() { return this._eithonPlayer.getName(); }

	public UUID getUniqueId() { return this._eithonPlayer.getUniqueId(); }

	public String toString() {
		String result = String.format("%s: Playtime %s, chats %d, blocks %d",
				getName(),
				TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()),
				this._chatActivities, this._blocksCreated);
		if (this._afkDescription != null) {
			result += " AFK: " + this._afkDescription;
		}
		return result;
	}

	public String timeStats() {
		String result = String.format("%s in %d intervals (longest %s, latest %s)",
				TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()), 
				this._timeInfo.getIntervals(), 
				TimeMisc.secondsToString(this._timeInfo.getLongestIntervalInSeconds()), 
				TimeMisc.secondsToString(this._timeInfo.getPreviousInterval()));
		if (this._afkDescription != null) {
			result += " AFK: " + this._afkDescription;
		}
		return result;
	}

	public String diffStats() {
		String result = String.format("%s in %d intervals, %d blocks created, %d destroyed, %d chats.",
				TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()), 
				this._timeInfo.getIntervals(),
				this._blocksCreated,
				this._blocksDestroyed,
				this._chatActivities);
		if (this._afkDescription != null) {
			result += " AFK: " + this._afkDescription;
		}
		return result;
	}

	public String chatStats() {
		String result = String.format("%d chats (latest %s)",
				this._chatActivities, this._lastAliveTime.toString());
		return result;
	}

	public String blockStats() {
		String result = String.format("%d blocks created (destroyed %d)",
				this._blocksCreated, this._blocksDestroyed);
		return result;
	}

	public long getTotalTimeInSeconds() { return this._timeInfo.getLongestIntervalInSeconds(); }

	public boolean isAfk() {
		return this._afkDescription != null;
	}

	public long getBlocksCreated() { return this._blocksCreated; }

	public long getChats() { return this._chatActivities; }

	public LocalDateTime getAfkTime() { return this._lastAliveTime; }

	public Object getAfkDescription() { return this._afkDescription; }
}
