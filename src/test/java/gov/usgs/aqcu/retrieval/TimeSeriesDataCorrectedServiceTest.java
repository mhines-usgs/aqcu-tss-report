package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class TimeSeriesDataCorrectedServiceTest {
    
	@MockBean
	private AquariusRetrievalService aquariusService;
    private TimeSeriesDataCorrectedService service;

    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new TimeSeriesDataCorrectedService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new TimeSeriesDataServiceResponse()

		);
	}

	@Test
	public void getRawResponseTest() {
		
	}
}