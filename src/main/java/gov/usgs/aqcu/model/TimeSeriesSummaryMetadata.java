package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.math.BigDecimal;


public class TimeSeriesSummaryMetadata {	
	
	private String requestingUser;
	private String timezone;
	private Instant startDate;
	private Instant endDate;
	private String title;
	private String primaryParameter;
	private String stationName;
	private String stationId;

	public String getRequestingUser() {
		return requestingUser;
	}
	
	public String getTimezone() {
		return timezone;
	}
	
	public Instant getStartDate() {
		return startDate;
	}
	
	public Instant getEndDate() {
		return endDate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getPrimaryParameter() {
		return primaryParameter;
	}
	
	public String getStationName() {
		return stationName;
	}
	
	public String getStationId() {
		return stationId;
	}
	
	public void setRequestingUser(String val) {
		requestingUser = val;
	}
	
	public void setTimezone(String val) {
		timezone = val;
	}
	
	public void setStartDate(Instant val) {
		startDate = val;
	}
	
	public void setEndDate(Instant val) {
		endDate = val;
	}
	
	public void setTitle(String val) {
		title = val;
	}
	
	public void setPrimaryParameter(String val) {
		primaryParameter = val;
	}
	
	public void setStationName(String val) {
		stationName = val;
	}
	
	public void setStationId(String val) {
		stationId = val;
	}
}
	
