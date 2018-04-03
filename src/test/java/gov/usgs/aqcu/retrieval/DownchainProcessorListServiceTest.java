package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeRange;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class DownchainProcessorListServiceTest {
	@MockBean
	private AquariusRetrievalService aquariusService;
    private DownchainProcessorListService service;
    public static final Processor PROC_A = new Processor()
        .setDescription("desc-a")
        .setInputRatingModelIdentifier("rating-a")
        .setInputTimeSeriesUniqueIds(new ArrayList<String>(Arrays.asList("ts-in-a1", "ts-in-a2")))
        .setOutputTimeSeriesUniqueId("ts-out-a")
        .setProcessorPeriod(new TimeRange().setStartTime(Instant.parse("2017-01-01T00:00:00Z")).setEndTime(Instant.parse("2017-02-01T00:00:00Z")))
        .setProcessorType("type-a")
        .setSettings(new HashMap<>());
    public static final Processor PROC_B = new Processor()
        .setDescription("desc-b")
        .setInputRatingModelIdentifier("rating-b")
        .setInputTimeSeriesUniqueIds(new ArrayList<String>(Arrays.asList("ts-in-b1", "ts-in-b2")))
        .setOutputTimeSeriesUniqueId("ts-out-b")
        .setProcessorPeriod(new TimeRange().setStartTime(Instant.parse("2017-01-01T00:00:00Z")).setEndTime(Instant.parse("2017-02-01T00:00:00Z")))
        .setProcessorType("type-b")
        .setSettings(new HashMap<>());
    public static final ArrayList<Processor> PROCESSOR_LIST = new ArrayList<>(Arrays.asList(PROC_A, PROC_B));


    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new DownchainProcessorListService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new ProcessorListServiceResponse()
				.setProcessors(PROCESSOR_LIST));
    }
    
    @Test
    public void getRawResponseTest() {
        List<Processor> actual = service.getRawResponse("ts-identifier", Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z")).getProcessors();
		assertThat(actual, containsInAnyOrder(PROC_A, PROC_B));
    }

    @Test
    public void getOutputTimeSeriesUniqueIdListTest() {
        List<String> outputTsIdList = service.getOutputTimeSeriesUniqueIdList(PROCESSOR_LIST);
        assertThat(outputTsIdList, containsInAnyOrder("ts-out-a", "ts-out-b"));
    }
}