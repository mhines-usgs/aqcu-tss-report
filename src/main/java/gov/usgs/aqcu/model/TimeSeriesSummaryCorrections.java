package gov.usgs.aqcu.model;

import java.util.List;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionProcessingOrder;

import java.util.ArrayList;

public class TimeSeriesSummaryCorrections {	
	private List<AqcuExtendedCorrection> preProcessing;
	private List<AqcuExtendedCorrection> normal;
	private List<AqcuExtendedCorrection> postProcessing;
	String corrUrl;

	public TimeSeriesSummaryCorrections(List<AqcuExtendedCorrection> correctionList, String corrUrl) {
		preProcessing = new ArrayList<>();
		normal = new ArrayList<>();
		postProcessing = new ArrayList<>();

		if(correctionList != null && correctionList.size() > 0) {
			for(AqcuExtendedCorrection corr : correctionList) {
				if(corr.getProcessingOrder() == CorrectionProcessingOrder.PreProcessing) {
					preProcessing.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.Normal) {
					normal.add(corr);
				} else if(corr.getProcessingOrder() == CorrectionProcessingOrder.PostProcessing) {
					postProcessing.add(corr);
				}
			}
		}

		setCorrUrl(corrUrl);
	}
	
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
