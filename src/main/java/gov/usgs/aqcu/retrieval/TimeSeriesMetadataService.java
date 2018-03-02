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
public class TimeSeriesMetadataService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesMetadataService.class);

	public TimeSeriesDescriptionListByUniqueIdServiceResponse get(String primaryTimeseriesIdentifier) throws Exception {
		ArrayList<String> timeSeriesUniqueIds = new ArrayList<>(Arrays.asList(primaryTimeseriesIdentifier));

		TimeSeriesDescriptionListByUniqueIdServiceRequest request = new TimeSeriesDescriptionListByUniqueIdServiceRequest()
				.setTimeSeriesUniqueIds(timeSeriesUniqueIds);
		TimeSeriesDescriptionListByUniqueIdServiceResponse tssDesc = executePublishApiRequest(request);
		return tssDesc;
	}
}
