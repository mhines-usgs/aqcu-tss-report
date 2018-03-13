package gov.usgs.aqcu.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;

public abstract class AqcuTimeUtils {
	public static final Instant OPEN_ENDED_START_THRESHOLD = Instant.parse("0000-01-01T00:00:00Z");
	public static final Instant OPEN_ENDED_END_THRESHOLD = Instant.parse("9999-12-31T00:00:00Z");

	public static boolean doPeriodsOverlap(PeriodOfApplicability p1, PeriodOfApplicability p2) {
		return doesTimeRangeOverlap(p1.getStartTime(), p1.getEndTime(), p2.getStartTime(), p2.getEndTime());
	}

	public static boolean doesTimeRangeOverlap(Instant start1, Instant end1, Instant start2, Instant end2) {
		return (start1.compareTo(end2) <= 0 && end1.compareTo(start2) >= 0);
	}

	public static boolean isOpenEndedTime(Instant time) {
		if(time.compareTo(OPEN_ENDED_START_THRESHOLD) <= 0 || time.compareTo(OPEN_ENDED_END_THRESHOLD) >= 0) {
			return true;
		}

		return false;
	}

	public static Instant toReportStartTime(LocalDate startDate) {
		Instant startTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		return startTime;
	}

	public static Instant toReportEndTime(LocalDate endDate) {
		Instant endTime = endDate.atTime(23,59,59,999999999).toInstant(ZoneOffset.UTC);
		return endTime;
	}
}