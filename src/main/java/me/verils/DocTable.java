package me.verils;

import java.util.Arrays;

public class DocTable {

	private int rownum;
	private int colnum;
	private boolean block;

	private String[][] data;

	public DocTable(int rownum, int colnum) {
		this.rownum = rownum;
		this.colnum = colnum;
		this.block = rownum == 1 && colnum == 1;
		this.data = new String[rownum][colnum];
	}

	public String[] getRow(int rownum) {
		return data[rownum];
	}

	public void setRow(int rownum, String[] row) {
		if (rownum >= 0 && rownum < this.rownum) {
			data[rownum] = row;
		}
	}

	public String getCell(int rownum, int colnum) {
		return data[rownum][colnum];
	}

	public void setCell(int rownum, int colnum, String text) {
		if (rownum >= 0 && rownum < this.rownum && colnum >= 0 && colnum < this.colnum) {
			data[rownum][colnum] = text;
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
