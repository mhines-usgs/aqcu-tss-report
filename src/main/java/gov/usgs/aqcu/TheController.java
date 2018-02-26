package gov.usgs.aqcu;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@RestController
@RequestMapping("/timeseriessummary")
public class TheController {
	private static final Logger LOG = LoggerFactory.getLogger(TheController.class);

	@Autowired
	private String aquariusUrl;

	@Autowired
	private String aquariusUser;

	@Autowired
	private String aquariusPassword;

	@GetMapping
	public TimeSeriesDescriptionListByUniqueIdServiceResponse getReport(
			@RequestParam String primaryTimeseriesIdentifier,
			@RequestParam String station,
			@RequestParam(required=false) String lastMonths,
			@RequestParam(required=false) String waterYear,
			@RequestParam(required=false) String startDate,
			@RequestParam(required=false) String endDate,
			@RequestParam(required=false) String excludedCorrections) {

		ArrayList<String> timeSeriesUniqueIds = new ArrayList<>(Arrays.asList(primaryTimeseriesIdentifier));

		//Get Time Series Unique Ids
		TimeSeriesDescriptionListByUniqueIdServiceRequest request = new TimeSeriesDescriptionListByUniqueIdServiceRequest()
				.setTimeSeriesUniqueIds(timeSeriesUniqueIds);
		TimeSeriesDescriptionListByUniqueIdServiceResponse tssDesc = executePublishApiRequest(request);
		return tssDesc;
	}

	public <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request) {
		try (AquariusClient client = AquariusClient.createConnectedClient(aquariusUrl, aquariusUser, aquariusPassword)) {
			return client.Publish.get(request);
		} catch (WebServiceException e) {
			LOG.error("A web service error occurred while fetching data from Aquarius: \nStatus: " + e.getStatusCode() + "\nError Code: "
					+ e.getErrorCode() + "\nMessage: " + e.getErrorMessage() + "\n");
			return null;
		} catch (Exception e) {
			LOG.error("An unexpected error occurred while attempting to fetch data from Aquarius: ", e);
			return null;
		}
	}

}
