package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;

import net.servicestack.client.IReturn;
import net.servicestack.client.WebServiceException;

@Component
public class TimeSeriesDescriptionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDescriptionListService.class);

	public TimeSeriesDescriptionListByUniqueIdServiceResponse getRawResponse(List<String> timeSeriesUniqueIds) throws Exception {
		TimeSeriesDescriptionListByUniqueIdServiceRequest request = new TimeSeriesDescriptionListByUniqueIdServiceRequest()
				.setTimeSeriesUniqueIds(new ArrayList<>(timeSeriesUniqueIds));
		TimeSeriesDescriptionListByUniqueIdServiceResponse tssDesc = executePublishApiRequest(request);
		return tssDesc;
	}
	
	public List<TimeSeriesDescription> getTimeSeriesDescriptionList(Set<String> timeSeriesUniqueIds) throws Exception {
		List<TimeSeriesDescription> descList = getRawResponse(new ArrayList<String>(timeSeriesUniqueIds)).getTimeSeriesDescriptions();
		
		if(descList.size() != timeSeriesUniqueIds.size()) {
			String errorString = "Failed to fetch descriptions for all requested Time Series Identifiers: \nRequested: " + timeSeriesUniqueIds.size() + 
				"\nRecieved: "  + descList.size();
			LOG.error(errorString);
			//TODO: Change to more specific exception
			throw new Exception(errorString);
		}
		return descList;
	}
	
	public Map<String,List<TimeSeriesDescription>> getBatchTimeSeriesDescriptionLists(Map<String,List<String>> timeSeriesUniqueIdMap) throws Exception {
		Map<String,List<TimeSeriesDescription>> outputMap = new HashMap<>();
		Set<String> timeSeriesUniqueIds = new HashSet<>();
		
		//Collect all TS Unique IDs
		for(Map.Entry<String,List<String>> entry : timeSeriesUniqueIdMap.entrySet()) {
			timeSeriesUniqueIds.addAll(entry.getValue());
		}
		
		//Collect All TS Descriptions
		List<TimeSeriesDescription> fullList = getTimeSeriesDescriptionList(timeSeriesUniqueIds);
		
		//Assign Descriptions back to proper input key
		for(Map.Entry<String,List<String>> entry : timeSeriesUniqueIdMap.entrySet()) {
			List<TimeSeriesDescription> descList = new ArrayList<>();
			for(String uniqueId : entry.getValue()) {
				for(TimeSeriesDescription desc : fullList) {
					if(desc.getUniqueId().equals(uniqueId)) {
						descList.add(desc);
						break;
					}
				}
			}
			outputMap.put(entry.getKey(), descList);
		}
		
		//Validate Number of Retrieved Descriptions
		for(Map.Entry<String,List<String>> entry : timeSeriesUniqueIdMap.entrySet()) {
			if(outputMap.get(entry.getKey()).size() != entry.getValue().size()) {
				String errorString = "Failed to match returned descriptions to requested time series groups in batch request. Group: " + entry.getKey() + "\nRequested: " +  entry.getValue().size() + 
				"\nRecieved: "  +outputMap.get(entry.getKey()).size();
			LOG.error(errorString);
			//TODO: Change to more specific exception
			throw new Exception(errorString);
			}
		}
		
		return outputMap;
	}
}
