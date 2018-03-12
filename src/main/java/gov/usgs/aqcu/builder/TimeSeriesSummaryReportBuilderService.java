package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Component
public class TimeSeriesSummaryReportBuilderService extends AqcuReportBuilderService {
	public static final String REPORT_TITLE = "Time Series Summary";
	public static final String REPORT_TYPE = "timeseriessummary";
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesSummaryReportBuilderService.class);

	private TimeSeriesDataCorrectedService timeSeriesDataCorrectedService;
	private RatingCurveListService ratingCurveListService;
	private UpchainProcessorListService upchainProcessorListService;
	private DownchainProcessorListService downchainProcessorListService;
	private CorrectionListService correctionListService;

	@Autowired
	public TimeSeriesSummaryReportBuilderService(
		Gson gson, 
		AqcuReportMetadataBuilderService reportMetadataBuilderService,
		AqcuDataGapListBuilderService dataGapListBuilderService,
		AqcuReportUrlBuilderService reportUrlBuilderService,
		GradeLookupService gradeLookupService, 
		QualifierLookupService qualifierLookupService,
		LocationDescriptionListService locationDescriptionListService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataCorrectedService timeSeriesDataCorrectedService,
		UpchainProcessorListService upchainProcessorListService,
		DownchainProcessorListService downchainProcessorListService,
		RatingCurveListService ratingCurveListService,
		CorrectionListService correctionListService) {
		super(gson, reportMetadataBuilderService, dataGapListBuilderService, reportUrlBuilderService, gradeLookupService, qualifierLookupService, locationDescriptionListService, timeSeriesDescriptionListService);
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.correctionListService = correctionListService;
	}

	public TimeSeriesSummaryReport buildReport(String primaryTimeseriesIdentifier, List<String> excludedCorrections, Instant startDate, Instant endDate, String requestingUser) throws Exception {
		//Downchain TS Unique Ids
		List<String> downchainUniqueIdList = downchainProcessorListService.getOutputTimeSeriesUniqueIdList(primaryTimeseriesIdentifier, startDate, endDate);

		//Upchain Processors, TS Unique Ids, and Rating Model Ids
		ProcessorListServiceResponse upchainProcessorsResponse = upchainProcessorListService.getRawResponse(primaryTimeseriesIdentifier, startDate, endDate);
		List<Processor> upchainProcessorList = upchainProcessorsResponse.getProcessors();
		List<String> upchainUniqueIdList = upchainProcessorListService.getInputTimeSeriesUniqueIdList(upchainProcessorsResponse);
		List<String> ratingModelUniqueIdList = upchainProcessorListService.getRatingModelUniqueIdList(upchainProcessorsResponse);
		String primaryRatingModelUniqueId = (ratingModelUniqueIdList != null && ratingModelUniqueIdList.size() > 0) ? ratingModelUniqueIdList.get(0) : null;

		//Filtered Rating Curves
		List<RatingCurve> ratingCurveList = null;
		List<RatingShift> ratingShiftList = null;
		if(primaryRatingModelUniqueId != null) {
			ratingCurveList = ratingCurveListService.getAqcuFilteredRatingCurves(primaryRatingModelUniqueId, null, null, null, startDate, endDate);
			ratingShiftList = ratingCurveListService.getAqcuFilteredRatingShifts(ratingCurveList, startDate, endDate);
		}

		//Filtered Corrections
		List<AqcuExtendedCorrection> correctionList = correctionListService.getAqcuExtendedCorrectionList(primaryTimeseriesIdentifier, startDate, endDate, excludedCorrections);

		//Primary Corrected Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataCorrectedService.get(primaryTimeseriesIdentifier, startDate, endDate);

		//Calculate Data Gaps
		List<AqcuDataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints(), startDate, endDate);

		//Metadata Lookups
		List<GradeMetadata> gradeMetadataList = gradeLookupService.getByGradeList(dataResponse.getGrades());
		List<QualifierMetadata> qualifierMetadataList = qualifierLookupService.getByQualifierList(dataResponse.getQualifiers());
		
		//Primary Description, Upchain Descriptions, and Downchain Descriptions
		Map<String,List<String>> batchDescriptionRequestMap = new HashMap<>();
		batchDescriptionRequestMap.put("primary",Collections.singletonList(primaryTimeseriesIdentifier));
		batchDescriptionRequestMap.put("upchain", upchainUniqueIdList);
		batchDescriptionRequestMap.put("downchain", downchainUniqueIdList);
		Map<String,List<TimeSeriesDescription>> batchDescriptionResultMap = timeSeriesDescriptionListService.getBatchTimeSeriesDescriptionLists(batchDescriptionRequestMap);
		TimeSeriesDescription primaryDescription = batchDescriptionResultMap.get("primary").get(0);
		List<TimeSeriesDescription> upchainDescriptions = batchDescriptionResultMap.get("upchain");
		List<TimeSeriesDescription> downchainDescriptions = batchDescriptionResultMap.get("downchain");

		//Location Description
		LocationDescription locationDescription = locationDescriptionListService.getFirstLocationDescription(primaryDescription.getLocationIdentifier());

		//Create Advanced Option URL Params
		Map<String,String> urlParams = createUrlParameters(excludedCorrections);

		//Create Report Metadata
		AqcuReportMetadata reportMetadata = reportMetadataBuilderService.createBaseReportMetadata(REPORT_TYPE, REPORT_TITLE, primaryTimeseriesIdentifier, primaryDescription.getIdentifier(), primaryDescription.getUtcOffset(), locationDescription.getName(), locationDescription.getIdentifier(), gradeMetadataList, qualifierMetadataList, startDate, endDate, urlParams, requestingUser);

		//Create Related Report URLs
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", startDate, endDate,  reportMetadata.getPrimaryTimeSeriesIdentifier(), reportMetadata.getStationId(), reportMetadata.getAdvancedOptions());
		Map<String,String> upchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", startDate, endDate, upchainUniqueIdList, reportMetadata.getStationId(), reportMetadata.getAdvancedOptions());
		Map<String,String> downchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", startDate, endDate, downchainUniqueIdList, reportMetadata.getStationId(), reportMetadata.getAdvancedOptions());

		//Build Report Object
		TimeSeriesSummaryReport report = createReport(dataResponse, primaryDescription, upchainDescriptions, upchainUrls, downchainDescriptions, downchainUrls, upchainProcessorList, correctionList, corrUrl, ratingCurveList, ratingShiftList, gapList, startDate, endDate, reportMetadata);

		return report;
	}

	private TimeSeriesSummaryReport createReport (
		TimeSeriesDataServiceResponse primaryDataResponse,
		TimeSeriesDescription primaryDescription,
		List<TimeSeriesDescription> upchainDescriptions,
		Map<String,String> upchainUrls,
		List<TimeSeriesDescription> downchainDescriptions,
		Map<String,String> downchainUrls,
		List<Processor> upchainProcessorList,
		List<AqcuExtendedCorrection> correctionList,
		String corrUrl,
		List<RatingCurve> ratingCurveList,
		List<RatingShift> ratingShiftList,
		List<AqcuDataGap> gapList,
		Instant startDate,
		Instant endDate,
		AqcuReportMetadata reportMetadata) {			
			TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();

			//Add Report Metadata
			report.setReportMetadata(reportMetadata);

			//Add Primary TS Data
			report.setPrimaryTsData(createTimeSeriesSummaryCorrectedData(primaryDataResponse, upchainProcessorList, gapList, startDate, endDate));

			//Add Primary TS Metadata
			report.setPrimaryTsMetadata(primaryDescription);

			//Add Upchain TS Metadata
			if(upchainDescriptions != null && upchainDescriptions.size() > 0) {
				report.setUpchainTs(createTimeSeriesSummaryRelatedSeriesList(upchainDescriptions, upchainUrls));
			}

			//Add Downchain TS Metadata
			if(downchainDescriptions != null && downchainDescriptions.size() > 0) {
				report.setDownchainTs(createTimeSeriesSummaryRelatedSeriesList(downchainDescriptions, downchainUrls));
			}

			//Add Corrections
			report.setCorrections(createTimeSeriesSummaryCorrections(correctionList, corrUrl));

			//Add Rating Curve Data 
			if(ratingCurveList != null && ratingCurveList.size() > 0) {
				report.setRatingCurves(ratingCurveList);
				report.setRatingShifts(createTimeSeriesSummaryRatingShifts(ratingCurveList, ratingShiftList, startDate, endDate));
			}

			return report;
	}

	private Map<String,String> createUrlParameters(List<String> excludedCorrections) {
		Map<String,String> urlParams = new HashMap<>();

		if(excludedCorrections != null && excludedCorrections.size() > 0) {
			urlParams.put("excludedCorrections", String.join(",", excludedCorrections));
		}

		return urlParams;
	}

	private List<TimeSeriesSummaryRelatedSeries> createTimeSeriesSummaryRelatedSeriesList(List<TimeSeriesDescription> descriptions, Map<String,String> reportUrls) {
		List<TimeSeriesSummaryRelatedSeries> series = new ArrayList<>();

		for(TimeSeriesDescription desc : descriptions) {
			TimeSeriesSummaryRelatedSeries newSeries = new TimeSeriesSummaryRelatedSeries();
			newSeries.setIdentifier(desc.getIdentifier());
			newSeries.setUrl(reportUrls.get(desc.getUniqueId()));
			series.add(newSeries);
		}

		return series;
	}

	private TimeSeriesSummaryCorrectedData createTimeSeriesSummaryCorrectedData(TimeSeriesDataServiceResponse response, List<Processor> upchainProcessorList, List<AqcuDataGap> gapList, Instant startDate, Instant endDate) {
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

	private TimeSeriesSummaryCorrections createTimeSeriesSummaryCorrections(List<AqcuExtendedCorrection> correctionList, String corrUrl) {
		TimeSeriesSummaryCorrections corrections = new TimeSeriesSummaryCorrections();
		List<AqcuExtendedCorrection> pre = new ArrayList<>();
		List<AqcuExtendedCorrection> normal = new ArrayList<>();
		List<AqcuExtendedCorrection> post = new ArrayList<>();

		if(correctionList != null && correctionList.size() > 0) {
			for(AqcuExtendedCorrection corr : correctionList) {
				if(corr.getProcessingOrder() == CorrectionProcessingOrder.PreProcessing) {
					pre.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.Normal) {
					normal.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.PostProcessing) {
					post.add(corr);
				}
			}
		}

		corrections.setPreProcessing(pre);
		corrections.setNormal(normal);
		corrections.setPostProcessing(post);
		corrections.setCorrUrl(corrUrl);

		return corrections;
	}

	private List<TimeSeriesSummaryRatingShift> createTimeSeriesSummaryRatingShifts(List<RatingCurve> curveList, List<RatingShift> shiftList, Instant startDate, Instant endDate) {
		List<TimeSeriesSummaryRatingShift> ratingShifts = new ArrayList<>();

		for(RatingShift shift : shiftList) {
			for(RatingCurve curve : curveList) {
				if(curve.getShifts().contains(shift)) {
					TimeSeriesSummaryRatingShift newShift = new TimeSeriesSummaryRatingShift(shift, curve.getId());
					ratingShifts.add(newShift);
					break;
				}
			}
		}

		return ratingShifts;
	}
}