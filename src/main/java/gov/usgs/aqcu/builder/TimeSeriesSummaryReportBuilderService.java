package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Component
public class TimeSeriesSummaryReportBuilderService {	
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesSummaryReportBuilderService.class);
	private final String BASE_URL = "http://temp/report/";
	private Gson gson;
	private TimeSeriesDataCorrectedService timeSeriesDataCorrectedService;
	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private RatingCurveListService ratingCurveListService;
	private UpchainProcessorListService upchainProcessorListService;
	private DownchainProcessorListService downchainProcessorListService;
	private LocationDescriptionListService locationDescriptionListService;
	
	@Autowired
	public TimeSeriesSummaryReportBuilderService(
		TimeSeriesDataCorrectedService timeSeriesDataCorrectedService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		UpchainProcessorListService upchainProcessorListService,
		DownchainProcessorListService downchainProcessorListService,
		RatingCurveListService ratingCurveListService,
		LocationDescriptionListService locationDescriptionListService,
		Gson gson) {
			
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.gson = gson;
	}

	public TimeSeriesSummaryReport buildReport( 
		String primaryTimeseriesIdentifier,
		List<String> excludedCorrections,
		Instant startDate,
		Instant endDate,
		String requestingUser) throws Exception {
	
		//Fetch Upchain Processors
		ProcessorListServiceResponse upchainProcessorsResponse = upchainProcessorListService.get(primaryTimeseriesIdentifier, startDate, endDate);
		List<Processor> upchainProcessorList = upchainProcessorsResponse.getProcessors();
		List<String> upchainIdentifierList = new ArrayList<>();
		List<TimeSeriesDescription> upchainDescriptions = new ArrayList<>();
		for(Processor proc : upchainProcessorList) {
			upchainIdentifierList.addAll(proc.getInputTimeSeriesUniqueIds());
		}
		
		//Fetch Downchain Processors
		ProcessorListServiceResponse downchainProcessorsResponse = downchainProcessorListService.get(primaryTimeseriesIdentifier, startDate, endDate);
		List<Processor> downchainProcessorList = downchainProcessorsResponse.getProcessors();
		List<String> downchainIdentifierList = new ArrayList<>();
		List<TimeSeriesDescription> downchainDescriptions = new ArrayList<>();
		for(Processor proc : downchainProcessorList) {
			downchainIdentifierList.addAll(proc.getInputTimeSeriesUniqueIds());
		}
		
		//Fetch Timeseries Metadata
		ArrayList<String> timeseriesIdentifiers = new ArrayList<>();
		timeseriesIdentifiers.add(primaryTimeseriesIdentifier);
		timeseriesIdentifiers.addAll(upchainIdentifierList);
		timeseriesIdentifiers.addAll(downchainIdentifierList);

		//Parse Descriptions
		TimeSeriesDescription primaryDescription = null;
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse = timeSeriesDescriptionListService.get(timeseriesIdentifiers);
		for(TimeSeriesDescription desc : metadataResponse.getTimeSeriesDescriptions()) {
			if(desc.getIdentifier() == primaryTimeseriesIdentifier) {
				primaryDescription = desc;
			} else if(upchainIdentifierList.contains(desc.getIdentifier())) {
				upchainDescriptions.add(desc);
			} else if(downchainIdentifierList.contains(desc.getIdentifier())) {
				downchainDescriptions.add(desc);
			} else {
				LOG.error("Unknown Time Series Description returned from description list request: " + desc.getIdentifier());
			}
		}
		
		if(primaryDescription == null || upchainIdentifierList.size() != upchainDescriptions.size() || downchainIdentifierList.size() != downchainDescriptions.size()) {
			String errorString = "Failed to fetch descriptions for all requested Time Series Identifiers: \nRequested: " + 
				timeseriesIdentifiers.size() + "(" + String.join(",", timeseriesIdentifiers) + ")\nRecieved: " + metadataResponse.getTimeSeriesDescriptions().size();
			LOG.error(errorString);
			//TODO: Change to more specific exception
			throw new Exception(errorString);
		}
		
		//Fetch Location Descriptions
		LocationDescriptionListServiceResponse locationResponse = locationDescriptionListService.get(primaryDescription.getLocationIdentifier());
		LocationDescription locationDescription = locationResponse.getLocationDescriptions().get(0);	

		//Fetch Rating Curves IFF we got at least one upchain processor to pull the rating model identifier from
		List<RatingCurve> ratingCurveList = null;
		if(upchainProcessorList.size() > 0 && upchainProcessorList.get(0).getInputRatingModelIdentifier() != null && upchainProcessorList.get(0).getInputRatingModelIdentifier().length() > 0) {
			RatingCurveListServiceResponse ratingCurvesResponse = ratingCurveListService.get(upchainProcessorList.get(0).getInputRatingModelIdentifier(), null, startDate, endDate);
			ratingCurveList = ratingCurvesResponse.getRatingCurves();
		}
		
		//Fetch Primary Series Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataCorrectedService.get(primaryTimeseriesIdentifier, startDate, endDate);
		
		TimeSeriesSummaryReport report = createReport(dataResponse, primaryDescription, upchainDescriptions, downchainDescriptions, locationDescription, upchainProcessorList, null, ratingCurveList, startDate, endDate, requestingUser);
		
		return report;
	}
	
	private TimeSeriesSummaryReport createReport (
		TimeSeriesDataServiceResponse primaryDataResponse,
		TimeSeriesDescription primaryDescription,
		List<TimeSeriesDescription> upchainDescriptions,
		List<TimeSeriesDescription> downchainDescriptions,
		LocationDescription locationDescription,
		List<Processor> upchainProcessorList,
		List<Correction> correctionsList,
		List<RatingCurve> ratingCurveList,
		Instant startDate,
		Instant endDate,
		String requestingUser) {			
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
			
			//Add Report Metadata
			report.setReportMetadata(createTimeSeriesSummaryMetadata(primaryDescription, locationDescription, startDate, endDate, requestingUser));
			
			//Add Primary TS Data
			report.setPrimaryTsData(createTimeSeriesSummaryCorrectedData(primaryDataResponse, upchainProcessorList, startDate, endDate));
			
			//Add Primary TS Metadata
			report.setPrimaryTsMetadata(primaryDescription);
			
			//Add Upchain TS Metadata
			if(upchainDescriptions != null && upchainDescriptions.size() > 0) {
				report.setUpchainTs(createTimeSeriesSummaryRelatedSeriesList(upchainDescriptions));
			}

			//Add Downchain TS Metadata
			if(downchainDescriptions != null && downchainDescriptions.size() > 0) {
				report.setDownchainTs(createTimeSeriesSummaryRelatedSeriesList(downchainDescriptions));
			}

			//Add Corrections
			if(correctionsList!= null && correctionsList.size() > 0) {
				report.setCorrections(createTimeSeriesSummaryCorrections(correctionsList));
			}
			
			
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
	
	private List<TimeSeriesSummaryRelatedSeries> createTimeSeriesSummaryRelatedSeriesList(List<TimeSeriesDescription> descriptions) {
		List<TimeSeriesSummaryRelatedSeries> series = new ArrayList<>();
		
		for(TimeSeriesDescription desc : descriptions) {
			TimeSeriesSummaryRelatedSeries newSeries = new TimeSeriesSummaryRelatedSeries();
			newSeries.setIdentifier(desc.getIdentifier());
			newSeries.setUrl(createReportURL("time-series-summary", new HashMap<String,String>()));
			series.add(newSeries);
		}
		
		return series;
	}
	
	private TimeSeriesSummaryCorrectedData createTimeSeriesSummaryCorrectedData(TimeSeriesDataServiceResponse response, List<Processor> upchainProcessorList, Instant startDate, Instant endDate) {
		TimeSeriesSummaryCorrectedData data = new TimeSeriesSummaryCorrectedData();
		
		//Copy Desired Fields
		data.setApprovals(response.getApprovals());
		data.setQualifiers(response.getQualifiers());
		data.setNotes(response.getNotes());
		data.setMethods(response.getMethods());
		data.setGrades(response.getGrades());
		data.setGapTolerances(response.getGapTolerances());
		data.setInterpolationTypes(response.getInterpolationTypes());
		
		data.setProcessors(upchainProcessorList);
		
		//Calculate Gaps
		
		
		return data;
	}
	
	public TimeSeriesSummaryCorrections createTimeSeriesSummaryCorrections(List<Correction> rawCorrections) {
		TimeSeriesSummaryCorrections corrections = new TimeSeriesSummaryCorrections();
		List<Correction> pre = new ArrayList<>();
		List<Correction> normal = new ArrayList<>();
		List<Correction> post = new ArrayList<>();
		
		for(Correction corr : rawCorrections) {
			if(corr.getProcessingOrder() == CorrectionProcessingOrder.PreProcessing) {
				pre.add(corr);
			} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.Normal) {
				normal.add(corr);
			} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.PostProcessing) {
				post.add(corr);
			}
		}
		
		corrections.setPreProcessing(pre);
		corrections.setNormal(normal);
		corrections.setPostProcessing(post);
		
		return corrections;
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
	
