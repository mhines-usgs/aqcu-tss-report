package gov.usgs.aqcu.builder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import gov.usgs.aqcu.parameter.RequestParameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ReportUrlBuilderTest {
	@Value("${aqcu.reports.webservice}")
	String aqcuWebserviceUrl;

	@Autowired
	ReportUrlBuilderService reportUrlBuilderService;

	@Before
	public void setup() {
		//Remove possible trailing slash from base URL for comparisons
		if(aqcuWebserviceUrl.endsWith("/")){
			aqcuWebserviceUrl = aqcuWebserviceUrl.substring(0, aqcuWebserviceUrl.length()-1);
		}
	}

	@Test
	public void nullParamsTest() {
		try {
			reportUrlBuilderService.buildAqcuReportUrl(null, null, null, null);
		} catch(NullPointerException e) {
			return;
		} catch(Exception e) {
			fail("Expected NullPointerException but got: " + e.toString());
		}

		fail("Expected NullPointerException but got none.");
	}

	@Test
	public void emptyParamsTest() {
		String expectedUrl = aqcuWebserviceUrl + "/service/reports/?&primaryTimeseriesIdentifier=&stationId=";
		String url = reportUrlBuilderService.buildAqcuReportUrl("", "", new RequestParameters(), "");
		assertEquals(0, expectedUrl.compareTo(url));
	}

	@Test
	public void noOverrideTest() {
		RequestParameters params = new RequestParameters();
		params.setPrimaryTimeseriesIdentifier("test-id");
		params.setStartDate(LocalDate.parse("2017-01-01"));
		params.setEndDate(LocalDate.parse("2017-02-01"));
		String expectedUrl = aqcuWebserviceUrl + "/service/reports/test-type?" + params.getAsQueryString(null, true) + "&stationId=test-station";
		String url = reportUrlBuilderService.buildAqcuReportUrl("test-type", "test-station", params, null);
		assertEquals(0, expectedUrl.compareTo(url));
	}

	@Test
	public void overrideTest() {
		RequestParameters params = new RequestParameters();
		params.setPrimaryTimeseriesIdentifier("test-id");
		params.setStartDate(LocalDate.parse("2017-01-01"));
		params.setEndDate(LocalDate.parse("2017-02-01"));
		String expectedUrl = aqcuWebserviceUrl + "/service/reports/test-type?" + params.getAsQueryString("test-override", true) + "&stationId=test-station";
		String url = reportUrlBuilderService.buildAqcuReportUrl("test-type", "test-station", params, "test-override");
		assertEquals(0, expectedUrl.compareTo(url));
	}

	@Test
	public void mapByUniqueIdListTest() {
		RequestParameters params = new RequestParameters();
		params.setPrimaryTimeseriesIdentifier("test-id");
		params.setStartDate(LocalDate.parse("2017-01-01"));
		params.setEndDate(LocalDate.parse("2017-02-01"));
		String expectedUrl1 = aqcuWebserviceUrl + "/service/reports/test-type?" + params.getAsQueryString("id1", true) + "&stationId=test-station";
		String expectedUrl2 = aqcuWebserviceUrl + "/service/reports/test-type?" + params.getAsQueryString("id2", true) + "&stationId=test-station";
		String expectedUrl3 = aqcuWebserviceUrl + "/service/reports/test-type?" + params.getAsQueryString("id3", true) + "&stationId=test-station";
		Map<String,String> urlMap = reportUrlBuilderService.buildAqcuReportUrlMapByUnqiueIdList("test-type", "test-station", params, Arrays.asList("id1", "id2", "id3"));

		assertEquals(urlMap.size(), 3);
		assertEquals(urlMap.get("id1").compareTo(expectedUrl1), 0);
		assertEquals(urlMap.get("id2").compareTo(expectedUrl2), 0);
		assertEquals(urlMap.get("id3").compareTo(expectedUrl3), 0);
	}
}
