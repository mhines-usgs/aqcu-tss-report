package gov.usgs.aqcu.retrieval;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataCorrectedServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import gov.usgs.aqcu.exception.AquariusException;

@Component
public class TimeSeriesDataCorrectedService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDataCorrectedService.class);

	public TimeSeriesDataServiceResponse getRawResponse(String primaryTimeseriesIdentifier, Instant startDate, Instant endDate) throws AquariusException {
		TimeSeriesDataCorrectedServiceRequest request = new TimeSeriesDataCorrectedServiceRequest()
				.setTimeSeriesUniqueId(primaryTimeseriesIdentifier)
				.setQueryFrom(startDate)
				.setIncludeGapMarkers(true)
				.setQueryTo(endDate);
		TimeSeriesDataServiceResponse timeSeriesResponse  = executePublishApiRequest(request);
		return timeSeriesResponse;
	}
}
