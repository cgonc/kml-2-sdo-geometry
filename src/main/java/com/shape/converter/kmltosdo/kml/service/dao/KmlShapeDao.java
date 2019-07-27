package com.shape.converter.kmltosdo.kml.service.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.shape.converter.kmltosdo.geometry.JGeometryUtils;
import com.shape.converter.kmltosdo.kml.service.model.KmlShapeDTO;
import com.shape.converter.kmltosdo.kml.service.model.KmlValidationResult;

@Repository
public class KmlShapeDao {

	private static final Logger log = LoggerFactory.getLogger(KmlShapeDao.class);

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private DataSource dataSource;

	@Autowired
	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	private Map<String, Object> getSqlParameterMap(KmlShapeDTO kmlShapeDTO) {
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("id", 1000L);
		parameterMap.put("geometry_type", kmlShapeDTO.getGeometryType());
		parameterMap.put("geometry", JGeometryUtils.getOracleStructFromGeometry(kmlShapeDTO.getGeometry(), dataSource));
		parameterMap.put("file_name", kmlShapeDTO.getFileName());
		return parameterMap;
	}

	private SqlParameterSource getSqlParameterSource(KmlShapeDTO kmlShapeDTO) {
		return new MapSqlParameterSource().addValues(getSqlParameterMap(kmlShapeDTO));
	}

	public List<String> findAllKmlShapeFileNames() {
		String sql = "select distinct file_name from kml_shape where file_name is not null";
		return namedParameterJdbcTemplate.query(sql, (resultSet, i) -> resultSet.getString("file_name"));
	}

	public List<String> findAllFailedKmlShapeFileNames() {
		String sql = "select distinct file_name from kml_shape_fail_log where file_name is not null";
		return namedParameterJdbcTemplate.query(sql, (resultSet, i) -> resultSet.getString("file_name"));
	}

	@Transactional (propagation = Propagation.MANDATORY)
	public int insert(KmlShapeDTO kmlShapeDTO) {
		String insertStatement = "insert into kml_shape(id, geometry_type, geometry, file_name) values (kml_shape_seq.nextval, :geometry_type, :geometry, :file_name)";
		return namedParameterJdbcTemplate.update(insertStatement, getSqlParameterSource(kmlShapeDTO));
	}

	public List<KmlValidationResult> findAllInvalidGeometriesIfAny(String filename) {
		String sql = new StringBuilder().append("SELECT ID, VALID ")
										.append(" FROM ( ")
										.append("         SELECT C.ID, ")
										.append("                SDO_GEOM.VALIDATE_GEOMETRY_WITH_CONTEXT(C.GEOMETRY, 0.005) VALID ")
										.append("         FROM KML_SHAPE C ")
										.append("        WHERE C.FILE_NAME = :filename ")
										.append("              )")
										.append("WHERE  VALID != 'TRUE' ")
										.toString();

		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("filename", filename);
		List<KmlValidationResult> kmlValidationResults = namedParameterJdbcTemplate.query(sql, parameterMap, (resultSet, i) -> {
			KmlValidationResult kmlValidationResult = new KmlValidationResult();
			kmlValidationResult.setId(resultSet.getLong("ID"));
			kmlValidationResult.setValidationResult(resultSet.getString("VALID"));
			return kmlValidationResult;
		});
		if(CollectionUtils.isNotEmpty(kmlValidationResults)){
			return kmlValidationResults;
		}
		return new ArrayList<>();
	}

	public int tryToValidateGeometryById(KmlValidationResult kmlValidationResult) {
		String updateValidateSql = "UPDATE KML_SHAPE C SET GEOMETRY = SDO_UTIL.RECTIFY_GEOMETRY(C.GEOMETRY, 0.005) WHERE C.ID = :id ";
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("id", kmlValidationResult.getId());
		try{
			return namedParameterJdbcTemplate.update(updateValidateSql, parameterMap);
		} catch (Exception ex){
			log.error("An error occured during validation of the geometry id : " + kmlValidationResult.getId() + ", Validation Result : " + kmlValidationResult.getValidationResult(), ex);
			return 0;
		}
	}

	public int deleteNotValidatableGeometryById(KmlValidationResult kmlValidationResult) {
		String deleteNotValidGeometry = " DELETE FROM KML_SHAPE WHERE ID =  :id ";
		Map<String, Object> parameterMap = new HashMap<>();
		parameterMap.put("id", kmlValidationResult.getId());
		return namedParameterJdbcTemplate.update(deleteNotValidGeometry, parameterMap);
	}

}
