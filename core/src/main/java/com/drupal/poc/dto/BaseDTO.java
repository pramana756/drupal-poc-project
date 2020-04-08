package com.drupal.poc.dto;


public class BaseDTO<T> {
	Object dtoList[];

	public void setDtoList(Object[] dtoList) {
		this.dtoList = dtoList;
	}

	public Object[] getDtoList() {
		return dtoList;
	}

}
