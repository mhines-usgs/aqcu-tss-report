package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GradeMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.QualifierMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.exception.AquariusException;
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
		super(dataGapListBuilderService, reportUrlBuilderService, gradeLookupService, qualifierLookupService, locationDescriptionListService, timeSeriesDescriptionListService);
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.correctionListService = correctionListService;
	}

	public TimeSeriesSummaryReport buildReport(String primaryTimeseriesIdentifier, List<String> excludedCorrections, Instant startTime, Instant endTime, String requestingUser) throws Exception {
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();

		//Create the Report
		report = addBasicReportMetadata(report, primaryTimeseriesIdentifier, startTime, endTime, excludedCorrections, requestingUser);
		report = addTimeSeriesData(report, primaryTimeseriesIdentifier, startTime, endTime, report.getReportMetadata().getAdvancedOptions());
		report = addCorrectionsData(report, primaryTimeseriesIdentifier, startTime, endTime, report.getReportMetadata().getStationId(), excludedCorrections, report.getReportMetadata().getAdvancedOptions());
		report = addLocationData(report, report.getReportMetadata().getStationId());
		report = addLookupData(report, report.getPrimaryTsData().getGrades(), report.getPrimaryTsData().getQualifiers());

		if(!report.getPrimaryTsData().getProcessors().isEmpty()) {
			report = addRatingInformation(report, report.getPrimaryTsData().getProcessors().get(0).getInputRatingModelIdentifier(), startTime, endTime);
		}
		
		return report;
	}

	//Adds the basic report metadata we can get without doing any external calls
	private TimeSeriesSummaryReport addBasicReportMetadata(TimeSeriesSummaryReport report, String primaryTimeseriesIdentifier, Instant startTime, Instant endTime,  List<String> excludedCorrections, String requestingUser) {
		Map<String,String> urlParams = createUrlParameters(excludedCorrections);
		report.setReportMetadata(new AqcuReportMetadata(REPORT_TYPE, REPORT_TITLE, primaryTimeseriesIdentifier, null, null, null, null, null, null, startTime, endTime, urlParams, requestingUser));

		return report;
	}

	private TimeSeriesSummaryReport addTimeSeriesData(TimeSeriesSummaryReport report, String primaryTimeseriesIdentifier, Instant startTime, Instant endTime, Map<String,String> advancedOptions) {
		//Downchain TS Unique Ids
		List<Processor> downchainProcessorList = downchainProcessorListService.getRawResponse(primaryTimeseriesIdentifier, startTime, endTime).getProcessors();
		List<String> downchainUniqueIdList = downchainProcessorListService.getOutputTimeSeriesUniqueIdList(downchainProcessorList);

		//Upchain Processors and TS Unique Ids
		List<Processor> upchainProcessorList = upchainProcessorListService.getRawResponse(primaryTimeseriesIdentifier, startTime, endTime).getProcessors();
		List<String> upchainUniqueIdList = upchainProcessorListService.getInputTimeSeriesUniqueIdList(upchainProcessorList);

		//Primary Description, Upchain Descriptions, and Downchain Descriptions
		Map<String,List<String>> batchDescriptionRequestMap = new HashMap<>();
		batchDescriptionRequestMap.put("primary",Collections.singletonList(primaryTimeseriesIdentifier));
		batchDescriptionRequestMap.put("upchain", upchainUniqueIdList);
		batchDescriptionRequestMap.put("downchain", downchainUniqueIdList);
		Map<String,List<TimeSeriesDescription>> batchDescriptionResultMap = timeSeriesDescriptionListService.getBatchTimeSeriesDescriptionLists(batchDescriptionRequestMap);
		TimeSeriesDescription primaryDescription = batchDescriptionResultMap.get("primary").get(0);
		List<TimeSeriesDescription> upchainDescriptions = batchDescriptionResultMap.get("upchain");
		List<TimeSeriesDescription> downchainDescriptions = batchDescriptionResultMap.get("downchain");

		//Primary TS Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataCorrectedService.getRawResponse(primaryTimeseriesIdentifier, startTime, endTime);

		//Calculate Data Gaps
		List<AqcuDataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints(), startTime, endTime);

		//Build Upchain and Downchain URLs
		Map<String,String> upchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", startTime, endTime, upchainUniqueIdList, primaryDescription.getLocationIdentifier(), advancedOptions);
		Map<String,String> downchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", startTime, endTime, downchainUniqueIdList, primaryDescription.getLocationIdentifier(), advancedOptions);

		//Add to Report
		report.getReportMetadata().setStationId(primaryDescription.getLocationIdentifier());
		report.getReportMetadata().setTimezone(primaryDescription.getUtcOffset());
		report.setPrimaryTsMetadata(primaryDescription);
		report.setPrimaryTsData(new TimeSeriesSummaryCorrectedData(dataResponse, upchainProcessorList, gapList));
		report.setUpchainTs(createTimeSeriesSummaryRelatedSeriesList(upchainDescriptions, upchainUrls));
		report.setDownchainTs(createTimeSeriesSummaryRelatedSeriesList(downchainDescriptions, downchainUrls));

		return report;
	}

	private TimeSeriesSummaryReport addCorrectionsData(TimeSeriesSummaryReport report, String primaryTimeseriesIdentifier, Instant startTime, Instant endTime, String startionId, List<String> excludedCorrections, Map<String,String> advancedOptions) {
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", startTime, endTime, primaryTimeseriesIdentifier, startionId, advancedOptions);
		List<AqcuExtendedCorrection> correctionList = correctionListService.getAqcuExtendedCorrectionList(primaryTimeseriesIdentifier, startTime, endTime, excludedCorrections);

		report.setCorrections(new TimeSeriesSummaryCorrections(correctionList, corrUrl));

		return report;
	}

	private TimeSeriesSummaryReport addLocationData(TimeSeriesSummaryReport report, String stationId) throws AquariusException {
		List<LocationDescription> locationList = locationDescriptionListService.getRawResponse(stationId).getLocationDescriptions();
		
		if(!locationList.isEmpty()) {
			report.getReportMetadata().setStationName(locationList.get(0).getName());
		}
		
		return report;
	}

	private TimeSeriesSummaryReport addRatingInformation(TimeSeriesSummaryReport report, String primaryRatingModelIdentifier, Instant startTime, Instant endTime) throws AquariusException {
		List<RatingCurve> ratingCurveList = null;
		List<RatingShift> ratingShiftList = null;
		if(!report.getPrimaryTsData().getProcessors().isEmpty()) {
			List<RatingCurve> rawCurveList = ratingCurveListService.getRawResponse(primaryRatingModelIdentifier, null, null, null).getRatingCurves();
			ratingCurveList = ratingCurveListService.getAqcuFilteredRatingCurves(rawCurveList, startTime, endTime);
			ratingShiftList = ratingCurveListService.getAqcuFilteredRatingShifts(ratingCurveList, startTime, endTime);
			report.setRatingCurves(ratingCurveList);

			//Create Rating Shifts
			List<TimeSeriesSummaryRatingShift> ratingShifts = new ArrayList<>();
			for(RatingShift shift : ratingShiftList) {
				for(RatingCurve curve : ratingCurveList) {
					if(curve.getShifts().contains(shift)) {
						TimeSeriesSummaryRatingShift newShift = new TimeSeriesSummaryRatingShift(shift, curve.getId());
						ratingShifts.add(newShift);
						break;
					}
				}
			}

			report.setRatingShifts(ratingShifts);
		}

		return report;
	}

	private TimeSeriesSummaryReport addLookupData(TimeSeriesSummaryReport report, List<Grade> gradeList, List<Qualifier> qualifierList) {
		List<GradeMetadata> gradeMetadataList = gradeLookupService.getByGradeList(gradeList);
		List<QualifierMetadata> qualifierMetadataList = qualifierLookupService.getByQualifierList(qualifierList);
		report.getReportMetadata().setGradeMetadata(gradeMetadataList);
		report.getReportMetadata().setQualifierMetadata(qualifierMetadataList);
		return report;
	}

	private Map<String,String> createUrlParameters(List<String> excludedCorrections) {
		Map<String,String> urlParams = new HashMap<>();

		if(!excludedCorrections.isEmpty()) {
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
}