package gov.usgs.aqcu.model;

import java.time.Instant;

public class TimeSeriesMetadata {
	
	private String primaryTimeseriesIdentifier;
	private Instant startDate;
	private Instant endDate;
	
	public String getPrimaryTimeseriesIdentifier() {
		return primaryTimeseriesIdentifier;
	}
	
	public Instant getStartDate() {
		return startDate;
	}
	
	public Instant getEndDate() {
		return endDate;
	}
	
	public void setPrimaryTimeseriesIdentifier(String val) {
		primaryTimeseriesIdentifier = val;
	}
	
	public void setStartDate(Instant val) {
		startDate = val;
	}
	
	public void setEndDate(Instant val) {
		endDate = val;
	}
}
