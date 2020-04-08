package com.drupal.poc.dto;

public class BasicDTO {
	private ValueDTO[] title;
	private ValueDTO[] path;
	private ValueDTO[] body;

	public ValueDTO[] getTitle() {
		return title;
	}

	public void setTitle(ValueDTO[] title) {
		this.title = title;
	}

	public ValueDTO[] getPath() {
		return path;
	}

	public void setPath(ValueDTO[] path) {
		this.path = path;
	}

	public ValueDTO[] getBody() {
		return body;
	}

	public void setBody(ValueDTO[] body) {
		this.body = body;
	}

}
