package gov.usgs.aqcu.model;

import java.time.Instant;
import java.math.BigDecimal;

public class TimeSeriesSummaryDataGap {
	private Instant startTime;
	private Instant endTime;
	private BigDecimal durationInHours;
	private String gapExtent;
	
	public Instant getStartTime() {
		return startTime;
	}
	
	public Instant getEndTime() {
		return endTime;
	}
	
	public String getGapExtent() {
		return gapExtent;
	}
	
	public BigDecimal getDurationInHours() {
		return durationInHours;
	}
	
	public void setStartTime(Instant val) {
		startTime= val;
	}
	
	public void setEndTime(Instant val) {
		endTime = val;
	}
	
	public void setGapExtent(String val) {
		gapExtent = val;
	}
	
	public void setDurationInHours(BigDecimal val) {
		durationInHours = val;
	}
}
