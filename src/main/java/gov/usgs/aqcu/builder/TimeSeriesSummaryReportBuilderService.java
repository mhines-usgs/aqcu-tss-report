package gov.usgs.aqcu.builder;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Qualifier;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;
import gov.usgs.aqcu.util.LogExecutionTime;
import gov.usgs.aqcu.util.TimeSeriesUtils;
import gov.usgs.aqcu.model.*;
import gov.usgs.aqcu.retrieval.*;

@Service
public class TimeSeriesSummaryReportBuilderService {
	public static final String REPORT_TITLE = "Time Series Summary";
	public static final String REPORT_TYPE = "timeseriessummary";

	private Logger log = LoggerFactory.getLogger(TimeSeriesSummaryReportBuilderService.class);

	private DataGapListBuilderService dataGapListBuilderService;
	private ReportUrlBuilderService reportUrlBuilderService;
	private GradeLookupService gradeLookupService;
	private QualifierLookupService qualifierLookupService;
	private LocationDescriptionListService locationDescriptionListService;
	private TimeSeriesDescriptionListService timeSeriesDescriptionListService;
	private TimeSeriesDataService timeSeriesDataService;
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
		LocationDescriptionListService locationDescriptionListService,
		TimeSeriesDescriptionListService timeSeriesDescriptionListService,
		TimeSeriesDataService timeSeriesDataService,
		UpchainProcessorListService upchainProcessorListService,
		DownchainProcessorListService downchainProcessorListService,
		RatingCurveListService ratingCurveListService,
		CorrectionListService correctionListService) {
		this.timeSeriesDataService = timeSeriesDataService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.downchainProcessorListService = downchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.correctionListService = correctionListService;
		this.dataGapListBuilderService = dataGapListBuilderService;
		this.reportUrlBuilderService = reportUrlBuilderService;
		this.gradeLookupService = gradeLookupService;
		this.qualifierLookupService = qualifierLookupService;
		this.locationDescriptionListService = locationDescriptionListService;
		this.timeSeriesDescriptionListService = timeSeriesDescriptionListService;
	}

	@LogExecutionTime
	public TimeSeriesSummaryReport buildReport(TimeSeriesSummaryRequestParameters requestParameters, String requestingUser) {
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();

		//Primary TS Metadata
		log.debug("Get and parse primary timeseries description.");
		TimeSeriesDescription primaryDescription = timeSeriesDescriptionListService.getTimeSeriesDescription(requestParameters.getPrimaryTimeseriesIdentifier());
		ZoneOffset primaryZoneOffset = TimeSeriesUtils.getZoneOffset(primaryDescription);
		String primaryStationId = primaryDescription.getLocationIdentifier();
		report.setPrimaryTsMetadata(primaryDescription);
		
		//Upchain Processors and Rating Model
		log.debug("Get upchain  processors");
		List<Processor> upchainProcessors = getProcessors(true, requestParameters, primaryZoneOffset);

		//Primary TS Data
		log.debug("Get and parse primary series corrected data.");
		report.setPrimaryTsData(getCorrectedData(requestParameters, primaryZoneOffset, upchainProcessors, TimeSeriesUtils.isDailyTimeSeries(primaryDescription)));

		//Rating Data
		log.debug("Get and parse primary timeseries description.");
		String primaryRatingModel = getRatingModel(upchainProcessors);
		if(primaryRatingModel != null && !primaryRatingModel.isEmpty()) {
			report.setRatingCurves(getRatingCurves(requestParameters, primaryZoneOffset, primaryRatingModel));
			report.setRatingShifts(getRatingShifts(requestParameters, primaryZoneOffset, report.getRatingCurves()));
		}
		
		//Upchain & Downchain TS
		log.debug("Get related upchain and downchain TS.");
		report.setUpchainTs(getDerivationChainTS(true, requestParameters, primaryZoneOffset, primaryStationId, upchainProcessors));
		report.setDownchainTs(getDerivationChainTS(false, requestParameters, primaryZoneOffset, primaryStationId, 
				getProcessors(false, requestParameters, primaryZoneOffset)));
		
		//Corrections Data
		log.debug("Get and parse primary series corrections.");
		report.setCorrections(getCorrectionsData(requestParameters, primaryZoneOffset, primaryStationId));

		//Report Metadata
		log.debug("Set report metadata.");
		report.setReportMetadata(getReportMetadata(requestParameters,
			requestingUser,
			report.getPrimaryTsMetadata().getLocationIdentifier(), 
			report.getPrimaryTsMetadata().getIdentifier(),
			report.getPrimaryTsMetadata().getUtcOffset(),
			report.getPrimaryTsData().getGrades(), 
			report.getPrimaryTsData().getQualifiers()
		));

		return report;
	}

	protected TimeSeriesSummaryCorrections getCorrectionsData(TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset, String stationId) {
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", stationId, requestParameters, null);
		List<ExtendedCorrection> correctionList = correctionListService.getExtendedCorrectionList(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters.getStartInstant(primaryZoneOffset), 
			requestParameters.getEndInstant(primaryZoneOffset), 
			requestParameters.getExcludedCorrections());

		return new TimeSeriesSummaryCorrections(correctionList, corrUrl);
	}

	protected List<Processor> getProcessors(boolean upchain, TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset) {
		List<Processor> processorList;

		if(upchain) {
			processorList = upchainProcessorListService.getRawResponse(requestParameters.getPrimaryTimeseriesIdentifier(), 
					requestParameters.getStartInstant(primaryZoneOffset), requestParameters.getEndInstant(primaryZoneOffset)).getProcessors();
		} else {
			processorList = downchainProcessorListService.getRawResponse(requestParameters.getPrimaryTimeseriesIdentifier(), 
					requestParameters.getStartInstant(primaryZoneOffset), requestParameters.getEndInstant(primaryZoneOffset)).getProcessors();
		}

		return processorList;
	}

	protected String getRatingModel(List<Processor> processorList) {
		String ratingModel = null;
		
		if(processorList != null && !processorList.isEmpty()) {
			ratingModel = processorList.get(0).getInputRatingModelIdentifier();
		}

		return ratingModel;
	}

	protected List<TimeSeriesSummaryRelatedSeries> getDerivationChainTS(boolean upchain, TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset, String stationId, List<Processor> processorList) {
		List<String> relatedTsIdList;
		List<TimeSeriesDescription> relatedDescriptionList;
		List<TimeSeriesSummaryRelatedSeries> relatedSeriesList = new ArrayList<>();

		if(upchain) {
			relatedTsIdList = upchainProcessorListService.getInputTimeSeriesUniqueIdList(processorList);
		} else {
			relatedTsIdList = downchainProcessorListService.getOutputTimeSeriesUniqueIdList(processorList);
		}

		if(relatedTsIdList != null && relatedTsIdList.size() > 0) {
			relatedDescriptionList = timeSeriesDescriptionListService.getTimeSeriesDescriptionList(relatedTsIdList);

			if(relatedDescriptionList != null && relatedDescriptionList.size() > 0) {
				Map<String,String> relatedUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", stationId, requestParameters, relatedTsIdList);
				relatedSeriesList = createTimeSeriesSummaryRelatedSeriesList(relatedDescriptionList, relatedUrls);
			}
		}

		return relatedSeriesList;
	}

	protected TimeSeriesSummaryCorrectedData getCorrectedData(TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset, List<Processor> upchainProcessorList, boolean isDVSeries) {
		//Fetch Corrected Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataService.get(
			requestParameters.getPrimaryTimeseriesIdentifier(), 
			requestParameters,
			primaryZoneOffset,
			isDVSeries,
			false,
			true,
			null
		);

		//Calculate Data Gaps
		List<DataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints(), isDVSeries, primaryZoneOffset);

		return new TimeSeriesSummaryCorrectedData(dataResponse, upchainProcessorList, gapList);
	}

	protected TimeSeriesSummaryReportMetadata getReportMetadata(TimeSeriesSummaryRequestParameters requestParameters, String requestingUser, String stationId, String primaryParameter, Double utcOffset, List<Grade> gradeList, List<Qualifier> qualifierList) {
		TimeSeriesSummaryReportMetadata metadata = new TimeSeriesSummaryReportMetadata();
		metadata.setTitle(REPORT_TITLE);
		metadata.setRequestingUser(requestingUser);
		metadata.setRequestParameters(requestParameters);
		metadata.setStationId(stationId);
		metadata.setStationName(locationDescriptionListService.getByLocationIdentifier(stationId).getName());
		metadata.setTimezone(utcOffset);
		metadata.setPrimaryParameter(primaryParameter);

		if(gradeList != null && !gradeList.isEmpty()) {
			metadata.setGradeMetadata(gradeLookupService.getByGradeList(gradeList));
		}
		
		if(qualifierList != null && !qualifierList.isEmpty()) {
			metadata.setQualifierMetadata(qualifierLookupService.getByQualifierList(qualifierList));
		}
		
		return metadata;
	}

	protected List<RatingCurve> getRatingCurves(TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset, String primaryRatingModelIdentifier) {
		List<RatingCurve> rawCurveList = ratingCurveListService.getRawResponse(primaryRatingModelIdentifier, null, null, null).getRatingCurves();
		List<RatingCurve> ratingCurveList = ratingCurveListService.getAqcuFilteredRatingCurves(rawCurveList, 
				requestParameters.getStartInstant(primaryZoneOffset), requestParameters.getEndInstant(primaryZoneOffset));

		return ratingCurveList;
	}

	protected List<TimeSeriesSummaryRatingShift> getRatingShifts(TimeSeriesSummaryRequestParameters requestParameters, ZoneOffset primaryZoneOffset, List<RatingCurve> ratingCurves) {
		List<RatingShift> ratingShiftList =  ratingCurveListService.getAqcuFilteredRatingShifts(ratingCurves, 
				requestParameters.getStartInstant(primaryZoneOffset), requestParameters.getEndInstant(primaryZoneOffset));

		//Create Rating Shifts
		List<TimeSeriesSummaryRatingShift> ratingShifts = new ArrayList<>();
		for(RatingShift shift : ratingShiftList) {
			for(RatingCurve curve : ratingCurves) {
				if(curve.getShifts().contains(shift)) {
					TimeSeriesSummaryRatingShift newShift = new TimeSeriesSummaryRatingShift(shift, curve.getId());
					ratingShifts.add(newShift);
					break;
				}
			}
		}

		return ratingShifts;
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