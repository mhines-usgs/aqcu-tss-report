package gov.usgs.aqcu.retrieval;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class CorrectionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	public CorrectionListServiceResponse get(String timeseriesUniqueId, Instant startDate, Instant endDate) throws Exception {
		CorrectionListServiceRequest request = new CorrectionListServiceRequest()
				.setTimeSeriesUniqueId(timeseriesUniqueId)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		CorrectionListServiceResponse correctionListResponse = executePublishApiRequest(request);
		return correctionListResponse;
	}
}
