package gov.usgs.aqcu.builder;

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

import gov.usgs.aqcu.retrieval.AquariusRetrievalService;
import net.servicestack.client.IReturn;

@RunWith(SpringRunner.class)
public class TimeSeriesSummaryReportBuilderTest {
    
	@MockBean
	private AquariusRetrievalService aquariusService;
	private TimeSeriesSummaryReportBuilderService service;
	
	@Test
	public void emptyParametersTest() {
		
	}
}