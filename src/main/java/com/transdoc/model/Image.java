package com.transdoc.model;

/**
 * DocPictureData
 *
 * @author Verils
 * @date 2017-10-20
 */
public class Image {

	private String name;
	private String contentType;
	private String extension;
	private byte[] content;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
