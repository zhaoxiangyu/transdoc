package com.transdoc.model;

import java.util.Arrays;

/**
 * DocTable
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocTable {

	private int rownum;
	private int colnum;
	private boolean block;

	private Article[][] data;

	public DocTable(int rownum, int colnum) {
		this.rownum = rownum;
		this.colnum = colnum;
		this.block = rownum == 1 && colnum == 1;
		this.data = new Article[rownum][colnum];
	}

	public Article[] getRow(int rownum) {
		return data[rownum];
	}

	public void setRow(int rownum, Article[] row) {
		if (rownum >= 0 && rownum < this.rownum) {
			data[rownum] = row;
		}
	}

	public Article getCell(int rownum, int colnum) {
		return data[rownum][colnum];
	}

	public void setCell(int rownum, int colnum, Article content) {
		if (rownum >= 0 && rownum < this.rownum && colnum >= 0 && colnum < this.colnum) {
			data[rownum][colnum] = content;
		}
	}

	public int getRownum() {
		return rownum;
	}

	public int getColnum() {
		return colnum;
	}

	public boolean isBlock() {
		return block;
	}

	@Override
	public String toString() {
		return "DocTable [rownum=" + rownum + ", colnum=" + colnum + ", codeBlock=" + block + ", data="
				+ Arrays.toString(data) + "]";
	}

}
