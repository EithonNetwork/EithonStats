package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.util.HashMap;
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
	private long _blocksBroken;
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
		this._blocksBroken = 0;
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
		diff._blocksCreated = now._blocksCreated - ((then == null) ? 0 : then._blocksCreated);
		diff._blocksBroken = now._blocksBroken - ((then == null) ? 0 : then._blocksBroken);
		diff._chatActivities = now._chatActivities - ((then == null) ? 0 : then._chatActivities);
		diff._eithonPlayer = now._eithonPlayer;
		diff._hasBeenUpdated = now._hasBeenUpdated;
		diff._lastAliveTime = now._lastAliveTime;
		diff._lastChatActivity = now._lastChatActivity;
		diff._startTime = now._startTime;
		diff._timeInfo = TimeStatistics.getDifference(now._timeInfo, (then == null) ? null : then._timeInfo);
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
	public void addBlocksBroken(long blocks) { this._blocksBroken += blocks; }

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
		this._blocksBroken = (long)jsonObject.get("blocksBroken");
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
		json.put("blocksBroken", this._blocksBroken);
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
		return Config.M.playerStats.getMessage(getNamedArguments());
	}

	public String timeStats() {
		return Config.M.timeStats.getMessage(getNamedArguments());
	}

	public String diffStats() {
		return Config.M.diffStats.getMessage(getNamedArguments());
	}

	public String chatStats() {
		return Config.M.chatStats.getMessage(getNamedArguments());
	}

	public String blockStats() {
		return Config.M.blockStats.getMessage(getNamedArguments());
	}

	public long getTotalTimeInSeconds() { return this._timeInfo.getTotalPlayTimeInSeconds(); }

	public boolean isAfk() {
		return this._afkDescription != null;
	}

	public long getBlocksCreated() { return this._blocksCreated; }

	public long getChats() { return this._chatActivities; }

	public LocalDateTime getAfkTime() { return this._lastAliveTime; }

	public Object getAfkDescription() { return this._afkDescription; }
	
	private HashMap<String,String> getNamedArguments() {
		HashMap<String,String> namedArguments = new HashMap<String, String>();
		namedArguments.put("PLAYER_NAME", this._eithonPlayer.getName());
		namedArguments.put("AFK_DESCRIPTION", this._afkDescription == null ? "" : this._afkDescription);
		namedArguments.put("BLOCKS_BROKEN", String.format("%d", this._blocksBroken));
		namedArguments.put("BLOCKS_CREATED", String.format("%d", this._blocksCreated));
		namedArguments.put("BLOCKS_CREATED_OR_BROKEN", String.format("%d", this._blocksCreated + this._blocksBroken));
		namedArguments.put("CHAT_ACTIVITIES", String.format("%d", this._chatActivities));
		namedArguments.put("INTERVALS", String.format("%d", this._timeInfo.getIntervals()));
		namedArguments.put("TOTAL_PLAY_TIME", TimeMisc.secondsToString(this._timeInfo.getTotalPlayTimeInSeconds()));
		namedArguments.put("LONGEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getLongestIntervalInSeconds()));
		namedArguments.put("LATEST_INTERVAL", TimeMisc.secondsToString(this._timeInfo.getPreviousIntervalInSeconds()));
		
		return namedArguments;
	}
}
