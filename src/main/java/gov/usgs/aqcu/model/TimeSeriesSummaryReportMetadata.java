package gov.usgs.aqcu.model;

import java.util.HashMap;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;

import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;

public class TimeSeriesSummaryReportMetadata extends ReportMetadata {
	private TimeSeriesSummaryRequestParameters requestParameters;
	private String primaryParameter;
	private String primaryTimeSeriesIdentifier;
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
	
	public void setRequestParameters(TimeSeriesSummaryRequestParameters val) {
		requestParameters = val;
		setStartDate(val.getStartInstant());
		setEndDate(val.getEndInstant());
		setPrimaryTimeSeriesIdentifier(val.getPrimaryTimeseriesIdentifier());
	}

	public void setGradeMetadata(Map<String, GradeMetadata> val) {
		gradeMetadata = val;
	}
}