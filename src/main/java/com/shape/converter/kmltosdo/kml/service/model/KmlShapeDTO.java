package com.shape.converter.kmltosdo.kml.service.model;

import org.locationtech.jts.geom.Geometry;

public class KmlShapeDTO {

	private String geometryType;
	private Geometry geometry;
	private String fileName;

	public String getGeometryType() {
		return geometryType;
	}

	public void setGeometryType(String geometryType) {
		this.geometryType = geometryType;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
