package gov.usgs.aqcu;

import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import org.apache.commons.lang3.StringUtils;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
import net.servicestack.client.WebServiceException;
import gov.usgs.aqcu.exception.AquariusException;

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
	
	@GetMapping(produces={MediaType.APPLICATION_JSON_VALUE,MediaType.TEXT_HTML_VALUE})
	public ResponseEntity<?> getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@RequestParam(required=true) @DateTimeFormat(pattern=InstantDeserializer.Pattern) Instant startDate,
			@RequestParam(required=true) @DateTimeFormat(pattern=InstantDeserializer.Pattern) Instant endDate,
			@RequestParam(required=false) String excludedCorrections,
			@RequestHeader("Accept") MediaType acceptType) throws Exception {
		//Pull Requesting User From Headers
		String requestingUser = "testUser";

		//Parse Excluded Corrections
		List<String> excludedCorrectionsList = null;
		if(excludedCorrections != null && excludedCorrections.length() >0) {
			excludedCorrectionsList = Arrays.asList(excludedCorrections.split(","));
		}

		//Build the TSS Report JSON
		TimeSeriesSummaryReport report = reportBuilderService.buildReport(primaryTimeseriesIdentifier, excludedCorrectionsList, startDate, endDate, requestingUser);
			
		//Render HTML if requested
		if(acceptType == MediaType.TEXT_HTML) {
			byte[] reportHtml = javaToRClient.render(requestingUser, "timeseriessummary", gson.toJson(report, TimeSeriesSummaryReport.class));
			return new ResponseEntity<byte[]>(new byte[1], new HttpHeaders(), HttpStatus.OK);
		} else {
			return new ResponseEntity<TimeSeriesSummaryReport>(report, new HttpHeaders(), HttpStatus.OK);
		}
	}
}
