package com.shape.converter.kmltosdo.kml.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shape.converter.kmltosdo.kml.service.dao.KmlShapeDao;

@Service
public class KmlReaderService {

	private static final Logger log = LoggerFactory.getLogger(KmlReaderService.class);

	@Value ("${kml.target.path}")
	private String kmlPath;

	@Autowired
	private KmlShapeDao kmlShapeDao;

	public List<Path> getUnprocessedFiles() throws IOException {
		List<String> processedFileNames = kmlShapeDao.findAllKmlShapeFileNames();
		List<String> allFailedKmlShapeFileNames = kmlShapeDao.findAllFailedKmlShapeFileNames();
		if(CollectionUtils.isNotEmpty(processedFileNames)){
			if(CollectionUtils.isNotEmpty(allFailedKmlShapeFileNames)){
				processedFileNames.addAll(allFailedKmlShapeFileNames);
			}
		}
		return Files.walk(Paths.get(kmlPath))
					.filter(path -> !processedFileNames.contains(path.getFileName()
																	 .toString()) && path.getFileName()
																						 .toString()
																						 .contains("proje_kml_dosya"))
					.filter(Files::isRegularFile)
					.collect(Collectors.toList());
	}

}
