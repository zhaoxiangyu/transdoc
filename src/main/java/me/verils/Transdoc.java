package me.verils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableIterator;

public class Transdoc {

	public static void main(String[] args) {
		File docsDir = new File("./docs");
		if (docsDir.isDirectory()) {
			File[] listFiles = docsDir.listFiles();
			for (File file : listFiles) {
				if (file.isFile() && file.getName().endsWith(".doc")) {
					handleDocFile(file);
				}
			}
		}
	}

	private static void handleDocFile(File file) {
		String filename = FilenameUtils.getBaseName(file.getName());
		System.out.println("==================== " + file.getName() + " ====================");

		File destDir = new File("docs", filename);
		destDir.mkdir();
		try (FileInputStream fis = new FileInputStream(file);
				HWPFDocument doc = new HWPFDocument(fis)) {
			DocParser docParser = DocParser.parseFromFile(file);
			docParser.getText();
			docParser.extractPicturesToFile("abc.%i");

			Range docRange = doc.getRange();

			// 处理表格
			StringBuilder tableContent = new StringBuilder();
			TableIterator tableIterator = new TableIterator(docRange);
			while (tableIterator.hasNext()) {
				Table table = tableIterator.next();
				if (table.numRows() == 1 && table.getRow(0).numCells() == 1) {
					// 处理代码块
					String cellText = table.getRow(0).getCell(0).text();
					tableContent.append("```\n").append(cellText).append("\n```\n");
				} else {

				}
			}

			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
