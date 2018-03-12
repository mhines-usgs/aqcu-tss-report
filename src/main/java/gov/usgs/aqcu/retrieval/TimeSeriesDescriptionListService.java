package gov.usgs.aqcu.retrieval;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse;
import gov.usgs.aqcu.exception.AquariusException;
import gov.usgs.aqcu.exception.AquariusProcessingException;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;


@Component
public class TimeSeriesDescriptionListService extends AquariusRetrievalService {
	private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesDescriptionListService.class);

	public TimeSeriesDescriptionListByUniqueIdServiceResponse getRawResponse(List<String> timeSeriesUniqueIds) throws AquariusException {
		TimeSeriesDescriptionListByUniqueIdServiceRequest request = new TimeSeriesDescriptionListByUniqueIdServiceRequest()
				.setTimeSeriesUniqueIds(new ArrayList<>(timeSeriesUniqueIds));
		TimeSeriesDescriptionListByUniqueIdServiceResponse tssDesc = executePublishApiRequest(request);
		return tssDesc;
	}

	public List<TimeSeriesDescription> getTimeSeriesDescriptionList(Set<String> timeSeriesUniqueIds) throws AquariusException {
		List<TimeSeriesDescription> descList = getRawResponse(new ArrayList<String>(timeSeriesUniqueIds)).getTimeSeriesDescriptions();

		if(descList.size() != timeSeriesUniqueIds.size()) {
			String errorString = "Failed to fetch descriptions for all requested Time Series Identifiers: \nRequested: " + timeSeriesUniqueIds.size() + 
				"\nReceived: "  + descList.size();
			LOG.error(errorString);
			throw new AquariusProcessingException(errorString);
		}
		return descList;
	}

	public Map<String,List<TimeSeriesDescription>> getBatchTimeSeriesDescriptionLists(Map<String,List<String>> timeSeriesUniqueIdMap) throws AquariusException {
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
				"\nReceived: "  +outputMap.get(entry.getKey()).size();
			LOG.error(errorString);
			throw new AquariusProcessingException(errorString);
			}
		}

		return outputMap;
	}
}
