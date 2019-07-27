package com.shape.converter.kmltosdo.kml.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shape.converter.kmltosdo.kml.service.entitiy.KmlShapeFailLogEntity;
import com.shape.converter.kmltosdo.kml.service.model.SplitKmlIntoJTSServiceResult;
import com.shape.converter.kmltosdo.kml.service.repo.KmlShapeFailLogRepo;

@Service
public class KmlShapeFailLogService {

	@Autowired
	private KmlShapeFailLogRepo kmlShapeFailLogRepo;

	@Transactional
	public void insertKmlShapeFailLogUsingJtsResult(SplitKmlIntoJTSServiceResult splitKmlIntoJTSServiceResult) {
		KmlShapeFailLogEntity newKmlShapeFailLogEntity = new KmlShapeFailLogEntity();
		newKmlShapeFailLogEntity.setFileName(splitKmlIntoJTSServiceResult.getKmlFileName());
		newKmlShapeFailLogEntity.setFailReason(splitKmlIntoJTSServiceResult.getErrorMessage());
		if(splitKmlIntoJTSServiceResult.getException() != null){
			newKmlShapeFailLogEntity.setFailDetail(ExceptionUtils.getStackTrace(splitKmlIntoJTSServiceResult.getException()));
		}
		kmlShapeFailLogRepo.save(newKmlShapeFailLogEntity);
	}

}
