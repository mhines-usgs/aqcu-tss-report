package gov.usgs.aqcu.parameter;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesSummaryRequestParameters extends RequestParameters {

	private List<String> excludedCorrections;

	public TimeSeriesSummaryRequestParameters() {}

	public TimeSeriesSummaryRequestParameters(TimeSeriesSummaryRequestParameters other) {
		super(other);
		setExcludedCorrections(other.getExcludedCorrections());
	}

	public List<String> getExcludedCorrections() {
		//Set default value if null
		if(excludedCorrections == null) {
			excludedCorrections = new ArrayList<>();
		}
		
		return excludedCorrections;
	}

	public void setExcludedCorrections(List<String> val) {
		this.excludedCorrections = val;
	}

	@Override 
	public String getAsQueryString(String overrideIdentifier, boolean absoluteTime) {
		String queryString = super.getAsQueryString(overrideIdentifier, absoluteTime);

		if(getExcludedCorrections().size() > 0) {
			queryString += "&excludedCorrections=" + String.join(",", excludedCorrections);
		}

		return queryString;
	}
}
