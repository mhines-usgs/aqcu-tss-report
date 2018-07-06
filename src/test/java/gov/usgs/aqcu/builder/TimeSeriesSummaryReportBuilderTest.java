package gov.usgs.aqcu.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import gov.usgs.aqcu.model.ExtendedCorrection;
import gov.usgs.aqcu.model.TimeSeriesSummaryCorrectedData;
import gov.usgs.aqcu.model.TimeSeriesSummaryCorrections;
import gov.usgs.aqcu.model.TimeSeriesSummaryRatingShift;
import gov.usgs.aqcu.model.TimeSeriesSummaryRelatedSeries;
import gov.usgs.aqcu.model.TimeSeriesSummaryReport;
import gov.usgs.aqcu.model.TimeSeriesSummaryReportMetadata;
import gov.usgs.aqcu.parameter.TimeSeriesSummaryRequestParameters;
import gov.usgs.aqcu.retrieval.AquariusRetrievalService;
import gov.usgs.aqcu.retrieval.CorrectionListService;
import gov.usgs.aqcu.retrieval.CorrectionListServiceTest;
import gov.usgs.aqcu.retrieval.DownchainProcessorListService;
import gov.usgs.aqcu.retrieval.GradeLookupService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.RatingCurveListServiceTest;
import gov.usgs.aqcu.retrieval.TimeSeriesDataCorrectedService;
import gov.usgs.aqcu.retrieval.TimeSeriesDataCorrectedServiceTest;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListServiceTest;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListServiceTest;
import gov.usgs.aqcu.util.AqcuGsonBuilderFactory;
import gov.usgs.aqcu.retrieval.DownchainProcessorListServiceTest;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.google.gson.Gson;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class TimeSeriesSummaryReportBuilderTest {
	@Value("${aqcu.reports.webservice}")
	String aqcuWebserviceUrl;
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	@MockBean
	private DownchainProcessorListService downchainService;
	@MockBean 
	private UpchainProcessorListService upchainService;
	@MockBean
	private TimeSeriesDescriptionListService descService;
	@MockBean
	private TimeSeriesDataCorrectedService tsDataService;
	@MockBean
	private CorrectionListService corrListService;
	@MockBean
	private GradeLookupService gradeService;
	@MockBean
	private QualifierLookupService qualService;
	@MockBean
	private RatingCurveListService ratingService;
	@MockBean
	private LocationDescriptionListService locService;

	private Gson gson;
	private DataGapListBuilderService gapService;
	private ReportUrlBuilderService reportUrlBuilderService;

	private TimeSeriesSummaryReportBuilderService service;
	private final String REQUESTING_USER = "test-user";
	private TimeSeriesSummaryRequestParameters requestParams;
	TimeSeriesSummaryReportMetadata metadata;
	ArrayList<Processor> downProcessors = DownchainProcessorListServiceTest.PROCESSOR_LIST;
	ArrayList<Processor> upProcessors = UpchainProcessorListServiceTest.PROCESSOR_LIST;
	TimeSeriesDescription primaryDesc = TimeSeriesDescriptionListServiceTest.DESC_1;
	List<TimeSeriesDescription> upDescs = TimeSeriesDescriptionListServiceTest.DESC_LIST;
	List<TimeSeriesDescription> downDescs = TimeSeriesDescriptionListServiceTest.DESC_LIST;
	TimeSeriesDataServiceResponse primaryData = TimeSeriesDataCorrectedServiceTest.TS_DATA_RESPONSE;
	List<ExtendedCorrection> extCorrs;
	List<RatingCurve> ratingCurves;
	List<RatingShift> rawShifts;
	List<TimeSeriesSummaryRatingShift> ratingShifts;
	LocationDescription primaryLoc = new LocationDescription().setIdentifier(primaryDesc.getLocationIdentifier()).setName("loc-name");

	@Before
	public void setup() {
		//Builder Servies
		gson = AqcuGsonBuilderFactory.getConfiguredGsonBuilder().create();
		reportUrlBuilderService = new ReportUrlBuilderService();
		gapService = new DataGapListBuilderService();
		service = new TimeSeriesSummaryReportBuilderService(gapService, reportUrlBuilderService, gradeService, qualService, locService, descService, tsDataService, upchainService, downchainService, ratingService, corrListService);
		ReflectionTestUtils.setField(reportUrlBuilderService, "aqcuWebserviceUrl", aqcuWebserviceUrl);

		//Request Parameters
		requestParams = new TimeSeriesSummaryRequestParameters();
		requestParams.setStartDate(LocalDate.parse("2017-01-01"));
		requestParams.setEndDate(LocalDate.parse("2017-02-01"));
		requestParams.setPrimaryTimeseriesIdentifier(primaryDesc.getUniqueId());
		requestParams.setExcludedCorrections(Arrays.asList("corr1", "corr2"));

		//Metadata
		metadata = new TimeSeriesSummaryReportMetadata();
		metadata.setPrimaryParameter(primaryDesc.getIdentifier());
		metadata.setRequestParameters(requestParams);
		metadata.setStationId(primaryDesc.getLocationIdentifier());
		metadata.setStationName(primaryLoc.getName());
		metadata.setTimezone(primaryDesc.getUtcOffset());
		metadata.setTitle(TimeSeriesSummaryReportBuilderService.REPORT_TITLE);

		//Corrections
		extCorrs = new ArrayList<>();
		for(Correction corr : CorrectionListServiceTest.CORR_LIST) {
			extCorrs.add(new ExtendedCorrection(corr));
		}

		//Rating Curves
		ratingCurves = RatingCurveListServiceTest.CURVE_LIST;

		//Rating Shifts
		rawShifts = new ArrayList<>();
		ratingShifts = new ArrayList<>();
		for(RatingCurve curve : ratingCurves) {
			rawShifts.addAll(curve.getShifts());
			for(RatingShift shift : curve.getShifts()) {
				ratingShifts.add(new TimeSeriesSummaryRatingShift(shift, RatingCurveListServiceTest.CURVE_A.getId()));
			}
		}
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void buildReportBasicTest() {
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(upProcessors));
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(Arrays.asList(upProcessors.get(0).getOutputTimeSeriesUniqueId(), upProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(descService.getTimeSeriesDescription(any(String.class)))
			.willReturn(primaryDesc);
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(TimeSeriesDescriptionListServiceTest.DESC_LIST);
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(primaryData);
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);
		given(ratingService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class)))
			.willReturn(new RatingCurveListServiceResponse().setRatingCurves(ratingCurves));
		given(ratingService.getAqcuFilteredRatingCurves(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(ratingCurves);
		given(ratingService.getAqcuFilteredRatingShifts(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(rawShifts);
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		
		TimeSeriesSummaryReport report = service.buildReport(requestParams, REQUESTING_USER);
		assertTrue(report != null);
		assertTrue(report.getReportMetadata() != null);
		assertEquals(report.getReportMetadata().getRequestingUser(), REQUESTING_USER);
		assertEquals(report.getReportMetadata().getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getStartDate(), metadata.getStartDate());
		assertEquals(report.getReportMetadata().getEndDate(), metadata.getEndDate());		
		assertEquals(report.getCorrections().getCorrUrl(), reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", metadata.getStationId(), requestParams, null));
		assertEquals(report.getCorrections().getPreProcessing().size(), 0);
		assertEquals(report.getCorrections().getNormal().size(), 1);
		assertThat(report.getCorrections().getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(report.getCorrections().getPostProcessing().size(), 1);
		assertThat(report.getCorrections().getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
		assertEquals(report.getPrimaryTsData().getApprovals(), primaryData.getApprovals());
		assertEquals(report.getPrimaryTsData().getGaps(), new ArrayList<>());
		assertEquals(report.getPrimaryTsData().getGapTolerances(), primaryData.getGapTolerances());
		assertEquals(report.getPrimaryTsData().getGrades(), primaryData.getGrades());
		assertEquals(report.getPrimaryTsData().getInterpolationTypes(), primaryData.getInterpolationTypes());
		assertEquals(report.getPrimaryTsData().getMethods(), primaryData.getMethods());
		assertEquals(report.getPrimaryTsData().getNotes(), primaryData.getNotes());
		assertEquals(report.getPrimaryTsData().getProcessors(), upProcessors);
		assertEquals(report.getPrimaryTsData().getQualifiers(), primaryData.getQualifiers());
		assertEquals(report.getUpchainTs().size(), upDescs.size());
		assertEquals(report.getDownchainTs().size(), downDescs.size());
		assertEquals(report.getPrimaryTsMetadata(), primaryDesc);
		assertEquals(report.getReportMetadata().getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getIdentifier());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
		assertEquals(report.getRatingCurves(), ratingCurves);
		assertEquals(report.getRatingShifts().size(), ratingShifts.size());
		assertEquals(gson.toJson(report.getRatingShifts()), gson.toJson(ratingShifts));
		assertEquals(report.getReportMetadata().getGradeMetadata(), new HashMap<>());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void builderReportNoProcessorsTest() {
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(new ArrayList<>()));
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(new ArrayList<>());
		given(descService.getTimeSeriesDescription(any(String.class)))
			.willReturn(primaryDesc);
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(new ArrayList<>());
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(primaryData);
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		
		TimeSeriesSummaryReport report = service.buildReport(requestParams, REQUESTING_USER);
		assertTrue(report != null);
		assertTrue(report.getReportMetadata() != null);
		assertEquals(report.getReportMetadata().getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(report.getReportMetadata().getRequestingUser(), REQUESTING_USER);
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getStartDate(), metadata.getStartDate());
		assertEquals(report.getReportMetadata().getEndDate(), metadata.getEndDate());		
		assertEquals(report.getCorrections().getCorrUrl(), reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", metadata.getStationId(), requestParams, null));
		assertEquals(report.getCorrections().getPreProcessing().size(), 0);
		assertEquals(report.getCorrections().getNormal().size(), 1);
		assertThat(report.getCorrections().getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(report.getCorrections().getPostProcessing().size(), 1);
		assertThat(report.getCorrections().getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
		assertEquals(report.getPrimaryTsData().getApprovals(), primaryData.getApprovals());
		assertEquals(report.getPrimaryTsData().getGaps(), new ArrayList<>());
		assertEquals(report.getPrimaryTsData().getGapTolerances(), primaryData.getGapTolerances());
		assertEquals(report.getPrimaryTsData().getGrades(), primaryData.getGrades());
		assertEquals(report.getPrimaryTsData().getInterpolationTypes(), primaryData.getInterpolationTypes());
		assertEquals(report.getPrimaryTsData().getMethods(), primaryData.getMethods());
		assertEquals(report.getPrimaryTsData().getNotes(), primaryData.getNotes());
		assertEquals(report.getPrimaryTsData().getProcessors(), new ArrayList<>());
		assertEquals(report.getPrimaryTsData().getQualifiers(), primaryData.getQualifiers());
		assertEquals(report.getUpchainTs().size(), 0);
		assertEquals(report.getDownchainTs().size(), 0);
		assertEquals(report.getPrimaryTsMetadata(), primaryDesc);
		assertEquals(report.getReportMetadata().getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getIdentifier());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
		assertEquals(report.getRatingCurves(), new ArrayList<>());
		assertEquals(report.getRatingShifts(), new ArrayList<>());
		assertEquals(report.getReportMetadata().getGradeMetadata(), new HashMap<>());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
	}

	@Test
	public void getCorrectionDataTest() {
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);

		TimeSeriesSummaryCorrections corrs = service.getCorrectionsData(requestParams, ZoneOffset.UTC, metadata.getStationId());
		assertEquals(corrs.getCorrUrl(), reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", metadata.getStationId(), requestParams, null));
		assertEquals(corrs.getPreProcessing().size(), 0);
		assertEquals(corrs.getNormal().size(), 1);
		assertThat(corrs.getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(corrs.getPostProcessing().size(), 1);
		assertThat(corrs.getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getReportMetadataTest() {
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);

		TimeSeriesSummaryReportMetadata newMetadata = service.getReportMetadata(requestParams, REQUESTING_USER, primaryLoc.getIdentifier(), primaryDesc.getIdentifier(), primaryDesc.getUtcOffset(), new ArrayList<>(), new ArrayList<>());
		assertTrue(newMetadata != null);
		assertEquals(newMetadata.getRequestingUser(), REQUESTING_USER);
		assertEquals(newMetadata.getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(newMetadata.getRequestParameters(), metadata.getRequestParameters());
		assertEquals(newMetadata.getStartDate(), metadata.getStartDate());
		assertEquals(newMetadata.getEndDate(), metadata.getEndDate());
		assertEquals(newMetadata.getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(newMetadata.getStationName(), primaryLoc.getName());
		assertEquals(newMetadata.getPrimaryParameter(), primaryDesc.getIdentifier());
		assertEquals(newMetadata.getTimezone(), metadata.getTimezone());
		assertEquals(newMetadata.getGradeMetadata(), new HashMap<>());
		assertEquals(newMetadata.getQualifierMetadata(), new HashMap<>());
	}

	@Test
	public void getCorrectedDataTest() {
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(primaryData);
		TimeSeriesSummaryCorrectedData corrData = service.getCorrectedData(requestParams, ZoneOffset.UTC, upProcessors, false);
		assertTrue(corrData != null);
		assertEquals(corrData.getApprovals(), primaryData.getApprovals());
		assertEquals(corrData.getGaps(), new ArrayList<>());
		assertEquals(corrData.getGapTolerances(), primaryData.getGapTolerances());
		assertEquals(corrData.getGrades(), primaryData.getGrades());
		assertEquals(corrData.getInterpolationTypes(), primaryData.getInterpolationTypes());
		assertEquals(corrData.getMethods(), primaryData.getMethods());
		assertEquals(corrData.getNotes(), primaryData.getNotes());
		assertEquals(corrData.getProcessors(), upProcessors);
		assertEquals(corrData.getQualifiers(), primaryData.getQualifiers());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getDerivationChainTSUpchainTest() {
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(Arrays.asList(upProcessors.get(0).getOutputTimeSeriesUniqueId(), upProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(upDescs);
		
		List<TimeSeriesSummaryRelatedSeries> relatedSeries = service.getDerivationChainTS(true, requestParams, ZoneOffset.UTC, metadata.getStationId(), upProcessors);
		assertTrue(relatedSeries != null);
		assertTrue(relatedSeries.size() == upDescs.size());
		assertTrue(relatedSeries.get(0).getIdentifier().compareTo(upDescs.get(0).getIdentifier()) == 0);
		assertTrue(relatedSeries.get(1).getIdentifier().compareTo(upDescs.get(1).getIdentifier()) == 0);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getDerivationChainTSDownchainTest() {
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(descService.getTimeSeriesDescriptionList(any(List.class)))
			.willReturn(downDescs);
		
		List<TimeSeriesSummaryRelatedSeries> relatedSeries = service.getDerivationChainTS(false, requestParams, ZoneOffset.UTC, metadata.getStationId(), downProcessors);
		assertTrue(relatedSeries != null);
		assertTrue(relatedSeries.size() == downDescs.size());
		assertTrue(relatedSeries.get(0).getIdentifier().compareTo(downDescs.get(0).getIdentifier()) == 0);
		assertTrue(relatedSeries.get(1).getIdentifier().compareTo(downDescs.get(1).getIdentifier()) == 0);
	}

	@Test
	public void getProcessorsDownchainTest() {
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		List<Processor> result = service.getProcessors(false, requestParams, ZoneOffset.UTC);
		assertTrue(result != null);
		assertTrue(result.size() == downProcessors.size());
		assertTrue(result.containsAll(downProcessors));
	}

	@Test
	public void getProcessorsUpchainTest() {
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(ZoneOffset.UTC), requestParams.getEndInstant(ZoneOffset.UTC)))
			.willReturn(new ProcessorListServiceResponse().setProcessors(upProcessors));
			List<Processor> result = service.getProcessors(true, requestParams, ZoneOffset.UTC);
			assertTrue(result != null);
			assertTrue(result.size() == upProcessors.size());
			assertTrue(result.containsAll(upProcessors));
	}

	@Test
	public void getRatingModelTest() {
		String ratingModel = service.getRatingModel(upProcessors);
		assertEquals(ratingModel, upProcessors.get(0).getInputRatingModelIdentifier());
	}

	@Test
	public void getRatingModelNullTest() {
		String ratingModel = service.getRatingModel(new ArrayList<>());
		assertEquals(ratingModel, null);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRatingCurvesTest() {
		given(ratingService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class)))
			.willReturn(new RatingCurveListServiceResponse().setRatingCurves(ratingCurves));
		given(ratingService.getAqcuFilteredRatingCurves(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(ratingCurves);

		List<RatingCurve> resultCurves = service.getRatingCurves(requestParams, ZoneOffset.UTC, "primaryRatingModel");
		assertEquals(resultCurves, ratingCurves);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void getRatingShiftsTest() {
		given(ratingService.getAqcuFilteredRatingShifts(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(rawShifts);

		List<TimeSeriesSummaryRatingShift> resultShifts = service.getRatingShifts(requestParams, ZoneOffset.UTC, ratingCurves);
		assertEquals(resultShifts.size(), ratingShifts.size());
		assertEquals(gson.toJson(resultShifts), gson.toJson(ratingShifts));
	}

	@Test
	public void createTimeSeriesSummaryRelatedSeriesListTest() {
		HashMap<String,String> reportUrls = new HashMap<>();
		for(TimeSeriesDescription desc : upDescs) {
			reportUrls.put(desc.getUniqueId(), "url-" + desc.getUniqueId());
		}
		List<TimeSeriesSummaryRelatedSeries> related = service.createTimeSeriesSummaryRelatedSeriesList(upDescs, reportUrls);
		assertEquals(related.size(), upDescs.size());
		for(int i = 0; i < upDescs.size(); i++) {
			assertEquals(related.get(i).getIdentifier(), upDescs.get(i).getIdentifier());
			assertEquals(related.get(i).getUrl(), "url-" + upDescs.get(i).getUniqueId());
		}
	}
}
