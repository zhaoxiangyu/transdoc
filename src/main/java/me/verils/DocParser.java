package me.verils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;

public class DocParser {

	private File docFile;
	private String docFileName;
	private String docDirPath;
	private HWPFDocument doc;
	private Range docRange;

	public static DocParser parseFromFile(File file) throws IOException {
		DocParser docParser = new DocParser();
		docParser.docFile = file;
		docParser.docFileName = file.getName().substring(0, file.getName().lastIndexOf("."));
		docParser.docDirPath = file.getParent();
		docParser.doc = new HWPFDocument(new FileInputStream(file));
		docParser.docRange = docParser.doc.getRange();
		return docParser;
	}

	private DocParser() {
	}

	public String getText() throws IOException {
		try (WordExtractor wordExtractor = new WordExtractor(doc)) {
			String text = wordExtractor.getText();
			return text;
		}
	}

	public List<Picture> getPictures() {
		return getPictures(true);
	}

	private List<Picture> getPictures(boolean isContained) {
		List<Picture> pictures = new ArrayList<>();
		PicturesTable picturesTable = doc.getPicturesTable();
		int numCharacterRuns = docRange.numCharacterRuns();
		for (int i = 0; i < numCharacterRuns; i++) {
			CharacterRun characterRun = docRange.getCharacterRun(i);
			if (picturesTable.hasPicture(characterRun)) {
				Picture picture = picturesTable.extractPicture(characterRun, isContained);
				pictures.add(picture);
			}
		}
		return pictures;
	}

	public void extractPicturesToFile(String picNamePattern) throws IOException {
		List<Picture> pictures = getPictures(false);
		for (int i = 0, size = pictures.size(); i < size; i++) {
			Picture picture = pictures.get(i);
			String picFilePath = "%d/%n/picture/%p.%e";
			picFilePath = picFilePath.replace("%d", docDirPath);
			picFilePath = picFilePath.replace("%n", docFileName);
			picFilePath = picFilePath.replace("%p", picNamePattern);
			picFilePath = picFilePath.replace("%i", (i < 10 ? "0" : "") + i);
			picFilePath = picFilePath.replace("%e", picture.suggestFileExtension());
			File picFile = new File(picFilePath.toString());
			picFile.getParentFile().mkdirs();
			picture.writeImageContent(new FileOutputStream(picFile));
		}
	}

	public File getDocFile() {
		return docFile;
	}
}
