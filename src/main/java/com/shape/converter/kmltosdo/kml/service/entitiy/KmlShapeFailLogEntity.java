package com.shape.converter.kmltosdo.kml.service.entitiy;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table (name = "KML_SHAPE_FAIL_LOG")
public class KmlShapeFailLogEntity {

	private Long id;
	private String fileName;
	private String failReason;
	private String failDetail;

	@Id
	@Column (name = "ID", nullable = false)
	@GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "KML_SHAPE_FAIL_LOG_SEQ")
	@SequenceGenerator (sequenceName = "kml_shape_fail_log_seq", allocationSize = 1, name = "KML_SHAPE_FAIL_LOG_SEQ")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Basic
	@Column (name = "FILE_NAME", length = 1000)
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Basic
	@Column (name = "FAIL_REASON", length = 1000)
	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	@Basic
	@Column (name = "FAIL_DETAIL")
	public String getFailDetail() {
		return failDetail;
	}

	public void setFailDetail(String failDetail) {
		this.failDetail = failDetail;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		KmlShapeFailLogEntity that = (KmlShapeFailLogEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(fileName, that.fileName) && Objects.equals(failReason, that.failReason) && Objects.equals(failDetail, that.failDetail);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fileName, failReason, failDetail);
	}
}
