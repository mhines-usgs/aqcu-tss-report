package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class TimeSeriesMetadataService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesMetadataService.class);

	@Autowired
	private String aquariusUrl;

	@Autowired
	private String aquariusUser;

	@Autowired
	private String aquariusPassword;

	public TimeSeriesDescriptionListByUniqueIdServiceResponse get(String primaryTimeseriesIdentifier) {
		ArrayList<String> timeSeriesUniqueIds = new ArrayList<>(Arrays.asList(primaryTimeseriesIdentifier));

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
