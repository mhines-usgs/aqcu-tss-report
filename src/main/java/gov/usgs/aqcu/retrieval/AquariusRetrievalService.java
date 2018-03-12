package gov.usgs.aqcu.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

import gov.usgs.aqcu.exception.AquariusRetrievalException;
import gov.usgs.aqcu.exception.AquariusProcessingException;

@Component
public class AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(LocationDescriptionListService.class);

	@Value("${aquarius.service.endpoint}")
	private String aquariusUrl;

	@Value("${aquarius.service.user}")
	private String aquariusUser;

	@Value("${aquarius.service.password}")
	private String aquariusPassword;

	protected <TResponse> TResponse executePublishApiRequest(IReturn<TResponse> request)  throws AquariusRetrievalException, AquariusProcessingException {
		try (AquariusClient client = AquariusClient.createConnectedClient(aquariusUrl.replaceAll("/AQUARIUS/", ""), aquariusUser, aquariusPassword)) {
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
			throw new AquariusRetrievalException(errorMessage);
		} catch (Exception e) {
			LOG.error("An unexpected error occurred while attempting to fetch data from Aquarius: ", e);
			throw new AquariusProcessingException(e.getMessage());
		}
	}
}
