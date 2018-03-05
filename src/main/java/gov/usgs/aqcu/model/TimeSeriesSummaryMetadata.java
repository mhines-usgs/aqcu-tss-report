package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.math.BigDecimal;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;

public class TimeSeriesSummaryMetadata {	
	
	private String requestingUser;
	private String timezone;
	private Instant startDate;
	private Instant endDate;
	private String title;
	private String primaryParameter;
	private String stationName;
	private String stationId;
	private Map<String, GradeMetadata> gradeMetadata;
	private Map<String, QualifierMetadata> qualifierMetadata;

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
	
	public Map<String, GradeMetadata> getGradeMetadata() {
		return gradeMetadata;
	}
	
	public Map<String, QualifierMetadata> getQualifierMetadata() {
		return qualifierMetadata;
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
	
	public void setGradeMetadata(Map<String, GradeMetadata> val) {
		gradeMetadata = val;
	}
	
	public void setQualifierMetadata(Map<String, QualifierMetadata> val) {
		qualifierMetadata = val;
	}
	
	public void setGradeMetadata(List<GradeMetadata> metadataList) {
		Map<String, GradeMetadata> map = new HashMap<>();
		
		for(GradeMetadata metadata : metadataList) {
			map.put(metadata.getIdentifier(), metadata);
		}
		
		gradeMetadata = map;
	}
	
	public void setQualifierMetadata(List<QualifierMetadata> metadataList) {
		Map<String, QualifierMetadata> map = new HashMap<>();
		
		for(QualifierMetadata metadata : metadataList) {
			map.put(metadata.getIdentifier(), metadata);
		}
		
		qualifierMetadata = map;
	}
}
	
