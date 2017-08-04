package me.verils.transdoc.model;

import java.util.Arrays;

public class DocTable {

	private int rownum;
	private int colnum;
	private boolean block;

	private DocContent[][] data;

	public DocTable(int rownum, int colnum) {
		this.rownum = rownum;
		this.colnum = colnum;
		this.block = rownum == 1 && colnum == 1;
		this.data = new DocContent[rownum][colnum];
	}

	public DocContent[] getRow(int rownum) {
		return data[rownum];
	}

	public void setRow(int rownum, DocContent[] row) {
		if (rownum >= 0 && rownum < this.rownum) {
			data[rownum] = row;
		}
	}

	public DocContent getCell(int rownum, int colnum) {
		return data[rownum][colnum];
	}

	public void setCell(int rownum, int colnum, DocContent content) {
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
