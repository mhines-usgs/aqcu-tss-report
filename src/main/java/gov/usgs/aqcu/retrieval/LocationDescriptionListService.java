package gov.usgs.aqcu.retrieval;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gov.usgs.aqcu.exception.AquariusException;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;


@Component
public class LocationDescriptionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(LocationDescriptionListService.class);

	public LocationDescriptionListServiceResponse getRawResponse(String stationId) throws AquariusException {
		LocationDescriptionListServiceRequest request = new LocationDescriptionListServiceRequest()
			.setLocationIdentifier(stationId);
				
		LocationDescriptionListServiceResponse locationResponse = executePublishApiRequest(request);
		return locationResponse;
	}

	public List<LocationDescription> getLocationDescriptionList(String stationId) throws AquariusException {
		return getRawResponse(stationId).getLocationDescriptions();
	}
}
