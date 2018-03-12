package gov.usgs.aqcu.model;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.Correction;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.CorrectionType;

public class AqcuExtendedCorrection extends Correction {	
	private AqcuExtendedCorrectionType aqcuExtendedCorrectionType;
	private String dominantType;
	
	public AqcuExtendedCorrection(Correction source) {
		//Construct Base Correction
		super.setType(source.getType());
		super.setStartTime(source.getStartTime());
		super.setEndTime(source.getEndTime());
		super.setAppliedTimeUtc(source.getAppliedTimeUtc());
		super.setComment(source.getComment());
		super.setUser(source.getUser());
		super.setParameters(source.getParameters());
		super.setProcessingOrder(source.getProcessingOrder());
		
		//Construct Extended Correction
		setAqcuExtendedCorrectionType(AqcuExtendedCorrectionType.fromCorrection(source));
	}

	public AqcuExtendedCorrectionType getAqcuExtendedCorrectionType() {
		return aqcuExtendedCorrectionType;
	}

	public String getDominantType() {
		return dominantType;
	}

	@Override
	public Correction setType(CorrectionType val) {
		Correction toReturn = super.setType(val);
		setDominantType();
		return toReturn;
	}

	public void setAqcuExtendedCorrectionType(AqcuExtendedCorrectionType val) {
		aqcuExtendedCorrectionType = val;
		setDominantType();
	}

	protected void setDominantType() {
		if(aqcuExtendedCorrectionType != null) {
			dominantType = aqcuExtendedCorrectionType.toString();
		} else if(getType() != null) {
			dominantType = getType().toString();
		} else {
			dominantType = null;
		}
	}
}
