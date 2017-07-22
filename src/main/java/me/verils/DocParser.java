package me.verils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;

public class DocParser {

	private File docFile;
	private HWPFDocument doc;
	private Range docRange;

	private List<DocPicture> docPictures;

	/**
	 * 静态工厂方法,读取一个文件并进行解析
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static DocParser parseFromFile(File file) throws IOException {
		DocParser docParser = new DocParser(file);
		return docParser;
	}

	/**
	 * 私有构造函数
	 * 
	 * @param file
	 * @throws IOException
	 */
	private DocParser(File file) throws IOException {
		this.docFile = file;
		this.doc = new HWPFDocument(new FileInputStream(file));
		this.docRange = doc.getRange();
		this.docPictures = this.getPictures(docRange, false);
	}

	/**
	 * 解析doc文件中的所有文本内容
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getAllText() throws IOException {
		try (WordExtractor wordExtractor = new WordExtractor(doc)) {
			String text = wordExtractor.getText();
			return text;
		}
	}

	/**
	 * 获取doc文件中的所有段落,包括表格和图片,以占位字符串的形式表示
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getParagraphs() throws Exception {
		List<String> paragraphs = new LinkedList<>();
		int numParagraphs = docRange.numParagraphs();
		int picIndex = 0;
		boolean isPreParagraphTable = false;
		for (int i = 0; i < numParagraphs; i++) {
			Paragraph paragraph = docRange.getParagraph(i);
			if (hasPicture(paragraph, picIndex)) {
				picIndex++;
				paragraphs.add("{picture}");
			} else if (paragraph.isInTable()) {
				if (!isPreParagraphTable) {
					paragraphs.add("{table}");
				}
				isPreParagraphTable = true;
			} else {
				paragraphs.add(WordExtractor.stripFields(paragraph.text()).trim());
				isPreParagraphTable = false;
			}
		}
		return paragraphs;
	}

	/**
	 * 判断段落中是否包含图片
	 * 
	 * @param paragraph
	 * @param index
	 *            已解析并识别的图片序号
	 * @return
	 */
	private boolean hasPicture(Paragraph paragraph, int index) {
		if (index >= docPictures.size()) {
			return false;
		}
		DocPicture docPicture = docPictures.get(index);
		CharacterRun characterRun = docPicture.getCharacterRun();
		if (characterRun.getStartOffset() >= paragraph.getStartOffset()
				&& characterRun.getEndOffset() <= paragraph.getEndOffset()) {
			return true;
		}
		return false;
	}

	/**
	 * 获取doc文档中的表格内容
	 * 
	 * @return
	 */
	public Queue<String> getTables() {
		Queue<String> tablesText = new LinkedList<>();
		TableIterator tableIterator = new TableIterator(docRange);
		// 遍历表格
		while (tableIterator.hasNext()) {
			StringBuilder tableContent = new StringBuilder();
			Table table = tableIterator.next();
			int numRows = table.numRows();
			TableRow tr = table.getRow(0);
			int numCells = tr.numCells();

			if (numRows == 1 && numCells == 1) {
				// 独立代码块单元格
				String cellText = tr.getCell(0).text().trim();
				tableContent.append("```\n").append(cellText).append("\n```\n");
			} else {
				// 正常表格
				for (int i = 0; i < numRows; i++) {
					tableContent.append("|");
					tr = table.getRow(i);
					numCells = tr.numCells();
					for (int j = 0; j < numCells; j++) {
						TableCell td = tr.getCell(j);
						String tdText = td.text().trim();
						tableContent.append(tdText).append("|");
					}
					if (i == 0) {
						tableContent.append("\n|");
						for (int j = 0; j < numCells; j++) {
							tableContent.append("-----|");
						}
					}
					tableContent.append("\n");
				}
			}
			tablesText.add(tableContent.toString());
		}
		return tablesText;
	}

	/**
	 * 获取doc文档中的图片内容
	 * 
	 * @param range
	 * @param isContained
	 * @return
	 */
	private List<DocPicture> getPictures(Range range, boolean isContained) {
		List<DocPicture> docPictures = new ArrayList<>();
		PicturesTable picturesTable = doc.getPicturesTable();
		int numCharacterRuns = docRange.numCharacterRuns();
		for (int i = 0; i < numCharacterRuns; i++) {
			CharacterRun characterRun = docRange.getCharacterRun(i);
			if (picturesTable.hasPicture(characterRun)) {
				Picture picture = picturesTable.extractPicture(characterRun, isContained);
				docPictures.add(new DocPicture(characterRun, picture));
			}
		}
		return docPictures;
	}

	/**
	 * 将doc文档中的图片导出到指定的目录,并定义图片命名的格式
	 * 
	 * @param picDir
	 *            图片存放的目录名称,该文件夹位置与doc文档文件处于同一目录内
	 * @param picNamePattern
	 *            图片命名格式,需包含"%i"作为图片的顺序,如"pic%i",则图片命名为"pic00.jpg","pic01.png"...
	 *            文件的后缀名定义由文档中的图片格式定义
	 * @return
	 * @throws IOException
	 */
	public Queue<String> extractPicturesToFiles(String picNamePattern) throws IOException {
		String docName = docFile.getName();
		docName = docName.substring(0, docName.lastIndexOf("."));
		File dir = new File(docFile.getParent(), docName);
		dir.mkdirs();
		Queue<String> picUris = new LinkedList<>();
		for (int i = 0, size = docPictures.size(); i < size; i++) {
			Picture picture = docPictures.get(i).getPicture();
			String filename = picNamePattern.replace("%i", (i < 10 ? "0" : "") + i) + "."
					+ picture.suggestFileExtension();
			File picFile = new File(dir, filename);
			picUris.add(picFile.getName());
			picture.writeImageContent(new FileOutputStream(picFile));
		}
		return picUris;
	}

	public File getDocFile() {
		return docFile;
	}
}

class DocPicture {
	private CharacterRun characterRun;
	private Picture picture;

	public DocPicture(CharacterRun characterRun, Picture picture) {
		super();
		this.characterRun = characterRun;
		this.picture = picture;
	}

	public CharacterRun getCharacterRun() {
		return characterRun;
	}

	public void setCharacterRun(CharacterRun characterRun) {
		this.characterRun = characterRun;
	}

	public Picture getPicture() {
		return picture;
	}

	public void setPicture(Picture picture) {
		this.picture = picture;
	}

}
