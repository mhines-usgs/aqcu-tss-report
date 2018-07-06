package gov.usgs.aqcu.builder;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import gov.usgs.aqcu.parameter.ReportRequestParameters;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ReportUrlBuilderService {
	private static final Logger LOG = LoggerFactory.getLogger(ReportUrlBuilderService.class);

	@Value("${aqcu.reports.webservice}")
	private String aqcuWebserviceUrl;
	private final String SERVICE_ENDPOINT = "/service";
	private final String REPORTS_ENDPOINT = "/reports";

	public String buildAqcuReportUrl(String reportType, String stationId, ReportRequestParameters requestParams, String overrideIdentifier) {
		String reportUrl = aqcuWebserviceUrl;
		//Remove possible trailing slash from the base URL
		if(reportUrl.endsWith("/")){
			reportUrl = reportUrl.substring(0, reportUrl.length()-1);
		}

		reportUrl += SERVICE_ENDPOINT + REPORTS_ENDPOINT + "/" + reportType + "?" +
			requestParams.getAsQueryString(overrideIdentifier, true) +
			"&station=" + stationId;

		return reportUrl;
	}

	public Map<String,String> buildAqcuReportUrlMapByUnqiueIdList(String reportType, String stationId, ReportRequestParameters requestParams, List<String> timeseriesIdentifiers) {
		Map<String,String> urlMap = new HashMap<>();

		for(String identifier : timeseriesIdentifiers) {
			urlMap.put(identifier, buildAqcuReportUrl(reportType, stationId, requestParams, identifier));
		}

		return urlMap;
	}
}