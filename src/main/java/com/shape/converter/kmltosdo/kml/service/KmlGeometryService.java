package com.shape.converter.kmltosdo.kml.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.collections.CollectionUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.shape.converter.kmltosdo.kml.service.model.KmlShapeDTO;
import com.shape.converter.kmltosdo.kml.service.model.SplitKmlIntoJTSServiceResult;

@Service
public class KmlGeometryService {

	private static final Logger log = LoggerFactory.getLogger(KmlGeometryService.class);

	public Set<Geometry> splitKmlFileIntoJTSGeometries(Path path, SplitKmlIntoJTSServiceResult splitKmlIntoJTSServiceResult) {
		Set<Geometry> geometries = new HashSet<>();
		splitKmlIntoJTSServiceResult.setKmlFileName(path.getFileName()
														.toString());
		try(InputStream is = new FileInputStream(path.toString())){
			PullParser parser = new PullParser(new KMLConfiguration(), is, SimpleFeature.class);

			List<SimpleFeature> features = new ArrayList<>();
			SimpleFeature simpleFeature = (SimpleFeature) parser.parse();
			while(simpleFeature != null){
				features.add(simpleFeature);
				simpleFeature = (SimpleFeature) parser.parse();
			}
			SimpleFeatureCollection fc = DataUtilities.collection(features);
			try(SimpleFeatureIterator iterator = fc.features()){
				while(iterator.hasNext()){
					SimpleFeature feature = iterator.next();
					Geometry defaultGeometry = (Geometry) feature.getDefaultGeometry();
					if(defaultGeometry != null){
						defaultGeometry.setSRID(8327);
						geometries.add(defaultGeometry);
					}
				}
			}

		} catch (FileNotFoundException e){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage(e.getMessage());
			splitKmlIntoJTSServiceResult.setException(e);
			log.error("FileNotFoundException occured : ", e);
		} catch (IOException e){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage(e.getMessage());
			splitKmlIntoJTSServiceResult.setException(e);
			log.error("IOException occured : ", e);
		} catch (SAXException e){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage(e.getMessage());
			splitKmlIntoJTSServiceResult.setException(e);
			log.error("SAXException occured : ", e);
		} catch (XMLStreamException e){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage(e.getMessage());
			splitKmlIntoJTSServiceResult.setException(e);
			log.error("XMLStreamException occured : ", e);
		} catch (Exception e){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage(e.getMessage());
			splitKmlIntoJTSServiceResult.setException(e);
			log.error("Exception occured : ", e);
		}
		if(CollectionUtils.isEmpty(geometries) && !splitKmlIntoJTSServiceResult.getHasError()){
			splitKmlIntoJTSServiceResult.setHasError(true);
			splitKmlIntoJTSServiceResult.setErrorMessage("Empty geometry has found in kml file.");
		}
		return geometries;
	}

	public List<KmlShapeDTO> convertGeometriesIntoKmlShapeDTO(Set<Geometry> geometries, Path path) {
		List<KmlShapeDTO> kmlShapeDTOSToBeInserted = new ArrayList<>();
		geometries.forEach(geometry -> {
			KmlShapeDTO kmlShapeDTO = new KmlShapeDTO();
			kmlShapeDTO.setFileName(path.getFileName()
										.toString());
			kmlShapeDTO.setGeometry(geometry);
			kmlShapeDTO.setGeometryType(geometry.getGeometryType());
			kmlShapeDTOSToBeInserted.add(kmlShapeDTO);
		});
		return kmlShapeDTOSToBeInserted;
	}
}
