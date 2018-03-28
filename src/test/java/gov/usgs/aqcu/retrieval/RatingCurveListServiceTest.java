package gov.usgs.aqcu.retrieval;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ParameterWithUnit;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.OffsetPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShiftPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.PeriodOfApplicability;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveType;

import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class RatingCurveListServiceTest {
	@MockBean
	private AquariusRetrievalService aquariusService;
	private RatingCurveListService service;

	Instant startTime = Instant.parse("2017-01-01T00:00:00Z");
	Instant endTime = Instant.parse("2017-01-10T00:00:00Z");

	PeriodOfApplicability periodA = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
		.setEndTime(Instant.parse("2017-01-02T00:00:00Z"))
		.setRemarks("premarks-a");
	PeriodOfApplicability periodB = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-02T00:00:00Z"))
		.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
		.setRemarks("premarks-b");
	PeriodOfApplicability periodC = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-04T00:00:00Z"))
		.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
		.setRemarks("premarks-a");
	PeriodOfApplicability periodD = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-05T00:00:00Z"))
		.setEndTime(Instant.parse("2017-01-07T00:00:00Z"))
		.setRemarks("premarks-b");
	PeriodOfApplicability periodE = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-07T00:00:00Z"))
		.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
		.setRemarks("premarks-a");
	PeriodOfApplicability periodF = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-10T00:00:00Z"))
		.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
		.setRemarks("premarks-b");
	PeriodOfApplicability periodG = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-11T00:00:00Z"))
		.setEndTime(Instant.parse("2017-01-12T00:00:00Z"))
		.setRemarks("premarks-a");
	PeriodOfApplicability periodH = new PeriodOfApplicability()
		.setStartTime(Instant.parse("2017-01-12T00:00:00Z"))
		.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
		.setRemarks("premarks-b");

	RatingShift shiftA1 = new RatingShift()
		.setPeriodOfApplicability(periodA)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftA2 = new RatingShift()
		.setPeriodOfApplicability(periodB)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftA3 = new RatingShift()
		.setPeriodOfApplicability(periodE)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftA4 = new RatingShift()
		.setPeriodOfApplicability(periodF)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftB1 = new RatingShift()
		.setPeriodOfApplicability(periodC)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftB2 = new RatingShift()
		.setPeriodOfApplicability(periodD)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));
	RatingShift shiftC1 = new RatingShift()
		.setPeriodOfApplicability(periodG)
		.setShiftPoints(new ArrayList<>(Arrays.asList(new RatingShiftPoint().setInputValue(1.0).setShift(1.0))));

	RatingCurve curveA = new RatingCurve()
		.setBaseRatingTable(new ArrayList<>(Arrays.asList(new RatingPoint().setInputValue(1.0).setOutputValue(1.0))))
		.setEquation("equation-a")
		.setId("id-a")
		.setInputParameter(new ParameterWithUnit().setParameterName("param-in-a").setParameterUnit("param-unit-a"))
		.setOffsets(new ArrayList<>(Arrays.asList(new OffsetPoint().setInputValue(1.0).setOffset(1.0))))
		.setOutputParameter(new ParameterWithUnit().setParameterName("param-out-a").setParameterUnit("param-unit-a"))
		.setPeriodsOfApplicability(new ArrayList<>(Arrays.asList(periodA, periodB, periodE)))
		.setRemarks("remarks-a")
		.setShifts(new ArrayList<>(Arrays.asList(shiftA1, shiftA2, shiftA3, shiftA4)))
		.setType(RatingCurveType.LinearTable);
	RatingCurve curveB = new RatingCurve()
		.setBaseRatingTable(new ArrayList<>(Arrays.asList(new RatingPoint().setInputValue(1.0).setOutputValue(1.0))))
		.setEquation("equation-a")
		.setId("id-a")
		.setInputParameter(new ParameterWithUnit().setParameterName("param-in-a").setParameterUnit("param-unit-a"))
		.setOffsets(new ArrayList<>(Arrays.asList(new OffsetPoint().setInputValue(1.0).setOffset(1.0))))
		.setOutputParameter(new ParameterWithUnit().setParameterName("param-out-a").setParameterUnit("param-unit-a"))
		.setPeriodsOfApplicability(new ArrayList<>(Arrays.asList(periodC, periodD)))
		.setRemarks("remarks-a")
		.setShifts(new ArrayList<>(Arrays.asList(shiftB1, shiftB2)))
		.setType(RatingCurveType.LinearTable);
	RatingCurve curveC = new RatingCurve()
		.setBaseRatingTable(new ArrayList<>(Arrays.asList(new RatingPoint().setInputValue(1.0).setOutputValue(1.0))))
		.setEquation("equation-a")
		.setId("id-a")
		.setInputParameter(new ParameterWithUnit().setParameterName("param-in-a").setParameterUnit("param-unit-a"))
		.setOffsets(new ArrayList<>(Arrays.asList(new OffsetPoint().setInputValue(1.0).setOffset(1.0))))
		.setOutputParameter(new ParameterWithUnit().setParameterName("param-out-a").setParameterUnit("param-unit-a"))
		.setPeriodsOfApplicability(new ArrayList<>(Arrays.asList(periodG)))
		.setRemarks("remarks-a")
		.setShifts(new ArrayList<>(Arrays.asList(shiftC1)))
		.setType(RatingCurveType.LinearTable);
	RatingCurve curveD = new RatingCurve()
		.setBaseRatingTable(new ArrayList<>(Arrays.asList(new RatingPoint().setInputValue(1.0).setOutputValue(1.0))))
		.setEquation("equation-a")
		.setId("id-a")
		.setInputParameter(new ParameterWithUnit().setParameterName("param-in-a").setParameterUnit("param-unit-a"))
		.setOffsets(new ArrayList<>(Arrays.asList(new OffsetPoint().setInputValue(1.0).setOffset(1.0))))
		.setOutputParameter(new ParameterWithUnit().setParameterName("param-out-a").setParameterUnit("param-unit-a"))
		.setPeriodsOfApplicability(new ArrayList<>(Arrays.asList(periodH)))
		.setRemarks("remarks-a")
		.setShifts(new ArrayList<>())
		.setType(RatingCurveType.LinearTable);

    @Before
	@SuppressWarnings("unchecked")
	public void setup() {
		service = new RatingCurveListService(aquariusService);
		given(aquariusService.executePublishApiRequest(any(IReturn.class))).willReturn(new RatingCurveListServiceResponse()
				.setRatingCurves(new ArrayList<RatingCurve>(Arrays.asList(curveA, curveB, curveC, curveD))));
	}

	@Test
	public void getRawResponseTest() {
		List<RatingCurve> results = service.getRawResponse("rating-model", null, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z")).getRatingCurves();
		assertEquals(results.size(), 4);
		assertThat(results, containsInAnyOrder(curveA, curveB, curveC, curveD));
	}

	@Test
	public void getAqcuFilteredRatingCurvesTest() {
		List<RatingCurve> raw = service.getRawResponse("rating-model", null, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z")).getRatingCurves();
		List<RatingCurve> filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 4);
		assertThat(filtered, containsInAnyOrder(curveA, curveB, curveC, curveD));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-12T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(curveA, curveB, curveC));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-11T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(curveA, curveB, curveC));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-07T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(curveA, curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-05T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(curveA, curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-04T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(curveA, curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-03T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(curveA, curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-02T00:00:00Z"));
		assertEquals(filtered.size(), 1);
		assertThat(filtered, containsInAnyOrder(curveA));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-04T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(curveA, curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-05T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(filtered.size(), 1);
		assertThat(filtered, containsInAnyOrder(curveB));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-07T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(curveA, curveC, curveD));
		filtered = service.getAqcuFilteredRatingCurves(raw, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 1);
		assertThat(filtered, containsInAnyOrder(curveD));
	}

	@Test
	public void getAqcuFilteredRatingShiftsTest() {
		List<RatingCurve> raw = service.getRawResponse("rating-model", null, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-02-01T00:00:00Z")).getRatingCurves();
		List<RatingShift> filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 7);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftA3, shiftA4, shiftB1, shiftB2, shiftC1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-12T00:00:00Z"));
		assertEquals(filtered.size(), 7);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftA3, shiftA4, shiftB1, shiftB2, shiftC1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-11T00:00:00Z"));
		assertEquals(filtered.size(), 7);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftA3, shiftA4, shiftB1, shiftB2, shiftC1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-07T00:00:00Z"));
		assertEquals(filtered.size(), 4);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftB1, shiftB2));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-05T00:00:00Z"));
		assertEquals(filtered.size(), 4);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftB1, shiftB2));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-04T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftB1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-03T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(shiftA1, shiftA2, shiftB1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-02T00:00:00Z"));
		assertEquals(filtered.size(), 1);
		assertThat(filtered, containsInAnyOrder(shiftA1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-04T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(shiftA2, shiftB1, shiftB2));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-05T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(filtered.size(), 2);
		assertThat(filtered, containsInAnyOrder(shiftB1, shiftB2));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-07T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 3);
		assertThat(filtered, containsInAnyOrder(shiftA3, shiftA4, shiftC1));
		filtered = service.getAqcuFilteredRatingShifts(raw, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(filtered.size(), 0);
	}

	@Test
	public void getRatingPeriodsWithinReportRangeTestOrdered() {
		ImmutablePair<Integer,PeriodOfApplicability> pa = new ImmutablePair<>(1, periodA);
		ImmutablePair<Integer,PeriodOfApplicability> pb = new ImmutablePair<>(2, periodB);
		ImmutablePair<Integer,PeriodOfApplicability> pc = new ImmutablePair<>(3, periodC);
		ImmutablePair<Integer,PeriodOfApplicability> pd = new ImmutablePair<>(4, periodD);
		ImmutablePair<Integer,PeriodOfApplicability> pe = new ImmutablePair<>(4, periodE);
		ImmutablePair<Integer,PeriodOfApplicability> pf = new ImmutablePair<>(4, periodF);
		ImmutablePair<Integer,PeriodOfApplicability> pg = new ImmutablePair<>(4, periodG);
		ImmutablePair<Integer,PeriodOfApplicability> ph = new ImmutablePair<>(4, periodH);
		List<ImmutablePair<Integer,PeriodOfApplicability>> fullPairSet = Arrays.asList(pa,pb,pc,pd,pe,pf,pg,ph);
		List<ImmutablePair<Integer,PeriodOfApplicability>> result;

		//Ordered
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 8);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-02T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-03T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-04T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-05T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-06T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-07T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-08T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-09T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-10T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-11T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 3);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 1);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-12T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-11T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-10T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-09T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-08T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-07T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-05T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSet, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-12T23:59:59Z"));
		assertEquals(result.size(), 1);
	}

	
	@Test
	public void getRatingPeriodsWithinReportRangeTestUnordered() {
		ImmutablePair<Integer,PeriodOfApplicability> pa = new ImmutablePair<>(1, periodA);
		ImmutablePair<Integer,PeriodOfApplicability> pb = new ImmutablePair<>(2, periodB);
		ImmutablePair<Integer,PeriodOfApplicability> pc = new ImmutablePair<>(3, periodC);
		ImmutablePair<Integer,PeriodOfApplicability> pd = new ImmutablePair<>(4, periodD);
		ImmutablePair<Integer,PeriodOfApplicability> pe = new ImmutablePair<>(4, periodE);
		ImmutablePair<Integer,PeriodOfApplicability> pf = new ImmutablePair<>(4, periodF);
		ImmutablePair<Integer,PeriodOfApplicability> pg = new ImmutablePair<>(4, periodG);
		ImmutablePair<Integer,PeriodOfApplicability> ph = new ImmutablePair<>(4, periodH);
		List<ImmutablePair<Integer,PeriodOfApplicability>> fullPairSetUnordered = Arrays.asList(pf,pa,ph,pc,pd,pb,pe,pg);
		List<ImmutablePair<Integer,PeriodOfApplicability>> result;

		//Unordered
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 8);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-02T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-03T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-04T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-05T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-06T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-07T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-08T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-09T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-10T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-11T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 3);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-13T00:00:00Z"));
		assertEquals(result.size(), 1);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-12T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-11T00:00:00Z"));
		assertEquals(result.size(), 7);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-10T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-09T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-08T00:00:00Z"));
		assertEquals(result.size(), 6);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-07T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-06T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-01T00:00:00Z"), Instant.parse("2017-01-05T00:00:00Z"));
		assertEquals(result.size(), 4);
		result = service.getRatingPeriodsWithinReportRange(fullPairSetUnordered, Instant.parse("2017-01-12T00:00:00Z"), Instant.parse("2017-01-12T23:59:59Z"));
		assertEquals(result.size(), 1);
	}

	@Test
	public void createEffectiveRatingPeriodTest() {
		PeriodOfApplicability periodA = new PeriodOfApplicability()
			.setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
			.setEndTime(Instant.parse("2017-01-02T00:00:00Z"))
			.setRemarks("premarks-a");
		PeriodOfApplicability periodB = new PeriodOfApplicability()
			.setStartTime(Instant.parse("2017-01-01T00:00:00Z"))
			.setEndTime(Instant.parse("9999-12-31T23:59:59.9999999Z"))
			.setRemarks("premarks-b");
		PeriodOfApplicability ef1 = service.createEffectiveRatingPeriod(periodA, null);
		PeriodOfApplicability ef2 = service.createEffectiveRatingPeriod(periodB, null);
		PeriodOfApplicability ef3 = service.createEffectiveRatingPeriod(periodA, periodB);
		PeriodOfApplicability ef4 = service.createEffectiveRatingPeriod(periodB, periodA);
		assertEquals(ef1.getRemarks().compareTo(periodA.getRemarks()), 0);
		assertEquals(ef2.getRemarks().compareTo(periodB.getRemarks()), 0);
		assertEquals(ef3.getRemarks().compareTo(periodA.getRemarks()), 0);
		assertEquals(ef4.getRemarks().compareTo(periodB.getRemarks()), 0);
		assertEquals(ef1.getStartTime(), periodA.getStartTime());
		assertEquals(ef2.getStartTime(), periodB.getStartTime());
		assertEquals(ef3.getStartTime(), periodA.getStartTime());
		assertEquals(ef4.getStartTime(), periodB.getStartTime());
		assertEquals(ef1.getEndTime(), periodA.getEndTime());
		assertEquals(ef2.getEndTime(), periodB.getEndTime());
		assertEquals(ef3.getEndTime(), periodA.getEndTime());
		assertEquals(ef4.getEndTime(), periodA.getStartTime());
	}
}