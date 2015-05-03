package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import net.eithon.library.json.IJson;
import net.eithon.library.time.TimeMisc;

import org.json.simple.JSONObject;

public class TimeStatistics implements IJson<TimeStatistics>{
	// Saved variables
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private long _totalPlayTimeInSeconds;
	private long _intervals;
	private long _longestIntervalInSeconds;
	
	// Non-saved, internal variables
	private long _previousIntervalInSeconds;
	private LocalDateTime _previousStartTime;
	private LocalDateTime _previousStopTime;

	public TimeStatistics()
	{
		this._previousStartTime = null;
		this._firstStartTime = null;
		this._totalPlayTimeInSeconds = 0;
		this._intervals = 0;
		this._longestIntervalInSeconds = 0;
		this._previousIntervalInSeconds = 0;
	}
	
	public void addInterval(LocalDateTime start, LocalDateTime stop) {
		long useLastInterval = 0;
		if ((this._previousStopTime != null) && (this._previousStopTime.isEqual(start))) useLastInterval = this._previousIntervalInSeconds;
		long playTimeInSeconds = stop.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC);
		rememberStartTime(start);
		rememberStopTime(stop);
		rememberInterval(useLastInterval + playTimeInSeconds);
		this._totalPlayTimeInSeconds += playTimeInSeconds;
	}

	private void rememberInterval(long interval) {
		this._previousIntervalInSeconds = interval;
		if (this._longestIntervalInSeconds < interval) this._longestIntervalInSeconds = interval;
	}

	private void rememberStartTime(LocalDateTime start) {
		this._previousStartTime = start;
		if ((this._firstStartTime == null) || (this._firstStartTime.isAfter(start))) this._firstStartTime = start;
	}

	private void rememberStopTime(LocalDateTime stop) {
		this._previousStopTime = stop;
		if ((this._lastStopTime == null) || (this._lastStopTime.isBefore(stop))) this._lastStopTime = stop;
	}

	public LocalDateTime getPreviousStartTime() { return this._previousStartTime; }
	public LocalDateTime getPreviousStopTime() { return this._previousStopTime; }
	public long getPreviousInterval() { return this._previousIntervalInSeconds; }
	public long getTotalPlayTimeInSeconds() { return this._totalPlayTimeInSeconds; }
	public long getLongestIntervalInSeconds() { return this._longestIntervalInSeconds; }
	public Object getIntervals() { return this._intervals; }

	
	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("firstStart", TimeMisc.fromLocalDateTime(this._firstStartTime));
		json.put("lastStop", TimeMisc.fromLocalDateTime(this._lastStopTime));
		json.put("totalPlayTimeInSeconds", this._totalPlayTimeInSeconds);
		json.put("intervals", this._intervals);
		json.put("longestIntervalInSeconds", this._longestIntervalInSeconds);
		return json;
	}

	@Override
	public TimeStatistics fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._firstStartTime = TimeMisc.toLocalDateTime(jsonObject.get("firstStart"));
		this._lastStopTime = TimeMisc.toLocalDateTime(jsonObject.get("lastStop"));
		this._totalPlayTimeInSeconds = (long)jsonObject.get("totalPlayTimeInSeconds");
		this._intervals = (long)jsonObject.get("intervals");
		this._longestIntervalInSeconds = (long)jsonObject.get("longestIntervalInSeconds");
		return this;
	}

	@Override
	public TimeStatistics factory() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static TimeStatistics getFromJson(Object json) {
		TimeStatistics info = new TimeStatistics();
		return info.fromJson(json);
	}
}
