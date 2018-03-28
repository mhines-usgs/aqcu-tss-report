package gov.usgs.aqcu.parameter;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TimeSeriesSummaryRequestParametersTest {

	Instant reportEndInstant = Instant.parse("2018-03-16T23:59:59.999999999Z");
	Instant reportStartInstant = Instant.parse("2018-03-16T00:00:00.00Z");
	LocalDate reportEndDate = LocalDate.of(2018, 03, 16);
	LocalDate reportStartDate = LocalDate.of(2018, 03, 16);
    String primaryIdentifier = "test-identifier";
    List<String> excludedCorrs = Arrays.asList("corr1", "corr2", "corr3");

    @Test
	public void getAsQueryStringTest() {
        TimeSeriesSummaryRequestParameters params = new TimeSeriesSummaryRequestParameters();
		params.setEndDate(reportEndDate);
		params.setStartDate(reportStartDate);
		params.setPrimaryTimeseriesIdentifier(primaryIdentifier);
        params.determineReportPeriod();
        params.setExcludedCorrections(excludedCorrs);
        String expected = "startDate=2018-03-16&endDate=2018-03-16&primaryTimeseriesIdentifier=test-identifier&excludedCorrections=corr1,corr2,corr3";
		assertEquals(0, params.getAsQueryString(null, false).compareTo(expected));
	}
}