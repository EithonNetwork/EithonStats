package net.eithon.plugin.stats.logic;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import net.eithon.library.json.JsonObject;
import net.eithon.library.time.TimeMisc;

import org.json.simple.JSONObject;

public class TimeStatisticsOld extends JsonObject<TimeStatisticsOld>{
	// Saved variables
	private LocalDateTime _firstStartTime;
	private LocalDateTime _lastStopTime;
	private long _totalPlayTimeInSeconds;
	private long _intervals;
	private long _longestIntervalInSeconds;
	private LocalDateTime _today;
	private long _playTimeTodayInSeconds;

	// Non-saved, internal variables
	private long _previousIntervalInSeconds;
	private LocalDateTime _previousStartTime;
	private LocalDateTime _previousStopTime;

	public TimeStatisticsOld()
	{
		this._previousStartTime = null;
		this._previousIntervalInSeconds = 0;
		resetTotalPlayTime();
	}

	public static TimeStatisticsOld getDifference(TimeStatisticsOld now, TimeStatisticsOld then) {
		TimeStatisticsOld diff = new TimeStatisticsOld();
		diff._firstStartTime = now._firstStartTime;
		diff._intervals = now._intervals - ((then == null) ? 0 : then._intervals);
		diff._lastStopTime = now._lastStopTime;
		diff._longestIntervalInSeconds = now._longestIntervalInSeconds;
		diff._previousIntervalInSeconds = now._previousIntervalInSeconds;
		diff._previousStartTime = now._previousStartTime;
		diff._previousStopTime = now._previousStopTime;
		diff._today = now.getToday();
		diff._playTimeTodayInSeconds = now.getPlayTimeTodayInSeconds();
		diff._totalPlayTimeInSeconds = now._totalPlayTimeInSeconds - ((then == null) ? 0 : then._totalPlayTimeInSeconds);
		return diff;
	}

	public void addInterval(LocalDateTime start, LocalDateTime stop) {
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

	public long addToTotalPlayTime(long playTimeInSeconds) {
		this._totalPlayTimeInSeconds += playTimeInSeconds;
		if (this._totalPlayTimeInSeconds < 0) resetTotalPlayTime();
		return this._totalPlayTimeInSeconds;
	}

	public void resetTotalPlayTime() {
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
		this._previousStartTime = start;
		if ((this._firstStartTime == null) || (this._firstStartTime.isAfter(start))) this._firstStartTime = start;
	}

	private void rememberStopTime(LocalDateTime stop) {
		this._previousStopTime = stop;
		if ((this._lastStopTime == null) || (this._lastStopTime.isBefore(stop))) this._lastStopTime = stop;
	}

	public LocalDateTime getPreviousStartTime() { return this._previousStartTime; }
	LocalDateTime getToday() { 		
		resetIfNewDay();
		return this._today; 
	}
	public LocalDateTime getPreviousStopTime() { return this._previousStopTime; }
	public long getPreviousIntervalInSeconds() { return this._previousIntervalInSeconds; }
	public long getTotalPlayTimeInSeconds() { return this._totalPlayTimeInSeconds; }
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

	public static TimeStatisticsOld getFromJson(Object json) {
		TimeStatisticsOld info = new TimeStatisticsOld();
		return info.fromJson(json);
	}
}
