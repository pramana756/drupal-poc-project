package com.drupal.poc.dto;

public class ArticlesDTO {
	private ValueDTO[] title;
	private ValueDTO[] path;
	private ValueDTO[] body;
	private ValueDTO[] field_media_image;
	private ValueDTO[] field_tags;

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

	public ValueDTO[] getImage() {
		return field_media_image;
	}

	public void setImage(ValueDTO[] image) {
		this.field_media_image = image;
	}

	public ValueDTO[] getTags() {
		return field_tags;
	}

	public void setTags(ValueDTO[] tags) {
		this.field_tags = tags;
	}
}
