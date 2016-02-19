package net.eithon.plugin.stats.logic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.stats.db.Accumulated;

public class TimeStatistics {
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

	public TimeStatistics()
	{
		this._previousStartTime = null;
		this._previousIntervalInSeconds = 0;
		resetTotalPlayTime();
	}

	public static TimeStatistics getDifference(TimeStatistics now, TimeStatistics then) {
		TimeStatistics diff = new TimeStatistics();
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
	public long getIntervals() { return this._intervals; }

	public TimeStatistics fromDb(ResultSet resultSet) throws SQLException {
		if (resultSet == null) return null;
		this._firstStartTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("first_start"));
		this._lastStopTime = TimeMisc.toLocalDateTime(resultSet.getTimestamp("last_stop"));
		this._totalPlayTimeInSeconds = resultSet.getLong("play_time_in_seconds");
		this._intervals = resultSet.getLong("intervals");
		this._longestIntervalInSeconds = resultSet.getLong("longest_interval_in_seconds");
		this._playTimeTodayInSeconds = resultSet.getLong("play_time_today_in_seconds");
		this._today = TimeMisc.toLocalDateTime(resultSet.getTimestamp("today"));
		return this;
	}

	public static TimeStatistics getFromDb(ResultSet resultSet) throws SQLException {
		TimeStatistics info = new TimeStatistics();
		return info.fromDb(resultSet);
	}

	public String getDbUpdates() {
		String updates = String.format("first_start_time='%s'", TimeMisc.toDbUtc(this._firstStartTime)) +
				String.format(", last_stop_time='%s'", TimeMisc.toDbUtc(this._lastStopTime)) +
				String.format(", today='%s'", TimeMisc.toDbUtc(this._today)) +
				String.format(", play_time_in_seconds=%d", this._totalPlayTimeInSeconds) +
				String.format(", intervals=%d", this._intervals) +
				String.format(", longest_interval_in_seconds=%d", this._longestIntervalInSeconds) +
				String.format(", play_time_today_in_seconds=%d", this._playTimeTodayInSeconds);
		return updates;
	}

	public LocalDateTime getFirstStartTime() {return this._firstStartTime; }

	public LocalDateTime getLastStopTime() { return this._lastStopTime; }

	public void copyFromDbRecord(Accumulated dbRecord) {
		this._firstStartTime = dbRecord.get_firstStartTime();
		this._lastStopTime = dbRecord.get_lastStopTime();
		this._totalPlayTimeInSeconds = dbRecord.get_totalPlayTimeInSeconds();
		this._intervals = dbRecord.get_intervals();
		this._longestIntervalInSeconds = dbRecord.get_longestIntervalInSeconds();
		this._today = dbRecord.get_today();
		this._playTimeTodayInSeconds = dbRecord.get_playTimeTodayInSeconds();
		
	}
}
