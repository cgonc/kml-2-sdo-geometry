package com.shape.converter.kmltosdo.kml.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shape.converter.kmltosdo.kml.service.dao.KmlShapeDao;
import com.shape.converter.kmltosdo.kml.service.model.KmlShapeDTO;
import com.shape.converter.kmltosdo.kml.service.model.KmlValidationResult;

@Service
public class KmlShapeService {

	private static final Logger log = LoggerFactory.getLogger(KmlShapeService.class);

	@Autowired
	private KmlShapeDao kmlShapeDao;

	@Transactional
	public int insertKmlGeometries(List<KmlShapeDTO> kmlShapeDTOList) {
		AtomicInteger rowsInserts = new AtomicInteger();
		kmlShapeDTOList.forEach(kmlShapeDTO -> {
			int insert = kmlShapeDao.insert(kmlShapeDTO);
			rowsInserts.addAndGet(insert);
			log.info("One transaction for {} , {} , {}", kmlShapeDTO.getFileName(), rowsInserts.get(), kmlShapeDTOList.size());
		});
		return rowsInserts.get();
	}

	public int tryToValidateGeometries(List<KmlValidationResult> kmlValidationResults) {
		AtomicInteger rowUpdates = new AtomicInteger();
		kmlValidationResults.forEach(kmlValidationResult -> {
			int updateRowCount = kmlShapeDao.tryToValidateGeometryById(kmlValidationResult);
			if(updateRowCount == 0){
				int deletedRowCount = kmlShapeDao.deleteNotValidatableGeometryById(kmlValidationResult);
				if(deletedRowCount == 0){
					log.error("SEVERE ERROR , IN-VALID GEOMETRY MAY EXISTS IN CURRENT DATABASE ID : {}", kmlValidationResult.getId());
				}
			} else {
				rowUpdates.addAndGet(updateRowCount);
			}
		});
		return rowUpdates.get();
	}

	public List<KmlValidationResult> findAllInvalidGeometriesIfAny(String filename) {
		return kmlShapeDao.findAllInvalidGeometriesIfAny(filename);
	}

}
