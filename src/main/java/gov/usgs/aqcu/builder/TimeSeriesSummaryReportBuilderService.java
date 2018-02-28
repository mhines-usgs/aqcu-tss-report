package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.Instant;

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
import gov.usgs.aqcu.model.TimeSeriesSummaryMetadata;


@Component
public class TimeSeriesSummaryReportBuilderService {	
	private final String BASE_URL = "http://temp/report/";

	public TimeSeriesSummaryReport buildTimeSeriesSummaryReport (
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse,
		RatingCurveListServiceResponse ratingCurvesResponse,
		Instant startDate,
		Instant endDate,
		String requestingUser) {			
		
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
			
			//Add Report Metadata
			report.setReportMetadata(createTimeSeriesSummaryMetadata(startDate, endDate, requestingUser));
			
			//Add Primary TS Metadata
			report.setPrimaryTsMetadata(metadataResponse.getTimeSeriesDescriptions().get(0));
			
			//Add Upchain TS Metadata
			
			//Add Downchain TS Metadata
			
			//Add Primary TS Data
			
			//Add Corrections
			
			//Add Rating Curve Data - If Applicable 
			if(ratingCurvesResponse != null && ratingCurvesResponse.getRatingCurves() != null && ratingCurvesResponse.getRatingCurves().size() > 0) {
				report.setRatingShifts(createTimeSeriesSummaryRatingShifts(ratingCurvesResponse));
				report.setRatingCurves(ratingCurvesResponse.getRatingCurves());
			}
			
			return report;
	}
	
	private TimeSeriesSummaryMetadata createTimeSeriesSummaryMetadata(
		Instant startDate,
		Instant endDate,
		String requestingUser) {
		TimeSeriesSummaryMetadata metadata = new TimeSeriesSummaryMetadata();
		
		metadata.setRequestingUser(requestingUser);
		metadata.setTimezone("Etc/GMT+5");
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);
		metadata.setTitle("Time Series Summary");
		metadata.setPrimaryParameter("primaryParmaeter");
		metadata.setStationName("stationName");
		metadata.setStationId("stationId");
		
		return metadata;
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
	
	private String createReportURL(String reportType, Map<String, String> parameters) {
		String reportUrl = BASE_URL + reportType + "?";
		
		for(Map.Entry<String, String> entry : parameters.entrySet()) {
			if(entry.getKey() != null && entry.getKey().length() > 0) {
				reportUrl += entry.getKey() + "=" + entry.getValue() + "&";
			}
		}
		
		return reportUrl.substring(0, reportUrl.length()-1);
	}
}
	
