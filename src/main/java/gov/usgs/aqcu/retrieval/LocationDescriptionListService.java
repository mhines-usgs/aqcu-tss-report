package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescriptionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class LocationDescriptionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(LocationDescriptionListService.class);

	public LocationDescriptionListServiceResponse getRawResponse(String stationId) throws Exception {
		LocationDescriptionListServiceRequest request = new LocationDescriptionListServiceRequest()
			.setLocationIdentifier(stationId);
				
		LocationDescriptionListServiceResponse locationResponse = executePublishApiRequest(request);
		return locationResponse;
	}
	
	public List<LocationDescription> getLocationDescriptionList(String stationId) throws Exception {
		return getRawResponse(stationId).getLocationDescriptions();
	}
	
	public LocationDescription getFirstLocationDescription(String stationId) throws Exception {
		return getLocationDescriptionList(stationId).get(0);
	}
}
