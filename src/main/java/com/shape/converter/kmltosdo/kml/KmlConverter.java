package com.shape.converter.kmltosdo.kml;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shape.converter.kmltosdo.kml.service.KmlGeometryService;
import com.shape.converter.kmltosdo.kml.service.KmlReaderService;
import com.shape.converter.kmltosdo.kml.service.KmlShapeFailLogService;
import com.shape.converter.kmltosdo.kml.service.KmlShapeService;
import com.shape.converter.kmltosdo.kml.service.KmlValidatorService;
import com.shape.converter.kmltosdo.kml.service.model.KmlShapeDTO;
import com.shape.converter.kmltosdo.kml.service.model.SplitKmlIntoJTSServiceResult;

@Component
public class KmlConverter {

	private static final Logger log = LoggerFactory.getLogger(KmlConverter.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Autowired
	private KmlReaderService kmlReaderService;

	@Autowired
	private KmlGeometryService kmlGeometryService;

	@Autowired
	private KmlShapeFailLogService kmlShapeFailLogService;

	@Autowired
	private KmlShapeService kmlShapeService;

	@Autowired
	private KmlValidatorService kmlValidatorService;

	@Scheduled (fixedDelay = 60 * 60 * 1000)
	public void convertMissingKmlFiles() throws IOException {
		log.info("The time is {}. Reading kml files starts....", dateFormat.format(new Date()));

		List<Path> unprocessedKmlFiles = kmlReaderService.getUnprocessedFiles();
		log.info("Total unprocessed file size {}", unprocessedKmlFiles.size());
		final AtomicInteger i = new AtomicInteger(0);
		unprocessedKmlFiles.forEach(unprocessedKmlFile -> {

			log.info("One kml reading starts : {} {} / {}", unprocessedKmlFile.toString(), i.incrementAndGet(), unprocessedKmlFiles.size());

			SplitKmlIntoJTSServiceResult splitKmlIntoJTSServiceResult = new SplitKmlIntoJTSServiceResult();
			Set<Geometry> geometries = kmlGeometryService.splitKmlFileIntoJTSGeometries(unprocessedKmlFile, splitKmlIntoJTSServiceResult);
			if(splitKmlIntoJTSServiceResult.getHasError()){
				log.info("{} kml file can not be parsed into JTS geometries {}", unprocessedKmlFile.getFileName()
																								   .toString(), splitKmlIntoJTSServiceResult.getErrorMessage());
				kmlShapeFailLogService.insertKmlShapeFailLogUsingJtsResult(splitKmlIntoJTSServiceResult);
			} else if(CollectionUtils.isNotEmpty(geometries)){
				List<KmlShapeDTO> kmlShapeDTOSToBeInserted = kmlGeometryService.convertGeometriesIntoKmlShapeDTO(geometries, unprocessedKmlFile);
				log.info("{} rows will be inserted", kmlShapeDTOSToBeInserted.size());
				int insertedRowCount = 0;
				try{
					insertedRowCount = kmlShapeService.insertKmlGeometries(kmlShapeDTOSToBeInserted);
					log.info("{} rows is inserted", insertedRowCount);
				} catch (Exception ex){
					log.error("An exception occured during transaction.", ex);
					splitKmlIntoJTSServiceResult.setHasError(true);
					splitKmlIntoJTSServiceResult.setKmlFileName(unprocessedKmlFile.getFileName()
																				  .toString());
					splitKmlIntoJTSServiceResult.setErrorMessage(ex.getMessage());
					splitKmlIntoJTSServiceResult.setException(ex);
					kmlShapeFailLogService.insertKmlShapeFailLogUsingJtsResult(splitKmlIntoJTSServiceResult);
				}

				if(insertedRowCount > 0){
					kmlValidatorService.validateAndRectifyGeometries(unprocessedKmlFile, 3);
				}
			}
			log.info("One kml reading ends : {}", unprocessedKmlFile.toString());
		});

		log.info("The time is {}. Reading kml files stops....", dateFormat.format(new Date()));

	}
}
