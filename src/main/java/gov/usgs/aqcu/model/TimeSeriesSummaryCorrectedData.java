package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Processor;

public class TimeSeriesSummaryCorrectedData extends TimeSeriesDataServiceResponse {
	private List<DataGap> gaps;
	private List<Processor> processors;

	public TimeSeriesSummaryCorrectedData(TimeSeriesDataServiceResponse response, List<Processor> upchainProcessorList, List<DataGap> gapList) {
		setApprovals(response.getApprovals());
		setQualifiers(response.getQualifiers());
		setNotes(response.getNotes());
		setMethods(response.getMethods());
		setGapTolerances(response.getGapTolerances());
		setInterpolationTypes(response.getInterpolationTypes());
		setGrades(response.getGrades());
		setProcessors(upchainProcessorList);
		setGaps(gapList);
	}
	
	public TimeSeriesSummaryCorrectedData() {
		gaps = new ArrayList<>();
		processors = new ArrayList<>();
	}
	
	public List<DataGap> getGaps() {
		return gaps;
	}
	
	public void setGaps(List<DataGap> val) {
		gaps = val;
	}
	
	public List<Processor> getProcessors() {
		return processors;
	}
	
	public void setProcessors(List<Processor> val) {
		processors = val;
	}
}
