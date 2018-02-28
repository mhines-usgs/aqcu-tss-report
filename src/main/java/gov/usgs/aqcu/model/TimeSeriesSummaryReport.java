package gov.usgs.aqcu.model;

import java.util.List;

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
	private List<TimeSeriesThreshold> thresholds;
	private List<RatingCurve> ratingCurves;
	private List<TimeSeriesSummaryRatingShift> ratingShifts;
	
	
	public TimeSeriesSummaryReport() {
		
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
	
	public List<TimeSeriesThreshold> getThresholds() {
		return thresholds;
	}
	
	public List<RatingCurve> getRatingCurves() {
		return ratingCurves;
	}
	
	public List<TimeSeriesSummaryRatingShift> getRatingShifts() {
		return ratingShifts;
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
	
	public void setThresholds(List<TimeSeriesThreshold> val) {
		thresholds = val;
	}
	
	public void setRatingCurves(List<RatingCurve> value) {
		ratingCurves = value;
	}
	
	public void setRatingShifts(List<TimeSeriesSummaryRatingShift> value) {
		ratingShifts = value;
	}
}
	
