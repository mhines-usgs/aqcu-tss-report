package gov.usgs.aqcu;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.usgs.aqcu.client.JavaToRClient;
import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;

@RestController
@RequestMapping("/timeseriessummary")
public class TheController {

	private TimeSeriesMetadataService timeSeriesMetadataService;
	private JavaToRClient javaToRClient;

	@Value("classpath:testData/timeSeriesSummary.json")
	private Resource mpPub1;
	@Bean
	public String reportJson() throws IOException {
		return new String(FileCopyUtils.copyToByteArray(mpPub1.getInputStream()));
	}

	@Autowired
	public TheController(TimeSeriesMetadataService timeSeriesMetadataService, JavaToRClient javaToRClient) {
		this.timeSeriesMetadataService = timeSeriesMetadataService;
		this.javaToRClient = javaToRClient;
	}

	@GetMapping
	public byte[] getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@RequestParam String station,
			@RequestParam(required=false) String lastMonths,
			@RequestParam(required=false) String waterYear,
			@RequestParam(required=false) String startDate,
			@RequestParam(required=false) String endDate,
			@RequestParam(required=false) String excludedCorrections) throws IOException {

//		timeSeriesMetadataService.get(primaryTimeseriesIdentifier);

		return javaToRClient.render("drsteini", "timeseriessummary", reportJson());
	}

}
