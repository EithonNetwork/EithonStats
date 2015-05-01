package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.IJson;
import net.eithon.plugin.stats.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerTime implements IJson<PlayerTime>, IUuidAndName {
	private EithonPlayer _eithonPlayer;
	private long _totalPlayTimeInSeconds;
	private long _intervals;
	private long _lastIntervalInSeconds;
	private long _longestIntervalInSeconds;
	private LocalDateTime _startTime;
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private LocalDateTime _lastAliveTime;
	private boolean _hasBeenUpdated;

	public PlayerTime(Player player)
	{
		this._eithonPlayer = new EithonPlayer(player);
		this._totalPlayTimeInSeconds = 0;
		this._intervals = 0;
		this._lastIntervalInSeconds = 0;
		this._longestIntervalInSeconds = 0;
		this._firstStartTime = null;
		this._lastStopTime = null;
		this._hasBeenUpdated = false;
		this._lastAliveTime = LocalDateTime.now();
	}

	PlayerTime() {
	}

	private void start(LocalDateTime startTime) {
		if (startTime == null) startTime = LocalDateTime.now();
		this._startTime = startTime;
		this._hasBeenUpdated = true;
		if (this._firstStartTime == null) this._firstStartTime = this._startTime;
		this._intervals++;
		this._lastAliveTime = this._startTime;
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
	}

	public LocalDateTime stop() {
		if (this._startTime == null) return null;
		LocalDateTime now = LocalDateTime.now();
		this._lastStopTime = now;
		if (now.minusMinutes(Config.V.inactivityMinutes).isAfter(this._lastAliveTime)) {
			this._lastStopTime = this._lastAliveTime.plusMinutes(Config.V.inactivityMinutes);
		}

		long nonBrokenInterval = 0;
		if (this._startTime.isEqual(this._lastStopTime)) {
			nonBrokenInterval = this._lastIntervalInSeconds;
		}
		this._lastIntervalInSeconds = this._lastStopTime.toEpochSecond(ZoneOffset.UTC) - this._startTime.toEpochSecond(ZoneOffset.UTC);
		if (this._longestIntervalInSeconds < nonBrokenInterval + this._lastIntervalInSeconds) {
			this._longestIntervalInSeconds = nonBrokenInterval + this._lastIntervalInSeconds;
		}
		this._totalPlayTimeInSeconds += this._lastIntervalInSeconds;
		this._startTime = null;
		return now;
	}

	private void lap() {
		if (this._startTime == null) return;
		LocalDateTime stopTime = stop();
		start(stopTime);
	}

	@Override
	public PlayerTime factory() { return new PlayerTime(); }

	@Override
	public PlayerTime fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._eithonPlayer = EithonPlayer.getFromJSon(jsonObject.get("player"));
		this._firstStartTime = LocalDateTime.parse((String)jsonObject.get("firstStart"));
		this._lastStopTime = LocalDateTime.parse((String)jsonObject.get("lastStop"));
		this._totalPlayTimeInSeconds = (long)jsonObject.get("totalPlayTimeInSeconds");
		this._intervals = (long)jsonObject.get("intervals");
		this._longestIntervalInSeconds = (long)jsonObject.get("longestIntervalInSeconds");
		this._lastIntervalInSeconds = (long)jsonObject.get("lastIntervalInSeconds");
		return this;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson(boolean doLap) {
		if (!this._hasBeenUpdated) return null;
		if (doLap) {
			this._hasBeenUpdated = false;
			lap();
		}
		JSONObject json = new JSONObject();
		json.put("player", this._eithonPlayer.toJson());
		String startTime = "";
		if (this._firstStartTime != null) {
			startTime = this._firstStartTime.toString();
		}
		json.put("firstStart", startTime);
		String stopTime = "";
		if (this._lastStopTime != null) {
			stopTime = this._lastStopTime.toString();
		}
		json.put("lastStop", stopTime);
		json.put("totalPlayTimeInSeconds", this._totalPlayTimeInSeconds);
		json.put("intervals", this._intervals);
		json.put("longestIntervalInSeconds", this._longestIntervalInSeconds);
		json.put("lastIntervalInSeconds", this._lastIntervalInSeconds);
		return json;
	}
	
	@Override
	public JSONObject toJson() {
		return toJson(true);
	}

	public String getName() { return this._eithonPlayer.getName(); }

	public UUID getUniqueId() { return this._eithonPlayer.getUniqueId(); }

	public String toString() {
		return String.format("%s: %s", toJson(false).toJSONString());
	}
}
