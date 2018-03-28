package gov.usgs.aqcu.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.aqcu.model.DataGap;
import gov.usgs.aqcu.model.ExtendedCorrection;
import gov.usgs.aqcu.model.ReportMetadata;
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
import gov.usgs.aqcu.retrieval.LocationDescriptionService;
import gov.usgs.aqcu.retrieval.QualifierLookupService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.RatingCurveListServiceTest;
import gov.usgs.aqcu.retrieval.TimeSeriesDataCorrectedService;
import gov.usgs.aqcu.retrieval.TimeSeriesDataCorrectedServiceTest;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListService;
import gov.usgs.aqcu.retrieval.TimeSeriesDescriptionListServiceTest;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListServiceTest;
import gov.usgs.aqcu.retrieval.DownchainProcessorListServiceTest;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ProcessorListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurve;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingShift;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionListServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeRange;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.RatingCurveListServiceResponse;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeSerializer;
import com.google.gson.Gson;

import net.servicestack.client.IReturn;

@SpringBootTest
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
	private LocationDescriptionService locService;
	
	@Autowired
	ReportUrlBuilderService reportUrlBuilderService;
	@Autowired
	Gson gson;

	private DataGapListBuilderService gapService;

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
	HashMap<String, List<TimeSeriesDescription>> descriptionMap;
	List<ExtendedCorrection> extCorrs;
	List<RatingCurve> ratingCurves;
	List<RatingShift> rawShifts;
	List<TimeSeriesSummaryRatingShift> ratingShifts;
	LocationDescription primaryLoc = new LocationDescription().setIdentifier(primaryDesc.getUniqueId()).setName("loc-name");

	@Before
	@SuppressWarnings("unchecked")
	public void setup() {
		//Builder Servies		
		gapService = new DataGapListBuilderService();
		service = new TimeSeriesSummaryReportBuilderService(gapService, reportUrlBuilderService, gradeService, qualService, locService, descService, tsDataService, upchainService, downchainService, ratingService, corrListService);

		//Request Parameters
		requestParams = new TimeSeriesSummaryRequestParameters();
		requestParams.setStartDate(LocalDate.parse("2017-01-01"));
		requestParams.setEndDate(LocalDate.parse("2017-02-01"));
		requestParams.setPrimaryTimeseriesIdentifier(primaryDesc.getUniqueId());
		requestParams.setExcludedCorrections(Arrays.asList("corr1", "corr2"));

		//Metadata
		metadata = new TimeSeriesSummaryReportMetadata();
		metadata.setStartDate(requestParams.getStartInstant());
		metadata.setEndDate(requestParams.getEndInstant());
		metadata.setPrimaryParameter(primaryDesc.getParameter());
		metadata.setPrimaryTimeSeriesIdentifier(primaryDesc.getUniqueId());
		metadata.setReportType(TimeSeriesSummaryReportBuilderService.REPORT_TYPE);
		metadata.setRequestingUser(REQUESTING_USER);
		metadata.setRequestParameters(requestParams);
		metadata.setStationId(primaryDesc.getLocationIdentifier());
		metadata.setStationName(primaryLoc.getName());
		metadata.setTimezone(primaryDesc.getUtcOffset());
		metadata.setTitle(TimeSeriesSummaryReportBuilderService.REPORT_TITLE);

		//Description Map
		descriptionMap = new HashMap<>();
		descriptionMap.put("primary", Arrays.asList(primaryDesc));
		descriptionMap.put("upchain", upDescs);
		descriptionMap.put("downchain", downDescs);

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
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(upProcessors));
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(Arrays.asList(upProcessors.get(0).getOutputTimeSeriesUniqueId(), upProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(descService.getBatchTimeSeriesDescriptionLists(any(HashMap.class)))
			.willReturn(descriptionMap);
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(primaryData);
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant(), requestParams.getExcludedCorrections()))
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
		assertEquals(report.getReportMetadata().getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(report.getReportMetadata().getReportType(), metadata.getReportType());
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getRequestingUser(), metadata.getRequestingUser());
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
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getParameter());
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
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(new ArrayList<>()));
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(new ArrayList<>());
		given(descService.getBatchTimeSeriesDescriptionLists(any(HashMap.class)))
			.willReturn(descriptionMap);
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(primaryData);
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant(), requestParams.getExcludedCorrections()))
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
		assertEquals(report.getReportMetadata().getReportType(), metadata.getReportType());
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getRequestingUser(), metadata.getRequestingUser());
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
		assertEquals(report.getUpchainTs().size(), upDescs.size());
		assertEquals(report.getDownchainTs().size(), downDescs.size());
		assertEquals(report.getPrimaryTsMetadata(), primaryDesc);
		assertEquals(report.getReportMetadata().getStationId(), primaryDesc.getLocationIdentifier());
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getParameter());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
		assertEquals(report.getRatingCurves(), new ArrayList<>());
		assertEquals(report.getRatingShifts(), new ArrayList<>());
		assertEquals(report.getReportMetadata().getGradeMetadata(), new HashMap<>());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
	}

	@Test
	public void addBasicReportMetadataTest() {
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addBasicReportMetadata(report, requestParams, REQUESTING_USER);
		assertTrue(report.getReportMetadata() != null);
		assertEquals(report.getReportMetadata().getPrimaryTimeSeriesIdentifier(), metadata.getPrimaryTimeSeriesIdentifier());
		assertEquals(report.getReportMetadata().getReportType(), metadata.getReportType());
		assertEquals(report.getReportMetadata().getRequestParameters(), metadata.getRequestParameters());
		assertEquals(report.getReportMetadata().getRequestingUser(), metadata.getRequestingUser());
		assertEquals(report.getReportMetadata().getStartDate(), metadata.getStartDate());
		assertEquals(report.getReportMetadata().getEndDate(), metadata.getEndDate());		
	}

	@Test
	@SuppressWarnings("unchecked")
	public void addTimeSeriesDataTest() {
		given(downchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(downProcessors));
		given(downchainService.getOutputTimeSeriesUniqueIdList(downProcessors))
			.willReturn(Arrays.asList(downProcessors.get(0).getOutputTimeSeriesUniqueId(), downProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(upchainService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(new ProcessorListServiceResponse().setProcessors(upProcessors));
		given(upchainService.getInputTimeSeriesUniqueIdList(upProcessors))
			.willReturn(Arrays.asList(upProcessors.get(0).getOutputTimeSeriesUniqueId(), upProcessors.get(1).getOutputTimeSeriesUniqueId()));
		given(descService.getBatchTimeSeriesDescriptionLists(any(HashMap.class)))
			.willReturn(descriptionMap);
		given(tsDataService.getRawResponse(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant()))
			.willReturn(primaryData);
		
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addTimeSeriesData(report, requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant(), requestParams);
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
		assertEquals(report.getReportMetadata().getPrimaryParameter(), primaryDesc.getParameter());
		assertEquals(report.getReportMetadata().getTimezone(), metadata.getTimezone());
	}

	@Test
	public void addCorrectionDataTest() {
		given(corrListService.getExtendedCorrectionList(requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant(), requestParams.getExcludedCorrections()))
			.willReturn(extCorrs);

		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addCorrectionsData(report, requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant(), metadata.getStationId(), requestParams);
		assertEquals(report.getCorrections().getCorrUrl(), reportUrlBuilderService.buildAqcuReportUrl("correctionsataglance", metadata.getStationId(), requestParams, null));
		assertEquals(report.getCorrections().getPreProcessing().size(), 0);
		assertEquals(report.getCorrections().getNormal().size(), 1);
		assertThat(report.getCorrections().getNormal(), containsInAnyOrder(extCorrs.get(0)));
		assertEquals(report.getCorrections().getPostProcessing().size(), 1);
		assertThat(report.getCorrections().getPostProcessing(), containsInAnyOrder(extCorrs.get(1)));
	}

	@Test
	public void addLocationDataTest() {
		given(locService.getByLocationIdentifier(metadata.getStationId()))
			.willReturn(primaryLoc);

		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addLocationData(report, metadata.getStationId());
		assertEquals(report.getReportMetadata().getStationName(), metadata.getStationName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void addRatingInformationTest() {
		given(ratingService.getRawResponse(any(String.class), any(Double.class), any(Instant.class), any(Instant.class)))
			.willReturn(new RatingCurveListServiceResponse().setRatingCurves(ratingCurves));
		given(ratingService.getAqcuFilteredRatingCurves(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(ratingCurves);
		given(ratingService.getAqcuFilteredRatingShifts(any(ArrayList.class), any(Instant.class), any(Instant.class)))
			.willReturn(rawShifts);

		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addRatingInformation(report,  requestParams.getPrimaryTimeseriesIdentifier(), requestParams.getStartInstant(), requestParams.getEndInstant());
		assertEquals(report.getRatingCurves(), ratingCurves);
		assertEquals(report.getRatingShifts().size(), ratingShifts.size());
		assertEquals(gson.toJson(report.getRatingShifts()), gson.toJson(ratingShifts));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void addLookupDataTest() {
		given(gradeService.getByGradeList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		given(qualService.getByQualifierList(any(ArrayList.class)))
			.willReturn(new HashMap<>());
		
		TimeSeriesSummaryReport report = new TimeSeriesSummaryReport();
		report = service.addLookupData(report, new ArrayList<>(), new ArrayList<>());

		assertEquals(report.getReportMetadata().getGradeMetadata(), new HashMap<>());
		assertEquals(report.getReportMetadata().getQualifierMetadata(), new HashMap<>());
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
