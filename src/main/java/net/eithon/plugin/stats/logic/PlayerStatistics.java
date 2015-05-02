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
	private EithonPlayer _eithonPlayer;
	private LocalDateTime _lastAliveTime;
	private long _blocksDestroyed;
	private long _blocksCreated;
	private long _chatActivities;
	private LocalDateTime _lastChatActivity;
	private boolean _hasBeenUpdated;
	private String _afkDescription;
	private TimeInfo _timeInfo;	
	private LocalDateTime _startTime;


	public PlayerStatistics(Player player)
	{
		this();
		this._eithonPlayer = new EithonPlayer(player);
	}

	PlayerStatistics() {
		this._timeInfo = new TimeInfo();
		this._hasBeenUpdated = false;
		this._lastAliveTime = LocalDateTime.now();
		this._chatActivities = 0;
		this._lastChatActivity = null;
		this._blocksCreated = 0;
		this._blocksDestroyed = 0;
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
		if (now.minusMinutes(Config.V.inactivityMinutes).isAfter(this._lastAliveTime)) {
			lap();
		}
		this._lastAliveTime = now;
		if (this._startTime == null) start(this._lastAliveTime);
	}

	public LocalDateTime stop(String description) {
		this._afkDescription = description;
		if (this._startTime == null) return null;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime stopTime = noLaterThanLastAliveTime(now);
		
		this._timeInfo.addInterval(this._startTime, stopTime);
		this._startTime = null;
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Stop: %s (%s)", now.toString(), description);
		return stopTime;
	}

	private LocalDateTime noLaterThanLastAliveTime(LocalDateTime time) {
		if (time.minusMinutes(Config.V.inactivityMinutes).isBefore(this._lastAliveTime)) return time;
		return this._lastAliveTime.plusMinutes(Config.V.inactivityMinutes);
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

	public void addBlocksCreated(long blocks) {
		this._blocksCreated += blocks;
	}

	public void addBlocksDestroyed(long blocks) {
		this._blocksDestroyed += blocks;
	}

	@Override
	public PlayerStatistics factory() { return new PlayerStatistics(); }

	@Override
	public PlayerStatistics fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJSon(jsonObject.get("player"));
		this._timeInfo = TimeInfo.getFromJson(jsonObject.get("timeInfo"));
		this._chatActivities = (long)jsonObject.get("chatActivities");
		this._lastChatActivity = LocalDateTime.parse((String)jsonObject.get("lastChatActivity"));
		this._blocksCreated = (long)jsonObject.get("blocksCreated");
		this._blocksDestroyed = (long)jsonObject.get("blocksDestroyed");
		return this;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJsonDelta(boolean saveAll, boolean doLap) {
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Enter toJson: %s", getName());
		if (!saveAll && !this._hasBeenUpdated) {
			Logger.libraryDebug(DebugPrintLevel.VERBOSE, "toJson: Not updated");
			return null;
		}
		if (doLap) {
			Logger.libraryDebug(DebugPrintLevel.VERBOSE, "toJson: Do lap");
			this._hasBeenUpdated = false;
			lap();
		}
		JSONObject json = new JSONObject();
		json.put("player", this._eithonPlayer.toJson());
		json.put("timeInfo", this._timeInfo.toJson());
		json.put("chatActivities", this._chatActivities);
		json.put("lastChatActivity", this._lastChatActivity);
		json.put("blocksCreated", this._blocksCreated);
		json.put("blocksDestroyed", this._blocksDestroyed);
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "toJson: Completed");
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
}
