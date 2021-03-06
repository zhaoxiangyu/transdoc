package com.transdoc.model;

/**
 * DocParagraph
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocParagraph {

	private boolean inTitle;
	private int titleLvl;
	private boolean inList;
	private int listLvl;

	private String content;

	public DocParagraph(String content) {
		super();
		this.content = content;
	}

	public DocParagraph(String content, boolean inList) {
		super();
		this.content = content;
		this.inList = inList;
	}

	public boolean isInTitle() {
		return inTitle;
	}

	public void setInTitle(boolean inTitle) {
		this.inTitle = inTitle;
	}

	public int getTitleLvl() {
		return titleLvl;
	}

	public void setTitleLvl(int titleLvl) {
		this.titleLvl = titleLvl;
	}

	public boolean isInList() {
		return inList;
	}

	public void setInList(boolean inList) {
		this.inList = inList;
	}

	public int getListLvl() {
		return listLvl;
	}

	public void setListLvl(int listLvl) {
		this.listLvl = listLvl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
