package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Arrays;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.UpchainProcessorListByTimeSeriesServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class UpchainProcessorListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(UpchainProcessorListService.class);

	public ProcessorListServiceResponse get(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
				UpchainProcessorListByTimeSeriesServiceRequest request = new UpchainProcessorListByTimeSeriesServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		ProcessorListServiceResponse processorsResponse = executePublishApiRequest(request);
		return processorsResponse;
	}
}
