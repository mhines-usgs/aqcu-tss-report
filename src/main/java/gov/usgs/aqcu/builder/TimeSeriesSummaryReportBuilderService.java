package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;

import gov.usgs.aqcu.model.TimeSeriesSummaryReport;
import gov.usgs.aqcu.model.TimeSeriesSummaryCorrections;
import gov.usgs.aqcu.model.TimeSeriesSummaryRatingShift;


@Component
public class TimeSeriesSummaryReportBuilderService {	
	public TimeSeriesSummaryReport buildTimeSeriesSummaryReport (
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse,
		RatingCurveListServiceResponse ratingCurvesResponse) {			
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
			
			if(ratingCurvesResponse != null && ratingCurvesResponse.getRatingCurves() != null && ratingCurvesResponse.getRatingCurves().size() > 0) {
				report.setRatingShifts(createTimeSeriesSummaryRatingShifts(ratingCurvesResponse));
				report.setRatingCurves(ratingCurvesResponse.getRatingCurves());
			}
			
			return report;
	}

	private List<TimeSeriesSummaryRatingShift> createTimeSeriesSummaryRatingShifts(RatingCurveListServiceResponse ratingCurvesResponse) {
		List<TimeSeriesSummaryRatingShift> ratingShifts = new ArrayList<>();
		
		for(RatingCurve curve : ratingCurvesResponse.getRatingCurves()) {
			for(RatingShift shift : curve.getShifts()) {
				TimeSeriesSummaryRatingShift newShift = new TimeSeriesSummaryRatingShift(shift, curve.getId());
				ratingShifts.add(newShift);
			}
		}
		
		return ratingShifts;
	}		
}
	
