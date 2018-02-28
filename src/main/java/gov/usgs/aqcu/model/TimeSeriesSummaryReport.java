package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesThreshold;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;

public class TimeSeriesSummaryReport {	
	private TimeSeriesDataServiceResponse primaryTsData;
	private TimeSeriesDescription upchainTs;
	private TimeSeriesDescription downchainTs;
	private TimeSeriesDescription primaryTsMetadata;
	private TimeSeriesSummaryCorrections corrections;
	private TimeSeriesSummaryMetadata reportMetadata;
	private List<RatingCurve> ratingCurves;
	private List<TimeSeriesSummaryRatingShift> ratingShifts;
	
	
	public TimeSeriesSummaryReport() {
		primaryTsData = new TimeSeriesDataServiceResponse();
		upchainTs = new TimeSeriesDescription();
		downchainTs = new TimeSeriesDescription();
		primaryTsMetadata = new TimeSeriesDescription();
		corrections = new TimeSeriesSummaryCorrections();
		reportMetadata = new TimeSeriesSummaryMetadata();
		ratingCurves = new ArrayList<>();
		ratingShifts = new ArrayList<>();
	}
	
	public TimeSeriesSummaryMetadata getReportMetadata() {
		return reportMetadata;
	}
	
	public TimeSeriesSummaryCorrections getCorrections() {
		return corrections;
	}
	
	public TimeSeriesDataServiceResponse getPrimaryTsData() {
		return primaryTsData;
	}
	
	public TimeSeriesDescription getUpchainTs() {
		return upchainTs;
	}
	
	public TimeSeriesDescription getDownchainTs() {
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
	
	public void setReportMetadata(TimeSeriesSummaryMetadata val) {
		reportMetadata = val;
	}
	
	public void setCorrections(TimeSeriesSummaryCorrections val) {
		corrections = val;
	}
	
	public void setPrimaryTsData(TimeSeriesDataServiceResponse val) {
		primaryTsData = val;
	}
	
	public void setUpchainTs(TimeSeriesDescription val) {
		upchainTs = val;
	}
	
	public void setDownchainTs(TimeSeriesDescription val) {
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
	
