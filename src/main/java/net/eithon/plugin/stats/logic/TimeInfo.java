package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import net.eithon.library.json.IJson;

import org.json.simple.JSONObject;

public class TimeInfo implements IJson<TimeInfo>{
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private long _totalPlayTimeInSeconds;
	private long _intervals;
	private long _longestIntervalInSeconds;
	
	private long _previousIntervalInSeconds;
	private LocalDateTime _previousStartTime;
	private LocalDateTime _previousStopTime;

	public TimeInfo()
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
		String firstStart = "";
		if (this._firstStartTime != null) {
			firstStart = this._firstStartTime.toString();
		}
		json.put("firstStart", firstStart);
		String lastStop = "";
		if (this._lastStopTime != null) {
			lastStop = this._lastStopTime.toString();
		}
		json.put("lastStop", lastStop);
		json.put("totalPlayTimeInSeconds", this._totalPlayTimeInSeconds);
		json.put("intervals", this._intervals);
		json.put("longestIntervalInSeconds", this._longestIntervalInSeconds);
		return json;
	}

	@Override
	public TimeInfo fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._firstStartTime = LocalDateTime.parse((String)jsonObject.get("lastStop"));
		this._lastStopTime = LocalDateTime.parse((String)jsonObject.get("firstStart"));
		this._totalPlayTimeInSeconds = (long)jsonObject.get("totalPlayTimeInSeconds");
		this._intervals = (long)jsonObject.get("intervals");
		this._longestIntervalInSeconds = (long)jsonObject.get("longestIntervalInSeconds");
		return this;
	}

	@Override
	public TimeInfo factory() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static TimeInfo getFromJson(Object json) {
		TimeInfo info = new TimeInfo();
		return info.fromJson(json);
	}
}
