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
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
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

	public TimeSeriesSummaryReport buildReport(String primaryTimeseriesIdentifier, List<String> excludedCorrections, Instant startDate, Instant endDate, String requestingUser) throws Exception {
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();

		//Create Advanced Option URL Params
		Map<String,String> urlParams = createUrlParameters(excludedCorrections);
		
		//Create Metadata
		report.setReportMetadata(new AqcuReportMetadata(REPORT_TYPE, REPORT_TITLE, primaryTimeseriesIdentifier, startDate, endDate, urlParams, requestingUser));

		return addCorrectionsData(report, excludedCorrections);
	}

	private TimeSeriesSummaryReport addCorrectionsData(TimeSeriesSummaryReport report, List<String> excludedCorrections) {
		AqcuReportMetadata meta = report.getReportMetadata();
		String corrUrl = reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", meta.getStartDate(), meta.getEndDate(),  meta.getPrimaryTimeSeriesIdentifier(), meta.getStationId(), meta.getAdvancedOptions());
		List<AqcuExtendedCorrection> correctionList = correctionListService.getAqcuExtendedCorrectionList(meta.getPrimaryTimeSeriesIdentifier(), meta.getStartDate(), meta.getEndDate(), excludedCorrections);

		report.setCorrections(new TimeSeriesSummaryCorrections(correctionList, corrUrl));

		return addTimeSeriesData(report);
	}

	private TimeSeriesSummaryReport addTimeSeriesData(TimeSeriesSummaryReport report) {
		AqcuReportMetadata meta = report.getReportMetadata();

		//Downchain TS Unique Ids
		List<Processor> downchainProcessorList = downchainProcessorListService.getProcessorList(meta.getPrimaryTimeSeriesIdentifier(), meta.getStartDate(), meta.getEndDate());
		List<String> downchainUniqueIdList = downchainProcessorListService.getOutputTimeSeriesUniqueIdList(downchainProcessorList);

		//Upchain Processors and TS Unique Ids
		List<Processor> upchainProcessorList = upchainProcessorListService.getProcessorList(meta.getPrimaryTimeSeriesIdentifier(), meta.getStartDate(), meta.getEndDate());
		List<String> upchainUniqueIdList = upchainProcessorListService.getInputTimeSeriesUniqueIdList(upchainProcessorList);

		//Primary Description, Upchain Descriptions, and Downchain Descriptions
		Map<String,List<String>> batchDescriptionRequestMap = new HashMap<>();
		batchDescriptionRequestMap.put("primary",Collections.singletonList(meta.getPrimaryTimeSeriesIdentifier()));
		batchDescriptionRequestMap.put("upchain", upchainUniqueIdList);
		batchDescriptionRequestMap.put("downchain", downchainUniqueIdList);
		Map<String,List<TimeSeriesDescription>> batchDescriptionResultMap = timeSeriesDescriptionListService.getBatchTimeSeriesDescriptionLists(batchDescriptionRequestMap);
		TimeSeriesDescription primaryDescription = batchDescriptionResultMap.get("primary").get(0);
		List<TimeSeriesDescription> upchainDescriptions = batchDescriptionResultMap.get("upchain");
		List<TimeSeriesDescription> downchainDescriptions = batchDescriptionResultMap.get("downchain");

		//Primary TS Data
		TimeSeriesDataServiceResponse dataResponse = timeSeriesDataCorrectedService.get(meta.getPrimaryTimeSeriesIdentifier(), meta.getStartDate(), meta.getEndDate());

		//Calculate Data Gaps
		List<AqcuDataGap> gapList = dataGapListBuilderService.buildGapList(dataResponse.getPoints(), meta.getStartDate(), meta.getEndDate());

		//Build Upchain and Downchain URLs
		Map<String,String> upchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", meta.getStartDate(), meta.getEndDate(), upchainUniqueIdList, primaryDescription.getLocationIdentifier(), meta.getAdvancedOptions());
		Map<String,String> downchainUrls = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("timeseriessummary", meta.getStartDate(), meta.getEndDate(), downchainUniqueIdList, primaryDescription.getLocationIdentifier(), meta.getAdvancedOptions());

		//Add to Report
		meta.setStationId(primaryDescription.getLocationIdentifier());
		meta.setTimezone(primaryDescription.getUtcOffset());
		report.setPrimaryTsMetadata(primaryDescription);
		report.setPrimaryTsData(new TimeSeriesSummaryCorrectedData(dataResponse, upchainProcessorList, gapList));
		report.setUpchainTs(createTimeSeriesSummaryRelatedSeriesList(upchainDescriptions, upchainUrls));
		report.setDownchainTs(createTimeSeriesSummaryRelatedSeriesList(downchainDescriptions, downchainUrls));

		return addLocationData(report);
	}

	private TimeSeriesSummaryReport addLocationData(TimeSeriesSummaryReport report) throws AquariusException {
		AqcuReportMetadata meta = report.getReportMetadata();
		List<LocationDescription> locationList = locationDescriptionListService.getLocationDescriptionList(meta.getStationId());
		
		if(!locationList.isEmpty()) {
			meta.setStationName(locationList.get(0).getName());
		}
		
		return addRatingInformation(report);
	}

	private TimeSeriesSummaryReport addRatingInformation(TimeSeriesSummaryReport report) throws AquariusException {
		AqcuReportMetadata meta = report.getReportMetadata();

		List<RatingCurve> ratingCurveList = null;
		List<RatingShift> ratingShiftList = null;
		if(!report.getPrimaryTsData().getProcessors().isEmpty()) {
			ratingCurveList = ratingCurveListService.getAqcuFilteredRatingCurves(report.getPrimaryTsData().getProcessors().get(0).getInputRatingModelIdentifier(), null, null, null, meta.getStartDate(), meta.getEndDate());
			ratingShiftList = ratingCurveListService.getAqcuFilteredRatingShifts(ratingCurveList, meta.getStartDate(), meta.getEndDate());
			report.setRatingCurves(ratingCurveList);
			report.setRatingShifts(createTimeSeriesSummaryRatingShiftList(ratingCurveList, ratingShiftList, meta.getStartDate(), meta.getEndDate()));
		}

		return addLookupData(report);
	}

	private TimeSeriesSummaryReport addLookupData(TimeSeriesSummaryReport report) {
		AqcuReportMetadata meta = report.getReportMetadata();
		List<GradeMetadata> gradeMetadataList = gradeLookupService.getByGradeList(report.getPrimaryTsData().getGrades());
		List<QualifierMetadata> qualifierMetadataList = qualifierLookupService.getByQualifierList(report.getPrimaryTsData().getQualifiers());
		meta.setGradeMetadata(gradeMetadataList);
		meta.setQualifierMetadata(qualifierMetadataList);
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

	private List<TimeSeriesSummaryRatingShift> createTimeSeriesSummaryRatingShiftList(List<RatingCurve> curveList, List<RatingShift> shiftList, Instant startDate, Instant endDate) {
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