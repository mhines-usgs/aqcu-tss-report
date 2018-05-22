package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;


import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ExtendedAttribute;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesThresholdPeriod;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesThreshold;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class TimeSeriesDescriptionListServiceTest {
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	private TimeSeriesDescriptionListService service;

	private static final TimeSeriesThresholdPeriod p1 = new TimeSeriesThresholdPeriod()
		.setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
		.setEndTime(Instant.parse("2017-02-01T00:00:00Z"))
		.setAppliedTime(Instant.parse("2017-02-01T00:00:00Z"))
		.setComments("comments-1")
		.setReferenceValue(1.0)
		.setSecondaryReferenceValue(2.0)
		.setSuppressData(false);

	private static final TimeSeriesThreshold threshold1 = new TimeSeriesThreshold()
		.setDescription("desc-1")
		.setDisplayColor("color1")
		.setName("name-1")
		.setPeriods(new ArrayList<TimeSeriesThresholdPeriod>(Arrays.asList(p1)));
	
	public static final TimeSeriesDescription DESC_1 = new TimeSeriesDescription()
		.setComment("comment-1")
		.setComputationIdentifier("comp-id-1")
		.setComputationPeriodIdentifier("period-id-1")
		.setDescription("desc-1")
		.setExtendedAttributes(Arrays.asList(new ExtendedAttribute().setName("name").setType("type").setValue("value")))
		.setIdentifier("id-1")
		.setLabel("label-1")
		.setLastModified(Instant.parse("2017-01-01T00:00:00Z"))
		.setLocationIdentifier("loc-id-1")
		.setParameter("param-1")
		.setPublish(false)
		.setRawEndTime(Instant.parse("2017-01-01T00:00:00Z"))
		.setRawStartTime(Instant.parse("2017-01-02T00:00:00Z"))
		.setSubLocationIdentifier("sub-id-1")
		.setThresholds(Arrays.asList(threshold1))
		.setTimeSeriesType("type-1")
		.setUniqueId("uid-1")
		.setUnit("unit-1")
		.setUtcOffset(0.0)
		.setUtcOffsetIsoDuration(Duration.ofHours(0));
	public static final TimeSeriesDescription DESC_2 = new TimeSeriesDescription()
		.setComment("comment-2")
		.setComputationIdentifier("comp-id-2")
		.setComputationPeriodIdentifier("period-id-2")
		.setDescription("desc-2")
		.setExtendedAttributes(Arrays.asList(new ExtendedAttribute().setName("name").setType("type").setValue("value")))
		.setIdentifier("id-2")
		.setLabel("label-2")
		.setLastModified(Instant.parse("2017-01-01T00:00:00Z"))
		.setLocationIdentifier("loc-id-2")
		.setParameter("param-2")
		.setPublish(true)
		.setRawEndTime(Instant.parse("2018-01-01T00:00:00Z"))
		.setRawStartTime(Instant.parse("2018-01-02T00:00:00Z"))
		.setSubLocationIdentifier("sub-id-2")
		.setThresholds(Arrays.asList(threshold1))
		.setTimeSeriesType("type-2")
		.setUniqueId("uid-2")
		.setUnit("unit-2")
		.setUtcOffset(2.0)
		.setUtcOffsetIsoDuration(Duration.ofHours(2));
	public static final ArrayList<TimeSeriesDescription> DESC_LIST = new ArrayList<>(Arrays.asList(DESC_1, DESC_2));

    @Before
	public void setup() {
		service = new TimeSeriesDescriptionListService(aquariusService);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRawResponseTest() {
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new TimeSeriesDescriptionListByUniqueIdServiceResponse()
				.setTimeSeriesDescriptions(new ArrayList<TimeSeriesDescription>(Arrays.asList(DESC_1, DESC_2))));
		List<TimeSeriesDescription> results = service.getRawResponse(Arrays.asList("ts-uid1", "ts-uid2")).getTimeSeriesDescriptions();
		assertEquals(results.size(), 2);
		assertThat(results, containsInAnyOrder(DESC_1, DESC_2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDescriptionListTest() {
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new TimeSeriesDescriptionListByUniqueIdServiceResponse()
				.setTimeSeriesDescriptions(new ArrayList<TimeSeriesDescription>(Arrays.asList(DESC_1, DESC_2))));
		List<TimeSeriesDescription> results = service.getTimeSeriesDescriptionList(Arrays.asList("ts-uid1", "ts-uid2"));
		assertEquals(results.size(), 2);
		assertThat(results, containsInAnyOrder(DESC_1, DESC_2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getTimeSeriesDescriptionTest() {
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new TimeSeriesDescriptionListByUniqueIdServiceResponse()
				.setTimeSeriesDescriptions(new ArrayList<TimeSeriesDescription>(Arrays.asList(DESC_1))));
		TimeSeriesDescription result = service.getTimeSeriesDescription("ts-uid1");
		assertEquals(result, DESC_1);
	}
}