package com.shape.converter.kmltosdo.kml.service.model;

public class SplitKmlIntoJTSServiceResult {

	private String kmlFileName;
	private Boolean hasError;
	private String errorMessage;
	private Exception exception;

	public SplitKmlIntoJTSServiceResult() {
		this.hasError = false;
		this.errorMessage = "";
	}

	public String getKmlFileName() {
		return kmlFileName;
	}

	public void setKmlFileName(String kmlFileName) {
		this.kmlFileName = kmlFileName;
	}

	public Boolean getHasError() {
		return hasError;
	}

	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
