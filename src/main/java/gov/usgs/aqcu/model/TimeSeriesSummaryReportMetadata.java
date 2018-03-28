package gov.usgs.aqcu.model;

import java.util.HashMap;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;

import gov.usgs.aqcu.parameter.RequestParameters;

public class TimeSeriesSummaryReportMetadata extends ReportMetadata {
	private Map<String, GradeMetadata> gradeMetadata;

	public TimeSeriesSummaryReportMetadata() {
		super();
		gradeMetadata = new HashMap<>();
	}

	public TimeSeriesSummaryReportMetadata(
		String reportType,
		String reportTitle,
		RequestParameters requestParameters,
		String requestingUser,
		String primaryParameter,
		Double utcOffset,
		String stationName,
		String stationId,
		Map<String,QualifierMetadata> qualifierMetadata,
		Map<String,GradeMetadata> gradeMetadata) {
		super(reportType, reportTitle, requestParameters, requestingUser, primaryParameter, utcOffset, stationName, stationId, qualifierMetadata);
		setGradeMetadata(gradeMetadata);
	}

	public Map<String, GradeMetadata> getGradeMetadata() {
		return gradeMetadata;
	}

	public void setGradeMetadata(Map<String, GradeMetadata> val) {
		gradeMetadata = val;
	}
}