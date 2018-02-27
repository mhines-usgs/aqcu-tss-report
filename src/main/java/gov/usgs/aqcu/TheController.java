package gov.usgs.aqcu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;

import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;

@RestController
@RequestMapping("/timeseriessummary")
public class TheController {

	private TimeSeriesMetadataService timeSeriesMetadataService;

	@Autowired
	public TheController(TimeSeriesMetadataService timeSeriesMetadataService) {
		this.timeSeriesMetadataService = timeSeriesMetadataService;
	}

	@GetMapping
	public TimeSeriesDescriptionListByUniqueIdServiceResponse getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@RequestParam String station,
			@RequestParam(required=false) String lastMonths,
			@RequestParam(required=false) String waterYear,
			@RequestParam(required=false) String startDate,
			@RequestParam(required=false) String endDate,
			@RequestParam(required=false) String excludedCorrections) {

		return timeSeriesMetadataService.get(primaryTimeseriesIdentifier);
	}

}
