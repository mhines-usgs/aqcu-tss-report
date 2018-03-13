package gov.usgs.aqcu.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gov.usgs.aqcu.exception.AquariusException;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;


@Component
public class LocationDescriptionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(LocationDescriptionListService.class);

	public LocationDescriptionListServiceResponse getRawResponse(String stationId) throws AquariusException {
		LocationDescriptionListServiceRequest request = new LocationDescriptionListServiceRequest()
			.setLocationIdentifier(stationId);
				
		LocationDescriptionListServiceResponse locationResponse = executePublishApiRequest(request);
		return locationResponse;
	}
}
