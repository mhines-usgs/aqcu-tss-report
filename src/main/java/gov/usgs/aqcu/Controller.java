package gov.usgs.aqcu;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.format.annotation.DateTimeFormat;

import com.aquaticinformatics.aquarius.sdk.timeseries.serializers.InstantDeserializer;

import gov.usgs.aqcu.builder.TimeSeriesSummaryReportBuilderService;
import gov.usgs.aqcu.client.JavaToRClient;
import gov.usgs.aqcu.model.TimeSeriesSummaryReport;
import gov.usgs.aqcu.util.AqcuTimeUtils;

@RestController
@Validated
@RequestMapping("/timeseriessummary")
public class Controller {
	private static final Logger LOG = LoggerFactory.getLogger(Controller.class);
	private Gson gson;
	private TimeSeriesSummaryReportBuilderService reportBuilderService;
	private JavaToRClient javaToRClient;

	@Autowired
	public Controller(
		TimeSeriesSummaryReportBuilderService reportBuilderService,
		JavaToRClient javaToRClient,
		Gson gson) {
		this.reportBuilderService = reportBuilderService;
		this.javaToRClient = javaToRClient;
		this.gson = gson;
	}

	@GetMapping(produces={MediaType.TEXT_HTML_VALUE})
	public ResponseEntity<?> getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@RequestParam(required=true) LocalDate startDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@RequestParam(required=true) LocalDate endDate,
			@RequestParam(required=false) List<String> excludedCorrections) throws Exception {
		//Pull Requesting User From Headers
		String requestingUser = "testUser";
		
		//Replace null parameters with empty values
		if(excludedCorrections == null) {
			excludedCorrections = new ArrayList<>();
		}

		//Build the TSS Report JSON
		TimeSeriesSummaryReport report = reportBuilderService.buildReport(primaryTimeseriesIdentifier, excludedCorrections, AqcuTimeUtils.toReportStartTime(startDate), AqcuTimeUtils.toReportEndTime(endDate), requestingUser);

		byte[] reportHtml = javaToRClient.render(requestingUser, "timeseriessummary", gson.toJson(report, TimeSeriesSummaryReport.class));
		return new ResponseEntity<byte[]>(reportHtml, new HttpHeaders(), HttpStatus.OK);
	}
	
	@GetMapping(value="/rawData", produces={MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<TimeSeriesSummaryReport> getReportRawData(
			@RequestParam String primaryTimeseriesIdentifier,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@RequestParam(required=true) LocalDate startDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			@RequestParam(required=true) LocalDate endDate,
			@RequestParam(required=false) List<String> excludedCorrections) throws Exception {
		//Pull Requesting User From Headers
		String requestingUser = "testUser";

		//Replace null parameters with empty values
		if(excludedCorrections == null) {
			excludedCorrections = new ArrayList<>();
		}

		//Build the TSS Report JSON
		TimeSeriesSummaryReport report = reportBuilderService.buildReport(primaryTimeseriesIdentifier, excludedCorrections, AqcuTimeUtils.toReportStartTime(startDate), AqcuTimeUtils.toReportEndTime(endDate), requestingUser);

		return new ResponseEntity<TimeSeriesSummaryReport>(report, new HttpHeaders(), HttpStatus.OK);
	}
}
