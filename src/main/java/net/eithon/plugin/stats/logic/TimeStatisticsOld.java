package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import net.eithon.library.json.JsonObject;
import net.eithon.library.time.TimeMisc;

import org.json.simple.JSONObject;

public class TimeStatisticsOld extends JsonObject<TimeStatisticsOld>{
	// Saved variables
	 LocalDateTime _firstStartTime;
	 LocalDateTime _lastStopTime;
	 long _totalPlayTimeInSeconds;
	 long _intervals;
	 long _longestIntervalInSeconds;
	 LocalDateTime _today;
	 long _playTimeTodayInSeconds;

	// Non-saved, internal variables
	private long _previousIntervalInSeconds;
	private LocalDateTime _previousStopTime;

	TimeStatisticsOld()
	{
		this._previousIntervalInSeconds = 0;
		resetTotalPlayTime();
	}

	void addInterval(LocalDateTime start, LocalDateTime stop) {
		long useLastInterval = 0;
		if ((this._previousStopTime != null) && (this._previousStopTime.isEqual(start))) {
			useLastInterval = this._previousIntervalInSeconds;
		}
		long playTimeInSeconds = differenceInSeconds(start, stop);
		rememberStartTime(start);
		rememberStopTime(stop);
		rememberInterval(useLastInterval, playTimeInSeconds);
		this._totalPlayTimeInSeconds += playTimeInSeconds;
		if (this._today == null) this._today = start.truncatedTo(ChronoUnit.DAYS);
		if (!isSameDay(this._today, stop)) {
			this._today = stop.truncatedTo(ChronoUnit.DAYS);
			this._playTimeTodayInSeconds = 0;
		}
		this._playTimeTodayInSeconds += playTimeInSeconds;
	}

	static boolean isSameDay(LocalDateTime time1, LocalDateTime time2) {
		if ((time1 == null)  || (time2 == null)) return false;
		return time1.toLocalDate().isEqual(time2.toLocalDate());
	}

	static long differenceInSeconds(LocalDateTime start, LocalDateTime stop) {
		return stop.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC);
	}

	private void resetTotalPlayTime() {
		this._firstStartTime = null;
		this._totalPlayTimeInSeconds = 0;
		this._intervals = 0;
		this._longestIntervalInSeconds = 0;
		resetIfNewDay();
	}

	private void rememberInterval(long useLastInterval, long playTimeInSeconds) {
		long interval = useLastInterval + playTimeInSeconds;
		this._previousIntervalInSeconds = interval;
		if (this._longestIntervalInSeconds < interval) this._longestIntervalInSeconds = interval;
		if (useLastInterval == 0) this._intervals++;
	}

	private void rememberStartTime(LocalDateTime start) {
		if ((this._firstStartTime == null) || (this._firstStartTime.isAfter(start))) this._firstStartTime = start;
	}

	private void rememberStopTime(LocalDateTime stop) {
		this._previousStopTime = stop;
		if ((this._lastStopTime == null) || (this._lastStopTime.isBefore(stop))) this._lastStopTime = stop;
	}

	LocalDateTime getToday() { 		
		resetIfNewDay();
		return this._today; 
	}
	long getPlayTimeTodayInSeconds() { 
		resetIfNewDay();
		return this._playTimeTodayInSeconds;
	}

	private void resetIfNewDay() {
		if (!isSameDay(this._today, LocalDateTime.now())) {
			this._today = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
			this._playTimeTodayInSeconds = 0;
		}
	}
	Object getIntervals() { return this._intervals; }


	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("firstStart", TimeMisc.fromLocalDateTime(this._firstStartTime));
		json.put("lastStop", TimeMisc.fromLocalDateTime(this._lastStopTime));
		json.put("totalPlayTimeInSeconds", this._totalPlayTimeInSeconds);
		json.put("intervals", this._intervals);
		json.put("longestIntervalInSeconds", this._longestIntervalInSeconds);
		json.put("playTimeTodayInSeconds", this.getPlayTimeTodayInSeconds());
		json.put("today", TimeMisc.fromLocalDateTime(this.getToday()));
		return json;
	}

	@Override
	public TimeStatisticsOld fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		if (jsonObject == null) return null;
		this._firstStartTime = TimeMisc.toLocalDateTime((String)jsonObject.get("firstStart"));
		this._lastStopTime = TimeMisc.toLocalDateTime((String)jsonObject.get("lastStop"));
		this._totalPlayTimeInSeconds = (long)jsonObject.get("totalPlayTimeInSeconds");
		this._intervals = (long)jsonObject.get("intervals");
		this._longestIntervalInSeconds = (long)jsonObject.get("longestIntervalInSeconds");
		final Object seconds = jsonObject.get("playTimeTodayInSeconds");
		if (seconds == null) this._playTimeTodayInSeconds = 0;
		else this._playTimeTodayInSeconds= (long)seconds;
		this._today = TimeMisc.toLocalDateTime((String)jsonObject.get("today"));
		return this;
	}

	@Override
	public TimeStatisticsOld factory() {
		return new TimeStatisticsOld();
	}

	static TimeStatisticsOld getFromJson(Object json) {
		TimeStatisticsOld info = new TimeStatisticsOld();
		return info.fromJson(json);
	}
}
