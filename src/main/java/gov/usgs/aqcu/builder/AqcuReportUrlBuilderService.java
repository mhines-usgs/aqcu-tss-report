package gov.usgs.aqcu.builder;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class AqcuReportUrlBuilderService {
	private static final Logger LOG = LoggerFactory.getLogger(AqcuReportUrlBuilderService.class);
	
	@Autowired
	private String aqcuWebserviceUrl;
	private final String SERVICE_ENDPOINT = "/service";
    private final String REPORTS_ENDPOINT = "/reports";
	private Gson gson;

	@Autowired
	public AqcuReportUrlBuilderService(Gson gson) {
		this.gson = gson;
	}

	public String buildAqcuReportUrl(String reportType, Instant startDate, Instant endDate, String primaryTimeseriesIdentifier, String stationId, Map<String, String> additionalParameters) {
		String reportUrl = aqcuWebserviceUrl;
		//Remove possible trailing slash from the base URL
		if(reportUrl.endsWith("/")){
			reportUrl = reportUrl.substring(0, reportUrl.length()-1);
		}

		reportUrl += SERVICE_ENDPOINT + REPORTS_ENDPOINT + "/" + reportType + "?" +
			"startDate=" + gson.toJson(startDate).substring(1,10) + "&" +
			"endDate=" + gson.toJson(endDate).substring(1,10) + "&" + 
			"primaryTimeseriesIdentifier=" + primaryTimeseriesIdentifier + "&" +
			"station=" + stationId;
			
		for(Map.Entry<String,String> param : additionalParameters.entrySet()) {
			if(param.getKey() != null && param.getKey().length() > 0 && param.getValue() != null & param.getValue().length() > 0) {
				reportUrl += "&" + param.getKey() + "=" + param.getValue();
			}
		}

		return reportUrl;
	}
	
	public Map<String,String> buildAqcuReportUrlMapByUnqiueIdList(String reportType, Instant startDate, Instant endDate, List<String> timeseriesIdentifiers, String stationId,  Map<String,String> params) {
		Map<String,String> urlMap = new HashMap<>();
		
		for(String identifier : timeseriesIdentifiers) {
			urlMap.put(identifier, buildAqcuReportUrl(reportType, startDate, endDate, identifier, stationId, params));
		}
		
		return urlMap;
	}
}
	
