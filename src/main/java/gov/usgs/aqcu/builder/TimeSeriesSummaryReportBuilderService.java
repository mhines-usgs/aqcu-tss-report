package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;
import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Service
public class TimeSeriesSummaryReportBuilderService {
	public static final String REPORT_TITLE = "Time Series Summary";
	public static final String REPORT_TYPE = "timeseriessummary";

	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesSummaryReportBuilderService.class);

	private DataGapListBuilderService dataGapListBuilderService;
	private ReportUrlBuilderService reportUrlBuilderService;
	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	private LocationDescriptionService locationDescriptionService;
	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private TimeSeriesDataCorrectedService timeSeriesDataCorrectedService;
	private RatingCurveListService ratingCurveListService;
	private UpchainProcessorListService upchainProcessorListService;
	private DownchainProcessorListService downchainProcessorListService;
	private CorrectionListService correctionListService;

	@Autowired
	public TimeSeriesSummaryReportBuilderService(
		DataGapListBuilderService dataGapListBuilderService,
		ReportUrlBuilderService reportUrlBuilderService,
		GradeLookupService gradeLookupService, 
		QualifierLookupService qualifierLookupService,
		LocationDescriptionService locationDescriptionService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataCorrectedService timeSeriesDataCorrectedService,
		UpchainProcessorListService upchainProcessorListService,
		DownchainProcessorListService downchainProcessorListService,
		RatingCurveListService ratingCurveListService,
		CorrectionListService correctionListService) {
		this.timeSeriesDataCorrectedService = timeSeriesDataCorrectedService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.correctionListService = correctionListService;
		this.dataGapListBuilderService = dataGapListBuilderService;
		this.reportUrlBuilderService = reportUrlBuilderService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
		this.locationDescriptionService = locationDescriptionService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
	}

	public TimeSeriesSummaryReport buildReport(TimeSeriesSummaryRequestParameters requestParameters, String requestingUser) {
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();

		//Create the Report
		report = addBasicReportMetadata(report, requestParameters, requestingUser);
		report = addTimeSeriesData(report, requestParameters.getPrimaryTimeseriesIdentifier(), requestParameters.getStartInstant(), requestParameters.getEndInstant(), requestParameters);
		report = addCorrectionsData(report, requestParameters.getPrimaryTimeseriesIdentifier(), requestParameters.getStartInstant(), requestParameters.getEndInstant(), report.getReportMetadata().getStationId(), requestParameters);
		report = addLocationData(report, report.getReportMetadata().getStationId());
		report = addLookupData(report, report.getPrimaryTsData().getGrades(), report.getPrimaryTsData().getQualifiers());

		if(!report.getPrimaryTsData().getProcessors().isEmpty() && report.getPrimaryTsData().getProcessors().get(0).getInputRatingModelIdentifier() != null) {
			report = addRatingInformation(report, report.getPrimaryTsData().getProcessors().get(0).getInputRatingModelIdentifier(), requestParameters.getStartInstant(), requestParameters.getEndInstant());
		}
		
		return report;
	}

	protected TimeSeriesSummaryReport addBasicReportMetadata(TimeSeriesSummaryReport report, TimeSeriesSummaryRequestParameters requestParameters, String requestingUser) {
		TimeSeriesSummaryReportMetadata metadata = new TimeSeriesSummaryReportMetadata();
		metadata.setReportType(REPORT_TYPE);
		metadata.setTitle(REPORT_TITLE);
		metadata.setRequestParameters(requestParameters);
		metadata.setRequestingUser(requestingUser);
		report.setReportMetadata(metadata);
		return report;
	}

	protected TimeSeriesSummaryReport addTimeSeriesData(TimeSeriesSummaryReport report, String primaryTimeseriesIdentifier, Instant startTime, Instant endTime, TimeSeriesSummaryRequestParameters requestParameters) {
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
		List<DataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints());

		//Build Upchain and Downchain URLs
		Map<String,String> upchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", primaryDescription.getLocationIdentifier(), requestParameters, upchainUniqueIdList);
		Map<String,String> downchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", primaryDescription.getLocationIdentifier(), requestParameters, downchainUniqueIdList);

		//Add to Report
		report.getReportMetadata().setStationId(primaryDescription.getLocationIdentifier());
		report.getReportMetadata().setTimezone(primaryDescription.getUtcOffset());
		report.getReportMetadata().setPrimaryParameter(primaryDescription.getParameter());
		report.setPrimaryTsMetadata(primaryDescription);
		report.setPrimaryTsData(new TimeSeriesSummaryCorrectedData(dataResponse, upchainProcessorList, gapList));
		report.setUpchainTs(createTimeSeriesSummaryRelatedSeriesList(upchainDescriptions, upchainUrls));
		report.setDownchainTs(createTimeSeriesSummaryRelatedSeriesList(downchainDescriptions, downchainUrls));

		return report;
	}

	protected TimeSeriesSummaryReport addCorrectionsData(TimeSeriesSummaryReport report, String primaryTimeSeriesIdentifier, Instant startTime, Instant endTime, String stationId, TimeSeriesSummaryRequestParameters requestParameters) {
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", stationId, requestParameters, null);
		List<ExtendedCorrection> correctionList = correctionListService.getExtendedCorrectionList(primaryTimeSeriesIdentifier, startTime, endTime, requestParameters.getExcludedCorrections());

		report.setCorrections(new TimeSeriesSummaryCorrections(correctionList, corrUrl));

		return report;
	}

	protected TimeSeriesSummaryReport addLocationData(TimeSeriesSummaryReport report, String stationId) {
		report.getReportMetadata().setStationName(locationDescriptionService.getByLocationIdentifier(stationId).getName());
				
		return report;
	}

	protected TimeSeriesSummaryReport addRatingInformation(TimeSeriesSummaryReport report, String primaryRatingModelIdentifier, Instant startTime, Instant endTime) {
		List<RatingCurve> ratingCurveList = null;
		List<RatingShift> ratingShiftList = null;
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

		return report;
	}

	protected TimeSeriesSummaryReport addLookupData(TimeSeriesSummaryReport report, List<Grade> gradeList, List<Qualifier> qualifierList) {
		report.getReportMetadata().setGradeMetadata(gradeLookupService.getByGradeList(gradeList));
		report.getReportMetadata().setQualifierMetadata(qualifierLookupService.getByQualifierList(qualifierList));
		return report;
	}

	protected List<TimeSeriesSummaryRelatedSeries> createTimeSeriesSummaryRelatedSeriesList(List<TimeSeriesDescription> descriptions, Map<String,String> reportUrls) {
		List<TimeSeriesSummaryRelatedSeries> series = new ArrayList<>();

		if(!descriptions.isEmpty()) {
			for(TimeSeriesDescription desc : descriptions) {
				TimeSeriesSummaryRelatedSeries newSeries = new TimeSeriesSummaryRelatedSeries();
				newSeries.setIdentifier(desc.getIdentifier());
				newSeries.setUrl(reportUrls.get(desc.getUniqueId()));
				series.add(newSeries);
			}
		}
		
		return series;
	}
}