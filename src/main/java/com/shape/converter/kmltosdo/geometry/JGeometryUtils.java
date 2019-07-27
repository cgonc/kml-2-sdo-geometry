package com.shape.converter.kmltosdo.geometry;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.locationtech.jts.geom.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shape.converter.kmltosdo.geometry.ora.OraWriter;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;

public class JGeometryUtils {

	private static final Logger log = LoggerFactory.getLogger(JGeometryUtils.class);

	public static STRUCT getOracleStructFromGeometry(Geometry geometry, DataSource dataSource) {
		Connection conn = null;
		OracleConnection orclConnection = null;
		try{
			conn = dataSource.getConnection();
			orclConnection = conn.unwrap(OracleConnection.class);

			OraWriter oraWriter = new OraWriter();
			oraWriter.setSRID(8307);
			oraWriter.setDimension(2);
			return oraWriter.write(geometry, orclConnection);

		} catch (SQLException e){
			e.printStackTrace();
		} finally{
			if(conn != null){
				((HikariDataSource) dataSource).evictConnection(conn);
				try{
					conn.close();
				} catch (SQLException e){
					log.error("couldnt close connection -> from connection", e);
				}
			}
			if(orclConnection != null){
				try{
					orclConnection.close();
				} catch (SQLException e){
					log.error("couldnt close connection -> from oracle", e);
				}
			}
		}

		return null;
	}

}
