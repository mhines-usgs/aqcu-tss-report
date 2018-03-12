package gov.usgs.aqcu.retrieval;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.DownchainProcessorListByTimeSeriesServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import gov.usgs.aqcu.exception.AquariusException;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;

@Component
public class DownchainProcessorListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(UpchainProcessorListService.class);

	public ProcessorListServiceResponse getRawResponse(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws AquariusException {
				DownchainProcessorListByTimeSeriesServiceRequest request = new DownchainProcessorListByTimeSeriesServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		ProcessorListServiceResponse processorsResponse = executePublishApiRequest(request);
		return processorsResponse;
	}

	public List<Processor> getProcessorList(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws AquariusException {
		return getRawResponse(primaryTimeseriesIdentifier, startDate, endDate).getProcessors();
	}

	public List<String> getOutputTimeSeriesUniqueIdList(List<Processor> processors) {
		Set<String> uniqueIds = new HashSet<>();
		
		for(Processor proc : processors) {
			uniqueIds.add(proc.getOutputTimeSeriesUniqueId());
		}
		
		return new ArrayList<>(uniqueIds);
	}
}
