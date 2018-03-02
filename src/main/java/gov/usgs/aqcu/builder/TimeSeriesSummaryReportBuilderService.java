package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.Instant;
import com.google.gson.Gson;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.model.TimeSeriesSummaryReport;
import gov.usgs.aqcu.model.TimeSeriesSummaryCorrections;
import gov.usgs.aqcu.model.TimeSeriesSummaryRatingShift;
import gov.usgs.aqcu.model.TimeSeriesSummaryMetadata;

import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;

@Component
public class TimeSeriesSummaryReportBuilderService {	
	private final String BASE_URL = "http://temp/report/";
	private TimeSeriesMetadataService timeSeriesMetadataService;
	private RatingCurveListService ratingCurveListService;
	private UpchainProcessorListService upchainProcessorListService;
	private LocationDescriptionListService locationDescriptionListService;
	
	@Autowired
	public TimeSeriesSummaryReportBuilderService(
		TimeSeriesMetadataService timeSeriesMetadataService, 
		UpchainProcessorListService upchainProcessorListService, 
		RatingCurveListService ratingCurveListService,
		LocationDescriptionListService locationDescriptionListService) {
		this.timeSeriesMetadataService = timeSeriesMetadataService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.locationDescriptionListService = locationDescriptionListService;
	}

	public TimeSeriesSummaryReport buildReport( 
		String primaryTimeseriesIdentifier,
		List<String> excludedCorrections,
		Instant startDate,
		Instant endDate,
		String requestingUser) throws Exception {
		//Fetch Time Series Descriptions
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse = timeSeriesMetadataService.get(primaryTimeseriesIdentifier);
		TimeSeriesDescription primaryDescription = metadataResponse.getTimeSeriesDescriptions().get(0);
		
		//Fetch Location Descriptions
		LocationDescriptionListServiceResponse locationResponse = locationDescriptionListService.get(primaryDescription.getLocationIdentifier());
		LocationDescription locationDescription = locationResponse.getLocationDescriptions().get(0);	
	
		//Fetch Upchain Processors
		ProcessorListServiceResponse processorsResponse = upchainProcessorListService.get(primaryTimeseriesIdentifier, startDate, endDate);

		//Fetch Rating Curves IFF we got at least one upchain processor to pull the rating model identifier from
		List<RatingCurve> ratingCurveList = null;
		if(processorsResponse != null && processorsResponse.getProcessors() != null && processorsResponse.getProcessors().size() > 0) {
			RatingCurveListServiceResponse ratingCurvesResponse = ratingCurveListService.get(processorsResponse.getProcessors().get(0).getInputRatingModelIdentifier(), null, startDate, endDate);
			ratingCurveList = ratingCurvesResponse.getRatingCurves();
		}
		
		TimeSeriesSummaryReport report = createReport(null, primaryDescription, null, null, locationDescription, null, ratingCurveList, startDate, endDate, requestingUser);
		
		return report;
	}
	
	private TimeSeriesSummaryReport createReport (
		TimeSeriesDataServiceResponse primaryData,
		TimeSeriesDescription primaryDescription,
		TimeSeriesDescription upchainDescription,
		TimeSeriesDescription downchainDescription,
		LocationDescription locationDescription,
		TimeSeriesSummaryCorrections corrections,
		List<RatingCurve> ratingCurveList,
		Instant startDate,
		Instant endDate,
		String requestingUser) {			
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
			
			//Add Report Metadata
			report.setReportMetadata(createTimeSeriesSummaryMetadata(primaryDescription, locationDescription, startDate, endDate, requestingUser));
			
			//Add Primary TS Data
			report.setPrimaryTsData(primaryData);
			
			//Add Primary TS Metadata
			report.setPrimaryTsMetadata(primaryDescription);
			
			//Add Upchain TS Metadata
			report.setUpchainTs(upchainDescription);
			
			//Add Downchain TS Metadata
			report.setDownchainTs(downchainDescription);
			
			//Add Corrections
			report.setCorrections(corrections);
			
			//Add Rating Curve Data - If Applicable 
			if(ratingCurveList != null && ratingCurveList.size() > 0) {
				report.setRatingCurves(ratingCurveList);
				report.setRatingShifts(createTimeSeriesSummaryRatingShifts(ratingCurveList));
			}
			
			return report;
	}
	
	private TimeSeriesSummaryMetadata createTimeSeriesSummaryMetadata(
		TimeSeriesDescription primaryDescription,
		LocationDescription locationDescription,
		Instant startDate,
		Instant endDate,
		String requestingUser) {
		TimeSeriesSummaryMetadata metadata = new TimeSeriesSummaryMetadata();
		
		metadata.setRequestingUser(requestingUser);
		metadata.setTimezone("Etc/GMT+" + (int)(-1 * primaryDescription.getUtcOffset()));
		metadata.setStartDate(startDate);
		metadata.setEndDate(endDate);
		metadata.setTitle("Time Series Summary");
		metadata.setPrimaryParameter(primaryDescription.getIdentifier());
		metadata.setStationName(locationDescription.getName());
		metadata.setStationId(locationDescription.getIdentifier());
		
		return metadata;
	}

	private List<TimeSeriesSummaryRatingShift> createTimeSeriesSummaryRatingShifts(List<RatingCurve> ratingCurveList) {
		List<TimeSeriesSummaryRatingShift> ratingShifts = new ArrayList<>();
		
		for(RatingCurve curve : ratingCurveList) {
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
	
