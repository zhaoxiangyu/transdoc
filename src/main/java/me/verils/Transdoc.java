package me.verils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hwpf.HWPFDocument;

public class Transdoc {

	private static final String UTF_8 = "UTF-8";
	private static final String DIR = "docs";

	public static void main(String[] args) {
		File docsDir = new File(DIR);
		if (docsDir.isDirectory()) {
			File[] listFiles = docsDir.listFiles();
			for (File file : listFiles) {
				if (file.isFile() && !file.getName().startsWith("~")
						&& file.getName().endsWith(".doc")) {
					handleDocFile(file);
				}
			}
		}
	}

	private static void handleDocFile(File file) {
		String filename = FilenameUtils.getBaseName(file.getName());
		System.out.print("========== " + file.getName() + " ==========");

		File dstDir = new File(file.getParent(), filename);
		dstDir.mkdirs();
		try (FileInputStream fis = new FileInputStream(file);
				HWPFDocument doc = new HWPFDocument(fis)) {

			DocParser docParser = DocParser.parseFromFile(file);
			List<String> paragraphs = docParser.getParagraphs();
			Queue<String> tables = docParser.getTables();
			Queue<String> pictures = docParser.extractPicturesToFiles("pic.%i");

			StringBuilder mdContent = new StringBuilder();
			for (String paragraph : paragraphs) {
				paragraph = "{table}".equals(paragraph) ? "\n" + tables.poll() : paragraph;
				paragraph = "{picture}".equals(paragraph) ? "![](" + pictures.poll() + ")\n"
						: paragraph;
				mdContent.append(paragraph).append("\n");
			}

			File dstFile = new File(dstDir, filename + ".md");
			FileUtils.writeStringToFile(dstFile, mdContent.toString(), UTF_8);
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
