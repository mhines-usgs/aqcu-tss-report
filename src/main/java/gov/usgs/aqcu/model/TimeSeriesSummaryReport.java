package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;

public class TimeSeriesSummaryReport {	
	private TimeSeriesSummaryCorrectedData primaryTsData;
	private List<TimeSeriesSummaryRelatedSeries> upchainTs;
	private List<TimeSeriesSummaryRelatedSeries> downchainTs;
	private TimeSeriesDescription primaryTsMetadata;
	private TimeSeriesSummaryCorrections corrections;
	private ReportMetadata reportMetadata;
	private List<RatingCurve> ratingCurves;
	private List<TimeSeriesSummaryRatingShift> ratingShifts;
	
	
	public TimeSeriesSummaryReport() {
		primaryTsData = new TimeSeriesSummaryCorrectedData();
		upchainTs = new ArrayList<>();
		downchainTs = new ArrayList<>();
		primaryTsMetadata = new TimeSeriesDescription();
		corrections = new TimeSeriesSummaryCorrections();
		reportMetadata = new ReportMetadata();
		ratingCurves = new ArrayList<>();
		ratingShifts = new ArrayList<>();
	}
	
	public ReportMetadata getReportMetadata() {
		return reportMetadata;
	}
	
	public TimeSeriesSummaryCorrections getCorrections() {
		return corrections;
	}
	
	public TimeSeriesSummaryCorrectedData getPrimaryTsData() {
		return primaryTsData;
	}
	
	public List<TimeSeriesSummaryRelatedSeries> getUpchainTs() {
		return upchainTs;
	}
	
	public List<TimeSeriesSummaryRelatedSeries> getDownchainTs() {
		return downchainTs;
	}
	
	public TimeSeriesDescription getPrimaryTsMetadata() {
		return primaryTsMetadata;
	}
	
	public List<RatingCurve> getRatingCurves() {
		return ratingCurves;
	}
	
	public List<TimeSeriesSummaryRatingShift> getRatingShifts() {
		return ratingShifts;
	}
	
	public void setReportMetadata(ReportMetadata val) {
		reportMetadata = val;
	}
	
	public void setCorrections(TimeSeriesSummaryCorrections val) {
		corrections = val;
	}
	
	public void setPrimaryTsData(TimeSeriesSummaryCorrectedData val) {
		primaryTsData = val;
	}
	
	public void setUpchainTs(List<TimeSeriesSummaryRelatedSeries> val) {
		upchainTs = val;
	}
	
	public void setDownchainTs(List<TimeSeriesSummaryRelatedSeries> val) {
		downchainTs = val;
	}
	
	public void setPrimaryTsMetadata(TimeSeriesDescription val) {
		primaryTsMetadata = val;
	}
	
	public void setRatingCurves(List<RatingCurve> value) {
		ratingCurves = value;
	}
	
	public void setRatingShifts(List<TimeSeriesSummaryRatingShift> value) {
		ratingShifts = value;
	}
}
	
