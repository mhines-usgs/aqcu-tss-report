package gov.usgs.aqcu.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.usgs.aqcu.exception.AqcuReportParameterException;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.serializers.InstantDeserializer;

public abstract class AqcuTimeUtils {
	private static final Logger LOG = LoggerFactory.getLogger(AqcuTimeUtils.class);
	public static final Instant OPEN_ENDED_START_THRESHOLD = InstantDeserializer.MinConcreteValue;
	public static final Instant OPEN_ENDED_END_THRESHOLD = InstantDeserializer.MaxConcreteValue;

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

	public static Pair<Instant,Instant> getPeportPeriodFromParams(Integer waterYear, Integer lastMonths, LocalDate startDate, LocalDate endDate) throws AqcuReportParameterException {
		Pair<Instant,Instant> reportPeriod;
		
		if(lastMonths != null) {
			reportPeriod = lastMonthsToReportPeriod(lastMonths);
		} else if(waterYear != null) {
			reportPeriod = waterYearToReportPeriod(waterYear);
		} else if(startDate != null && endDate != null) {
			reportPeriod = new ImmutablePair<Instant,Instant>(dateToReportStartTime(startDate), dateToReportEndTime(endDate));
		} else {
			String errorString = "Missing information required to build report time period. Must include at least one of: [lastMonths, waterYear, {startDate, endDate}].";
			LOG.error(errorString);
			throw new AqcuReportParameterException(errorString);
		}

		//Validate Parsed Report Period
		//Start before or equal to End
		if(reportPeriod.getKey().isAfter(reportPeriod.getValue())) {
			LOG.error("The report period start date must be before or equal to the report period end date.");
			throw new AqcuReportParameterException("The report period start date must be before or equal to the report period end date.");
		}

		//Min and Max Times
		if(reportPeriod.getKey().isBefore(OPEN_ENDED_START_THRESHOLD) || reportPeriod.getKey().isAfter(OPEN_ENDED_END_THRESHOLD) || 
			reportPeriod.getValue().isBefore(OPEN_ENDED_START_THRESHOLD) || reportPeriod.getValue().isAfter(OPEN_ENDED_END_THRESHOLD)) {
			LOG.error("Provided report time period is outside of the allowed range.");
			throw new AqcuReportParameterException("Provided report time period is outside of the allowed range.");
		}

		return reportPeriod;
	}

	private static Pair<Instant,Instant> waterYearToReportPeriod(Integer waterYear) {
		Instant reportStartTime = Instant.from(LocalDateTime.of(waterYear-1,10,1,0,0,0).toInstant(ZoneOffset.UTC));
		Instant reportEndTime = Instant.from(LocalDateTime.of(waterYear,9,30,23,59,59,999999999).toInstant(ZoneOffset.UTC));

		return new ImmutablePair<Instant,Instant>(reportStartTime, reportEndTime);
	}

	private static Pair<Instant,Instant> lastMonthsToReportPeriod(Integer lastMonths) {
		LocalDate nowDate = LocalDate.now().minusMonths(lastMonths);
		Instant reportStartTime = LocalDateTime.of(nowDate.getYear(), nowDate.getMonth(), 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
		Instant reportEndTime = LocalDate.now().atTime(23,59,59,999999999).toInstant(ZoneOffset.UTC);

		return new ImmutablePair<Instant,Instant>(reportStartTime, reportEndTime);
	}

	private static Instant dateToReportStartTime(LocalDate startDate) {
		Instant startTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		return startTime;
	}

	public static Instant dateToReportEndTime(LocalDate endDate) {
		Instant endTime = endDate.atTime(23,59,59,999999999).toInstant(ZoneOffset.UTC);
		return endTime;
	}
}