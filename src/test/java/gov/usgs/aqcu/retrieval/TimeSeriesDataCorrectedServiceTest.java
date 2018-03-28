package gov.usgs.aqcu.retrieval;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.InterpolationType;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Method;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Note;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalDateTimeOffset;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.StatisticalTimeRange;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Approval;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.DoubleWithDisplay;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.GapTolerance;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Grade;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class TimeSeriesDataCorrectedServiceTest {
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	private TimeSeriesDataCorrectedService service;
	private static final ArrayList<Approval> approvals = new ArrayList<>(Arrays.asList(
		new Approval()
			.setApprovalLevel(1)
			.setComment("test-1")
			.setDateAppliedUtc(Instant.parse("2017-01-01T00:00:00Z"))
			.setLevelDescription("desc-1")
			.setUser("user-1"),
		new Approval()
			.setApprovalLevel(2)
			.setComment("test-2")
			.setDateAppliedUtc(Instant.parse("2017-01-01T00:00:00Z"))
			.setLevelDescription("desc-2")
			.setUser("user-2")
	));
	private static final ArrayList<GapTolerance> gapTolerances = new ArrayList<>(Arrays.asList(
		new GapTolerance()
			.setToleranceInMinutes(2.0)
	));
	private static final ArrayList<Grade> grades = new ArrayList<>(Arrays.asList(
		new Grade()
			.setGradeCode("1.0")
	));
	private static final ArrayList<InterpolationType> interps = new ArrayList<>(Arrays.asList(
		new InterpolationType()
			.setType("type")
	));
	private static final ArrayList<Method> methods = new ArrayList<>(Arrays.asList(
		new Method()
			.setMethodCode("1.0")
	));
	private static final ArrayList<Note> notes = new ArrayList<>(Arrays.asList(
		new Note()
			.setNoteText("note text")
	));
	private static final ArrayList<TimeSeriesPoint> points = new ArrayList<>(Arrays.asList(
		new TimeSeriesPoint()
			.setTimestamp(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2017-01-01T00:00:00Z")))
			.setValue(new DoubleWithDisplay().setDisplay("1").setNumeric(1.0))
	));
	
	public static final TimeSeriesDataServiceResponse TS_DATA_RESPONSE = new TimeSeriesDataServiceResponse()
		.setApprovals(approvals)
		.setGapTolerances(gapTolerances)
		.setGrades(grades)
		.setInterpolationTypes(interps)
		.setLabel("label")
		.setLocationIdentifier("loc-id")
		.setMethods(methods)
		.setNotes(notes)
		.setNumPoints(new Long("1"))
		.setParameter("param")
		.setPoints(points)
		.setQualifiers(new ArrayList<>())
		.setTimeRange(new StatisticalTimeRange()
			.setStartTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2017-01-01T00:00:00Z")))
			.setEndTime(new StatisticalDateTimeOffset().setDateTimeOffset(Instant.parse("2017-03-01T00:00:00Z"))))
		.setUniqueId("uuid")
		.setUnit("unit");	

    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new TimeSeriesDataCorrectedService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(TS_DATA_RESPONSE);
	}

	@Test
	public void getRawResponseTest() {
		TimeSeriesDataServiceResponse result = service.getRawResponse("tsid", Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-03-01T00:00:00Z"));
		assertEquals(result, TS_DATA_RESPONSE);
	}
}