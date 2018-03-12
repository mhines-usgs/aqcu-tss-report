package gov.usgs.aqcu.retrieval;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.UpchainProcessorListByTimeSeriesServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;

@Component
public class UpchainProcessorListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(UpchainProcessorListService.class);

	public ProcessorListServiceResponse getRawResponse(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
				UpchainProcessorListByTimeSeriesServiceRequest request = new UpchainProcessorListByTimeSeriesServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		ProcessorListServiceResponse processorsResponse = executePublishApiRequest(request);
		return processorsResponse;
	}

	public List<Processor> getProcessorList(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
		return getRawResponse(primaryTimeseriesIdentifier, startDate, endDate).getProcessors();
	}

	public List<String> getInputTimeSeriesUniqueIdList(ProcessorListServiceResponse response) {
		Set<String> uniqueIds = new HashSet<>();

		for(Processor proc : response.getProcessors()) {
			uniqueIds.addAll(proc.getInputTimeSeriesUniqueIds());
		}

		return new ArrayList<>(uniqueIds);
	}

	public List<String> getInputTimeSeriesUniqueIdList(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
		return getInputTimeSeriesUniqueIdList(getRawResponse(primaryTimeseriesIdentifier, startDate, endDate));
	}

	public List<String> getRatingModelUniqueIdList(ProcessorListServiceResponse response) {
		Set<String> uniqueIds = new HashSet<>();

		for(Processor proc : response.getProcessors()) {
			uniqueIds.add(proc.getInputRatingModelIdentifier());
		}

		return new ArrayList<>(uniqueIds);
	}

	public List<String> getRatingModelUniqueIdList(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws Exception {
		return getRatingModelUniqueIdList(getRawResponse(primaryTimeseriesIdentifier, startDate, endDate));
	}
}
