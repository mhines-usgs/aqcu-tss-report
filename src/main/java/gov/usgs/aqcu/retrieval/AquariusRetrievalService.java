package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Arrays;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

import gov.usgs.aqcu.exception.AquariusException;

@Component
public class AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(LocationDescriptionListService.class);

	@Autowired
	private String aquariusUrl;

	@Autowired
	private String aquariusUser;

	@Autowired
	private String aquariusPassword;

	protected <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request)  throws Exception {
		try (AquariusClient client = AquariusClient.createConnectedClient(aquariusUrl, aquariusUser, aquariusPassword)) {
			return client.Publish.get(request);
		} catch (WebServiceException e) {
			String errorMessage = "A Web Service Exception occurred while executing a Publish API Request against Aquarius:\n{" +
			"\nAquarius Instance: " + aquariusUrl +
			"\nRequest: " + request.toString() +
			"\nStatus: " + e.getStatusCode() + 
			"\nDescription: " + e.getStatusDescription() +
			"\nCause: " + e.getErrorMessage() +
			"\nDetails: " + e.getServerStackTrace() + "\n}\n";
			LOG.error(errorMessage);
			throw new AquariusException(errorMessage);
		} catch (Exception e) {
			LOG.error("An unexpected error occurred while attempting to fetch data from Aquarius: ", e);
			throw e;
		}
	}
}
