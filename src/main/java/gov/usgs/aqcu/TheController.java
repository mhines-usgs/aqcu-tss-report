package gov.usgs.aqcu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;

import gov.usgs.aqcu.model.TimeSeriesSummaryReport;

import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;

import gov.usgs.aqcu.builder.TimeSeriesSummaryReportBuilderService;

@RestController
@RequestMapping("/timeseriessummary")
public class TheController {
	private static final Logger LOG = LoggerFactory.getLogger(TheController.class);
	private Gson gson;
	private TimeSeriesMetadataService timeSeriesMetadataService;
	private RatingCurveListService ratingCurveListService;
	private UpchainProcessorListService upchainProcessorListService;
	private TimeSeriesSummaryReportBuilderService reportBuilderService;

	@Autowired
	public TheController(
		TimeSeriesMetadataService timeSeriesMetadataService, 
		UpchainProcessorListService upchainProcessorListService, 
		RatingCurveListService ratingCurveListService,
		TimeSeriesSummaryReportBuilderService reportBuilderService,
		GsonBuilder gsonBuilder) {
		this.timeSeriesMetadataService = timeSeriesMetadataService;
		this.upchainProcessorListService = upchainProcessorListService;
		this.ratingCurveListService = ratingCurveListService;
		this.reportBuilderService = reportBuilderService;
		
		gson = gsonBuilder.create();
	}

	@GetMapping
	public String getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@RequestParam String station,
			@RequestParam(required=false) String lastMonths,
			@RequestParam(required=false) String waterYear,
			@RequestParam(required=false) String startDateString,
			@RequestParam(required=false) String endDateString,
			@RequestParam(required=false) String excludedCorrections) {	
			
		Instant startDate = Instant.parse(startDateString);
		Instant endDate = Instant.parse(endDateString);
		
		TimeSeriesDescriptionListByUniqueIdServiceResponse metadataResponse = timeSeriesMetadataService.get(primaryTimeseriesIdentifier);
		ProcessorListServiceResponse processorsResponse = upchainProcessorListService.get(primaryTimeseriesIdentifier, startDate, endDate);
		RatingCurveListServiceResponse ratingCurvesResponse = ratingCurveListService.get(processorsResponse.getProcessors().get(0).getInputRatingModelIdentifier(), null, startDate, endDate);
		
		TimeSeriesSummaryReport report = reportBuilderService.buildTimeSeriesSummaryReport(metadataResponse, ratingCurvesResponse);
		
		return gson.toJson(report);
	}
	
}
