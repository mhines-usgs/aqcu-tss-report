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

import gov.usgs.aqcu.model.ExtendedCorrection;
import gov.usgs.aqcu.model.ExtendedCorrectionType;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionType;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class CorrectionListServiceTest {
	@MockBean
	private AquariusRetrievalService aquariusService;
    private CorrectionListService service;
    
    public static final Correction CORR_A = new Correction()
        .setAppliedTimeUtc(Instant.parse("2017-01-01T00:00:00Z"))
        .setComment("comment-a")
        .setEndTime(Instant.parse("2017-01-02T00:00:00Z"))
        .setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
        .setParameters(new HashMap<>())
        .setProcessingOrder(CorrectionProcessingOrder.Normal)
        .setType(CorrectionType.Offset)
        .setUser("user-a");
	public static final Correction CORR_B = new Correction()
        .setAppliedTimeUtc(Instant.parse("2016-01-01T00:00:00Z"))
        .setComment("comment-b-freehand")
        .setEndTime(Instant.parse("2016-01-02T00:00:00Z"))
        .setStartTime(Instant.parse("2016-01-01T00:00:00Z"))
        .setParameters(new HashMap<>())
        .setProcessingOrder(CorrectionProcessingOrder.PostProcessing)
        .setType(CorrectionType.CopyPaste)
        .setUser("user-b");
    public static final ArrayList<Correction> CORR_LIST = new ArrayList<>(Arrays.asList(CORR_A, CORR_B));

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new CorrectionListService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new CorrectionListServiceResponse()
				.setCorrections(CORR_LIST));
	}

	@Test
	public void getRawResponseTest() {
		List<Correction> actual = service.getRawResponse("test-identifier", Instant.parse("2016-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z")).getCorrections();
		assertEquals(2, actual.size());
		assertThat(actual, containsInAnyOrder(CORR_A, CORR_B));
    }
    
    @Test
	public void getAqcuExtendedCorrectionListTest() {
		List<ExtendedCorrection> actual = service.getExtendedCorrectionList("test-identifier", Instant.parse("2016-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z"));
        assertEquals(2, actual.size());
        assertEquals(actual.get(0).getComment().compareTo(CORR_A.getComment()), 0);
        assertEquals(actual.get(0).getAqcuExtendedCorrectionType(), null);
        assertEquals(actual.get(0).getDominantType().compareTo(CORR_A.getType().toString()), 0);
        assertEquals(actual.get(1).getComment().compareTo(CORR_B.getComment()), 0);
        assertEquals(actual.get(1).getAqcuExtendedCorrectionType(), ExtendedCorrectionType.Freehand);
        assertEquals(actual.get(1).getDominantType().compareTo(ExtendedCorrectionType.Freehand.toString()), 0);
    }

    @Test
	public void getAqcuExtendedCorrectionListFilteredTest() {
		List<ExtendedCorrection> actual = service.getExtendedCorrectionList("test-identifier", Instant.parse("2016-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z"), Arrays.asList(CorrectionType.Offset.toString()));
        assertEquals(1, actual.size());
        assertEquals(actual.get(0).getComment().compareTo(CORR_B.getComment()), 0);
        assertEquals(actual.get(0).getAqcuExtendedCorrectionType(), ExtendedCorrectionType.Freehand);
        assertEquals(actual.get(0).getDominantType().compareTo(ExtendedCorrectionType.Freehand.toString()), 0);
    }
}