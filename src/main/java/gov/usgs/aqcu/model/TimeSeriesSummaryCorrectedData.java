package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;
import gov.usgs.aqcu.model.TimeSeriesSummaryDataGap;

public class TimeSeriesSummaryCorrectedData extends TimeSeriesDataServiceResponse {
	private List<TimeSeriesSummaryDataGap> gaps;
	private List<Processor> processors;
	
	public TimeSeriesSummaryCorrectedData() {
		gaps = new ArrayList<>();
		processors = new ArrayList<>();
	}
	
	public List<TimeSeriesSummaryDataGap> getGaps() {
		return gaps;
	}
	
	public void setGaps(List<TimeSeriesSummaryDataGap> val) {
		gaps = val;
	}
	
	public List<Processor> getProcessors() {
		return processors;
	}
	
	public void setProcessors(List<Processor> val) {
		processors = val;
	}
}
