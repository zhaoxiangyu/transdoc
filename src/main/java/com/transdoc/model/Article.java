package com.transdoc.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * DocContent
 *
 * @author Verils
 * @date 2017-10-20
 */
public class Article {

	/** 文档的所有段落 */
	List<DocParagraph> paragraphs;
	/** 文档的所有表格和代码块 */
	List<DocTable> tables;
	/** 文档的所有图片导出后的本地相对路径 */
	List<String> picturePaths;

	public Article() {
		paragraphs = new ArrayList<DocParagraph>();
		picturePaths = new ArrayList<String>();
	}

	public Article(DocParagraph paragraph) {
		this();
		paragraphs.add(paragraph);
	}

	public Article(List<DocParagraph> paragraphs) {
		this.paragraphs = paragraphs;
	}

	public DocParagraph getFirstParagraph() {
		return paragraphs.get(0);
	}

	public void addParagraph(DocParagraph paragraph) {
		paragraphs.add(paragraph);
	}

	/**
	 * 获取文档的所有段落,包含标题,文本段落,以及表格和图片的占位符,占位符占一个独立的段落,每行视为一个段落
	 * 
	 * @return 文档解析后的所有段落
	 */
	public List<DocParagraph> listParagraphs() {
		List<DocParagraph> docParagraphs = new ArrayList<DocParagraph>();
		if (paragraphs != null) {
			docParagraphs.addAll(paragraphs);
		}
		return docParagraphs;
	}

	/**
	 * 获得文档中的所有列表对象,对该列表操作不会影响到原有数据
	 * 
	 * @return 所有列表对象
	 */
	public LinkedList<DocTable> listTables() {
		LinkedList<DocTable> docTables = new LinkedList<DocTable>();
		if (tables != null) {
			docTables.addAll(tables);
		}
		return docTables;
	}

	/**
	 * 获得文档中的所有图片对象,对该列表操作不会影响到原有数据
	 * 
	 * @return 所有图片对象
	 */
	public LinkedList<String> listPictures() {
		LinkedList<String> pictures = new LinkedList<String>();
		if (picturePaths != null) {
			pictures.addAll(picturePaths);
		}
		return pictures;
	}

	public List<DocParagraph> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<DocParagraph> paragraphs) {
		this.paragraphs = paragraphs;
	}

	public List<DocTable> getTables() {
		return tables;
	}

	public void setTables(List<DocTable> tables) {
		this.tables = tables;
	}

	public List<String> getPicturePaths() {
		return picturePaths;
	}

	public void setPicturePaths(List<String> picturePaths) {
		this.picturePaths = picturePaths;
	}

}
