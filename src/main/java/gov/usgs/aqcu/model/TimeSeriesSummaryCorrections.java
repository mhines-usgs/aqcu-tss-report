package gov.usgs.aqcu.model;

import java.util.List;
import java.util.ArrayList;

public class TimeSeriesSummaryCorrections {	
	private List<AqcuExtendedCorrection> preProcessing;
	private List<AqcuExtendedCorrection> normal;
	private List<AqcuExtendedCorrection> postProcessing;
	String corrUrl;
	
	public TimeSeriesSummaryCorrections() {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing  = new ArrayList<>();
	}
	
	public List<AqcuExtendedCorrection> getPreProcessing() {
		return preProcessing;
	}
	
	public List<AqcuExtendedCorrection> getNormal() {
		return normal;
	}
	
	public List<AqcuExtendedCorrection> getPostProcessing() {
		return postProcessing;
	}
	
	public String getCorrUrl() {
		return corrUrl;
	}

	public void setPreProcessing(List<AqcuExtendedCorrection> val) {
		preProcessing = val;
	}
	
	public void setNormal(List<AqcuExtendedCorrection> val) {
		normal = val;
	}
	
	public void setPostProcessing(List<AqcuExtendedCorrection> val) {
		postProcessing = val;
	}
	
	public void setCorrUrl(String val) {
		corrUrl = val;
	}
}
