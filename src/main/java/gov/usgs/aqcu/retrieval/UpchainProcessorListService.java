package gov.usgs.aqcu.retrieval;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.UpchainProcessorListByTimeSeriesServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;

@Component
public class UpchainProcessorListService {
	private static final Logger LOG = LoggerFactory.getLogger(UpchainProcessorListService.class);

	private AquariusRetrievalService aquariusRetrievalService;

	@Autowired
	public UpchainProcessorListService(
		AquariusRetrievalService aquariusRetrievalService
	) {
		this.aquariusRetrievalService = aquariusRetrievalService;
	}
	
	public ProcessorListServiceResponse getRawResponse(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) {
				UpchainProcessorListByTimeSeriesServiceRequest request = new UpchainProcessorListByTimeSeriesServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		ProcessorListServiceResponse processorsResponse = aquariusRetrievalService.executePublishApiRequest(request);
		return processorsResponse;
	}

	public List<String> getInputTimeSeriesUniqueIdList(List<Processor> processors) {
		Set<String> uniqueIds = new HashSet<>();

		for(Processor proc : processors) {
			uniqueIds.addAll(proc.getInputTimeSeriesUniqueIds());
		}

		return new ArrayList<>(uniqueIds);
	}

	public List<String> getRatingModelUniqueIdList(List<Processor> processors) {
		Set<String> uniqueIds = new HashSet<>();

		for(Processor proc : processors) {
			uniqueIds.add(proc.getInputRatingModelIdentifier());
		}

		return new ArrayList<>(uniqueIds);
	}
}
