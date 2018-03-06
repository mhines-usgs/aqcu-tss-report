package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.time.Instant;
import java.lang.Math;
import java.time.Duration;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GapTolerance;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierListServiceResponse;

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
	private CorrectionListService correctionListService;
	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	
	
	@Autowired
	public TimeSeriesSummaryReportBuilderService(
		TimeSeriesDataCorrectedService timeSeriesDataCorrectedService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		UpchainProcessorListService upchainProcessorListService,
		DownchainProcessorListService downchainProcessorListService,
		RatingCurveListService ratingCurveListService,
		LocationDescriptionListService locationDescriptionListService,
		CorrectionListService correctionListService,
		GradeLookupService gradeLookupService,
		QualifierLookupService qualifierLookupService,
		Gson gson) {
			
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.correctionListService = correctionListService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
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
		Set<String> upchainIdentifiers = new LinkedHashSet<>();
		List<TimeSeriesDescription> upchainDescriptions = new ArrayList<>();
		for(Processor proc : upchainProcessorList) {
			upchainIdentifiers.addAll(proc.getInputTimeSeriesUniqueIds());
		}

		//Fetch Downchain Processors
		ProcessorListServiceResponse downchainProcessorsResponse = downchainProcessorListService.get(primaryTimeseriesIdentifier, startDate, endDate);
		List<Processor> downchainProcessorList = downchainProcessorsResponse.getProcessors();
		Set<String> downchainIdentifiers = new LinkedHashSet<>();
		List<TimeSeriesDescription> downchainDescriptions = new ArrayList<>();
		for(Processor proc : downchainProcessorList) {
			downchainIdentifiers.add(proc.getOutputTimeSeriesUniqueId());
		}

		//Fetch Timeseries Metadata
		Set<String> timeseriesIdentifiers = new LinkedHashSet<>();
		ArrayList<String> uniqueTimeseriesIdentifiers;
		timeseriesIdentifiers.add(primaryTimeseriesIdentifier);
		timeseriesIdentifiers.addAll(upchainIdentifiers);
		timeseriesIdentifiers.addAll(downchainIdentifiers);
		uniqueTimeseriesIdentifiers = new ArrayList<>(timeseriesIdentifiers);

		//Parse Descriptions
		TimeSeriesDescription primaryDescription = null;
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse = timeSeriesDescriptionListService.get(uniqueTimeseriesIdentifiers);
		for(TimeSeriesDescription desc : metadataResponse.getTimeSeriesDescriptions()) {
			if(desc.getUniqueId().equals(primaryTimeseriesIdentifier)) {
				primaryDescription = desc;
			} else if(upchainIdentifiers.contains(desc.getUniqueId())) {
				upchainDescriptions.add(desc);
			} else if(downchainIdentifiers.contains(desc.getUniqueId())) {
				downchainDescriptions.add(desc);
			} else {
				LOG.error("Unknown Time Series Description returned from description list request: " + desc.getUniqueId());
			}
		}

		//Validate descriptions
		if(primaryDescription == null || upchainIdentifiers.size() != upchainDescriptions.size() || downchainIdentifiers.size() != downchainDescriptions.size()) {
			String errorString = "Failed to fetch descriptions for all requested Time Series Identifiers: \nRequested: " + 
				uniqueTimeseriesIdentifiers.size() + "{\nPrimary: " + primaryTimeseriesIdentifier + "\nUpchain: " + String.join(",", upchainIdentifiers) + 
				"\nDownchain: " + String.join(",", downchainIdentifiers) + "}\nRecieved: "  + metadataResponse.getTimeSeriesDescriptions().size();
			LOG.error(errorString);
			//TODO: Change to more specific exception
			throw new Exception(errorString);
		}

		//Fetch Corrections
		CorrectionListServiceResponse correctionsResponse = correctionListService.get(primaryTimeseriesIdentifier, startDate, endDate);
		List<Correction> correctionList = correctionsResponse.getCorrections();

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
		
		//Calculate Gaps
		List<TimeSeriesSummaryDataGap> gapList = createTimeSeriesSummaryDataGaps(dataResponse.getPoints(), dataResponse.getGapTolerances(), primaryDescription.getRawStartTime(), primaryDescription.getRawEndTime(), startDate, endDate);

		//Additional Metadata Lookups
		List<GradeMetadata> gradeMetadataList = gradeLookupService.getByGradeList(dataResponse.getGrades());
		List<QualifierMetadata> qualifierMetadataList = qualifierLookupService.getByQualifierList(dataResponse.getQualifiers());
		
		//Build Report Object
		TimeSeriesSummaryReport report = createReport(dataResponse, primaryDescription, upchainDescriptions, downchainDescriptions, locationDescription, upchainProcessorList, correctionList, ratingCurveList, gradeMetadataList, qualifierMetadataList, gapList, startDate, endDate, requestingUser);
		
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
		List<GradeMetadata> gradeMetadataList,
		List<QualifierMetadata> qualifierMetadataList,
		List<TimeSeriesSummaryDataGap> gapList,
		Instant startDate,
		Instant endDate,
		String requestingUser) {			
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
			
			//Add Report Metadata
			report.setReportMetadata(createTimeSeriesSummaryMetadata(primaryDescription, locationDescription, gradeMetadataList, qualifierMetadataList, startDate, endDate, requestingUser));
			
			//Add Primary TS Data
			report.setPrimaryTsData(createTimeSeriesSummaryCorrectedData(primaryDataResponse, upchainProcessorList, gapList, startDate, endDate));
			
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
		List<GradeMetadata> gradeMetadataList,
		List<QualifierMetadata> qualifierMetadataList,
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
		metadata.setQualifierMetadata(qualifierMetadataList);
		metadata.setGradeMetadata(gradeMetadataList);
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
	
	private TimeSeriesSummaryCorrectedData createTimeSeriesSummaryCorrectedData(TimeSeriesDataServiceResponse response, List<Processor> upchainProcessorList, List<TimeSeriesSummaryDataGap> gapList, Instant startDate, Instant endDate) {
		TimeSeriesSummaryCorrectedData data = new TimeSeriesSummaryCorrectedData();
		
		//Copy Desired Fields
		data.setApprovals(response.getApprovals());
		data.setQualifiers(response.getQualifiers());
		data.setNotes(response.getNotes());
		data.setMethods(response.getMethods());
		data.setGapTolerances(response.getGapTolerances());
		data.setInterpolationTypes(response.getInterpolationTypes());
		data.setGrades(response.getGrades());
		data.setProcessors(upchainProcessorList);
		data.setGaps(gapList);
		
		return data;
	}
	
	private List<TimeSeriesSummaryDataGap> createTimeSeriesSummaryDataGaps(List<TimeSeriesPoint> timeSeriesPoints, List<GapTolerance> gapTolerances, Instant seriesStartDate, Instant seriesEndDate, Instant startDate, Instant endDate) {
		List<TimeSeriesSummaryDataGap> gapList = new ArrayList<>();
				
		for(int i = 0; i < timeSeriesPoints.size(); i++) {
			TimeSeriesPoint point = timeSeriesPoints.get(i);
			Instant time = timeSeriesPoints.get(i).getTimestamp().getDateTimeOffset();
			Instant preTime = (i > 0) ?  timeSeriesPoints.get(i-1).getTimestamp().getDateTimeOffset() : null;
			Instant postTime = (i < (timeSeriesPoints.size() -1)) ? timeSeriesPoints.get(i+1).getTimestamp().getDateTimeOffset() : null;
			Boolean gapContained = false;
			
			if(point.getValue().getNumeric() == null) {
				//Gap Marker Found
				TimeSeriesSummaryDataGap gap = new TimeSeriesSummaryDataGap();
				gap.setStartTime(preTime);
				gap.setEndTime(postTime);
				
				//Determine where this gap is
				if(preTime != null && postTime != null) {
					gap.setGapExtent(TimeSeriesSummaryDataGapExtent.CONTAINED);
				} else if(preTime == null) {
					gap.setGapExtent(TimeSeriesSummaryDataGapExtent.OVER_START);
				} else if(postTime == null) {
					gap.setGapExtent(TimeSeriesSummaryDataGapExtent.OVER_END);
				} else {
					gap.setGapExtent(TimeSeriesSummaryDataGapExtent.OVER_ALL);
				}
				
				gapList.add(gap);
			}
		}
		
		return gapList;
	}
	
	private TimeSeriesSummaryCorrections createTimeSeriesSummaryCorrections(List<Correction> rawCorrections) {
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
	
