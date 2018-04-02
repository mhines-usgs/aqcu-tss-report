package gov.usgs.aqcu.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;

public class TimeSeriesSummaryReportMetadataTest {
	TimeSeriesSummaryRequestParameters params = new TimeSeriesSummaryRequestParameters();

    @Before
    public void setup() {
		params.setStartDate(LocalDate.parse("2017-01-01"));
		params.setEndDate(LocalDate.parse("2017-02-01"));
		params.setPrimaryTimeseriesIdentifier("primary-id");
    }

    @Test
	public void setRequestParametersTest() {
	   TimeSeriesSummaryReportMetadata metadata = new TimeSeriesSummaryReportMetadata();
	   metadata.setRequestParameters(params);

	   assertEquals(metadata.getRequestParameters(), params);
	   assertEquals(metadata.getStartDate(), params.getStartInstant());
	   assertEquals(metadata.getEndDate(), params.getEndInstant());
	   assertEquals(metadata.getPrimaryTimeSeriesIdentifier(), params.getPrimaryTimeseriesIdentifier());
	}
}