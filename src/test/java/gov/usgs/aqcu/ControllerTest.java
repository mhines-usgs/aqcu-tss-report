package gov.usgs.aqcu;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.*;

import gov.usgs.aqcu.retrieval.TimeSeriesMetadataService;
import gov.usgs.aqcu.retrieval.UpchainProcessorListService;
import gov.usgs.aqcu.retrieval.RatingCurveListService;
import gov.usgs.aqcu.retrieval.LocationDescriptionListService;

import gov.usgs.aqcu.model.TimeSeriesSummaryReport;
import gov.usgs.aqcu.builder.TimeSeriesSummaryReportBuilderService;
import gov.usgs.aqcu.client.JavaToRClient;

public class ControllerTest {
	@Mock
	private TimeSeriesMetadataService timeSeriesMetadataService;
	@Mock
	private UpchainProcessorListService upchainProcessorListService;
	@Mock
	private RatingCurveListService ratingCurveListService;
	@Mock
	private LocationDescriptionListService locationDescriptionListService;
	@Mock
	private TimeSeriesSummaryReportBuilderService reportBuilderService;
	@Mock
	private JavaToRClient client;
	private Gson gson;
	private TheController controller;
	
}
