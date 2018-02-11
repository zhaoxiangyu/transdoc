package com.transdoc.model;

import java.io.IOException;
import java.io.OutputStream;

/**
 * DocPictureData
 *
 * @author Verils
 * @date 2017-10-20
 */
public abstract class Image {

	private String pictureName;
	private String extension;
	private String contentType;
	private String path;

	public String getPictureName() {
		return pictureName;
	}

	public void setPictureName(String pictureName) {
		this.pictureName = pictureName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public abstract byte[] getData();

	public abstract void writeTo(OutputStream out) throws IOException;

}