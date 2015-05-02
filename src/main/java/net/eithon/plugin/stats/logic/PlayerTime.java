package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.IJsonDelta;
import net.eithon.library.plugin.Logger;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.stats.Config;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class PlayerTime implements IJsonDelta<PlayerTime>, IUuidAndName {
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
		if (this._startTime == null) start(now);
		this._lastAliveTime = now;
	}

	public LocalDateTime stop() {
		if (this._startTime == null) return null;
		LocalDateTime now = LocalDateTime.now();

		long lastNonBrokenInterval = 0;
		if ((this._lastStopTime != null) && this._startTime.isEqual(this._lastStopTime)) {
			lastNonBrokenInterval = this._lastIntervalInSeconds;
			Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Non broken interval %d", lastNonBrokenInterval);
		} else {
			this._intervals++;
		}
		
		this._lastStopTime = noLaterThanLastAliveTime(now);

		long thisIntervalInSeconds = this._lastStopTime.toEpochSecond(ZoneOffset.UTC) - this._startTime.toEpochSecond(ZoneOffset.UTC);
		long nonBrokenInterval = lastNonBrokenInterval + thisIntervalInSeconds;
		if (this._longestIntervalInSeconds < nonBrokenInterval) {
			this._longestIntervalInSeconds = nonBrokenInterval;
		}
		this._lastIntervalInSeconds = nonBrokenInterval;
		this._totalPlayTimeInSeconds += thisIntervalInSeconds;
		this._startTime = null;
		Logger.libraryDebug(DebugPrintLevel.VERBOSE, "Stop: %s", now.toString());
		return now;
	}

	private LocalDateTime noLaterThanLastAliveTime(LocalDateTime time) {
		if (time.minusMinutes(Config.V.inactivityMinutes).isBefore(this._lastAliveTime)) return time;
		return this._lastAliveTime.plusMinutes(Config.V.inactivityMinutes);
	}

	public void lap() {
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
		return String.format("%s: latest: %d seconds, total %d seconds in %d intervals (longest %d seconds)",
				getName(), this._lastIntervalInSeconds, 
				this._totalPlayTimeInSeconds, this._intervals, this._longestIntervalInSeconds);
	}
}
