package gov.usgs.aqcu.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListServiceResponse;
import java.time.Instant;
import java.util.ArrayList;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class TimeSeriesDescriptionService {
private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDescriptionServiceRequest.class);

	@Autowired
	private String aquariusUrl;

	@Autowired
	private String aquariusUser;

	@Autowired
	private String aquariusPassword;
	
	public TimeSeriesDescriptionListServiceResponse get(String locationIdentifier, String primaryTimeseriesIdentifier, Boolean publish, String computationIdentifier, String computationPeriodIdentifier, ArrayList extendedFilters ) {
		TimeSeriesDescriptionServiceRequest request = new TimeSeriesDescriptionServiceRequest()
				.setLocationIdentifier(locationIdentifier)
				.setParameter(primaryTimeseriesIdentifier)
				.setPublish(publish)
				.setComputationIdentifier(computationIdentifier)
				.setComputationPeriodIdentifier(computationPeriodIdentifier)
				.setExtendedFilters(extendedFilters);
	
	TimeSeriesDescriptionListServiceResponse timeSeriesDescriptionResponse = executePublishApiRequest(request);
	return timeSeriesDescriptionResponse;
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

