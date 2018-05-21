package gov.usgs.aqcu.parameter;

import java.util.ArrayList;
import java.util.List;

public class TimeSeriesSummaryRequestParameters extends ReportRequestParameters {

	private List<String> excludedCorrections;

	public TimeSeriesSummaryRequestParameters() {
		excludedCorrections = new ArrayList<>();
	}

	public List<String> getExcludedCorrections() {		
		return excludedCorrections;
	}

	public void setExcludedCorrections(List<String> val) {
		this.excludedCorrections = val != null ? val : new ArrayList<>();
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
