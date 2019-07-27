package com.shape.converter.kmltosdo.kml.service;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shape.converter.kmltosdo.kml.service.model.KmlValidationResult;

@Service
public class KmlValidatorService {

	private static final Logger log = LoggerFactory.getLogger(KmlValidatorService.class);

	@Autowired
	private KmlReaderService kmlReaderService;

	@Autowired
	private KmlShapeService kmlShapeService;

	public void validateAndRectifyGeometries(Path fileName, int trialCount) {
		List<KmlValidationResult> allInvalidGeometriesIfAny = kmlReaderService.findAllInvalidGeometriesIfAny(fileName.getFileName()
																													 .toString());
		if(CollectionUtils.isNotEmpty(allInvalidGeometriesIfAny)){
			log.info("Invalid geometries has been found {}", allInvalidGeometriesIfAny.size());
			int validationCount = kmlShapeService.tryToValidateGeometries(allInvalidGeometriesIfAny);
			log.info("Geometries are validated : {}", validationCount);

			allInvalidGeometriesIfAny = kmlReaderService.findAllInvalidGeometriesIfAny(fileName.getFileName()
																							   .toString());
			if(CollectionUtils.isNotEmpty(allInvalidGeometriesIfAny) && trialCount < 3){
				int trialCountCallStack = trialCount + 1;
				log.error("SEVERE ERROR. INVALID GEOMETRY HAS FOUND {} TRY ONCE MORE {} / 3", fileName.getFileName(), trialCount);
				validateAndRectifyGeometries(fileName, trialCountCallStack);
			}
		}
	}
}
