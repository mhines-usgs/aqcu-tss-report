package gov.usgs.aqcu.model;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;

public class TimeSeriesSummaryReportMetadata extends ReportMetadata {
	private TimeSeriesSummaryRequestParameters requestParameters;
	private String primaryParameter;
	private String primaryTimeSeriesIdentifier;
	private String requestingUser;
	private Map<String, GradeMetadata> gradeMetadata;

	public TimeSeriesSummaryReportMetadata() {
		super();
		gradeMetadata = new HashMap<>();
	}

	public String getPrimaryTimeSeriesIdentifier() {
		return primaryTimeSeriesIdentifier;
	}
	
	public String getPrimaryParameter() {
		return primaryParameter;
	}

	public String getRequestingUser() {
		return requestingUser;
	}

	public Map<String, GradeMetadata> getGradeMetadata() {
		return gradeMetadata;
	}
	
	public TimeSeriesSummaryRequestParameters getRequestParameters() {
		return requestParameters;
	}
	
	public void setPrimaryTimeSeriesIdentifier(String val) {
		primaryTimeSeriesIdentifier = val;
	}

	public void setPrimaryParameter(String val) {
		primaryParameter = val;
	}

	public void setRequestingUser(String val) {
		requestingUser = val;
	}
	
	public void setRequestParameters(TimeSeriesSummaryRequestParameters val) {
		requestParameters = val;
		//Report Period displayed should be exactly as recieved, so get as UTC
		setStartDate(val.getStartInstant(ZoneOffset.UTC));
		setEndDate(val.getEndInstant(ZoneOffset.UTC));
		setPrimaryTimeSeriesIdentifier(val.getPrimaryTimeseriesIdentifier());
	}

	public void setGradeMetadata(Map<String, GradeMetadata> val) {
		gradeMetadata = val;
	}
}