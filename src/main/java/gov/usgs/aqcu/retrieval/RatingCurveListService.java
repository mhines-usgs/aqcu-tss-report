package gov.usgs.aqcu.retrieval;

import java.util.ArrayList;
import java.util.Arrays;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class RatingCurveListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(RatingCurveListService.class);

	public RatingCurveListServiceResponse get(String ratingModelIdentifier, Double utcOffset, Instant startDate, Instant endDate) throws Exception {
		RatingCurveListServiceRequest request = new RatingCurveListServiceRequest()
				.setRatingModelIdentifier(ratingModelIdentifier)
				.setUtcOffset(utcOffset)
				.setQueryFrom(startDate)
				.setQueryTo(endDate);
		RatingCurveListServiceResponse ratingCurveResponse = executePublishApiRequest(request);
		return ratingCurveResponse;
	}
}
