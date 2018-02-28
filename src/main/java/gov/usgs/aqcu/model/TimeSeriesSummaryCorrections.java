package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;

public class TimeSeriesSummaryCorrections {	
	private List<Correction> preProcessing;
	private List<Correction> normal;
	private List<Correction> postProcessing;
	
	public TimeSeriesSummaryCorrections() {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing  = new ArrayList<>();
	}
	
	public List<Correction> getPreProcessing() {
		return preProcessing;
	}
	
	public List<Correction> getNormal() {
		return normal;
	}
	
	public List<Correction> getPostProcessing() {
		return postProcessing;
	}

	public void setPreProcessing(List<Correction> val) {
		preProcessing = val;
	}
	
	public void setNormal(List<Correction> val) {
		normal = val;
	}
	
	public void setPostProcessing(List<Correction> val) {
		postProcessing = val;
	}
}
