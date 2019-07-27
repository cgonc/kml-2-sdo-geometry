# kml-2-sdo-geometry
A spring boot project for persisting KML files into Oracle database in SDO_GEOMETRY format.

This project is a Spring Boot based project and can be used for converting KML files into Oracle's SDO_GEOMETRY format.

## Initial Setup
You need to create the following tables, spatial index and sequences in your Oracle database as follows:
 ````sql
CREATE TABLE KML_SHAPE
(
  ID             NUMBER NOT NULL,
  GEOMETRY_TYPE  VARCHAR2(255 BYTE),
  GEOMETRY       MDSYS.SDO_GEOMETRY,
  FILE_NAME      VARCHAR2(255 BYTE)
);

CREATE UNIQUE INDEX KML_SHAPE_PK ON KML_SHAPE (ID);

CREATE INDEX KML_SHAPE_SIDX ON KML_SHAPE (GEOMETRY)
INDEXTYPE IS MDSYS.SPATIAL_INDEX NOPARALLEL;

CREATE SEQUENCE KML_SHAPE_SEQ
  START WITH 100
  MAXVALUE 9999999999999999999999999999
  MINVALUE 100
  NOCYCLE
  CACHE 20
  ORDER;
  
CREATE TABLE KML_SHAPE_FAIL_LOG
(
ID             NUMBER NOT NULL,
FILE_NAME      VARCHAR2(255 BYTE),
GEOMETRY       MDSYS.SDO_GEOMETRY,
FILE_NAME      VARCHAR2(255 BYTE)
);

CREATE TABLE KML_SHAPE_FAIL_LOG
(
  ID           NUMBER NOT NULL,
  FILE_NAME    VARCHAR2(1000 BYTE),
  FAIL_REASON  VARCHAR2(1000 BYTE),
  FAIL_DETAIL  CLOB
);

CREATE SEQUENCE KML_SHAPE_FAIL_LOG_SEQ
  START WITH 100
  MAXVALUE 9999999999999999999999999999
  MINVALUE 100
  NOCYCLE
  CACHE 20
  ORDER;
````

Before creating a spatial index you need to add the meta information of your **KML_SHAPE** table to your **user_sdo_geom_metadata**.
For detailed instruction on adding a spatial index, you can check the official documents.
https://docs.oracle.com/database/121/SPATL/toc.htm

After the database setup, you also need to set up your application.properties file.
Put your connection properties and target KML folder information to the application.properties file.
````properties
spring.datasource.url=${ORACLE_CONNECTION_URL}
spring.datasource.username=${ORACLE_CONNECTION_USERNAME}
spring.datasource.password=${ORACLE_CONNECTION_PASSWORD}

kml.target.path=${KML_FOLDER}
````

After boot, a spring component function named KmlConverter.convertMissingKmlFiles will be scheduled to work. 
It will roughly do the following things :

    1. It calculates the unprocessed kml files by comparing the KML_SHAPE table and your kml.target.path
    2. For each unprocessed files :
        a. Read the content of the kml file.
        b. Convert the kml file into a collection of JTS geometries.
        c. Convert JTS geometries into STRUCT and insert them to the table in a single transaction.
        d. Validate inserted geometries and rectify them.
        e. Insert a fail log to your database if an error occurs during this transaction
        
