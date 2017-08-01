package me.verils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

	/** doc文档对象 */
	private HWPFDocument doc;
	/** 文档整体范围 */
	private Range docRange;

	/** 文档的所有段落 */
	private List<DocParagraph> paragraphs;
	/** 文档的所有表格和代码块 */
	private List<DocTable> tables;
	/** 文档的图片对象 */
	private List<DocPicture> pictures;
	/** 文档的所有图片导出后的本地相对路径 */
	private List<String> picturePaths;

	/**
	 * 私有构造函数
	 * 
	 * @param file
	 *            文档所在的文件
	 * @throws Exception
	 *             解析时如有问题则返回异常
	 */
	public DocParser(File file) throws Exception {
		doc = new HWPFDocument(new FileInputStream(file));
		docRange = doc.getRange();
		parseTables();
		parsePictures();
		parseParagraphs(docRange);
	}

	/**
	 * 将doc文档中的图片导出到指定的目录,并按照给定的格式命名.如果目录不存在,则会创建目录
	 * 
	 * @param picDir
	 *            存放导出图片的目录
	 * @param picNamePattern
	 *            图片命名格式
	 * @throws IOException
	 */
	public void extractPicturesToFiles(File picDir, String picNamePattern) throws IOException {
		picturePaths.clear();
		if (!pictures.isEmpty()) {
			picDir.mkdirs();
			String picDirName = picDir.getName();
			for (int i = 0, size = pictures.size(); i < size; i++) {
				Picture picture = pictures.get(i).picture;
				String filename = picNamePattern.replace("%d", (i < 10 ? "0" : "") + i) + "."
						+ picture.suggestFileExtension();
				File picFile = new File(picDir, filename);
				picturePaths.add(picDirName + "/" + picFile.getName());
				FileOutputStream fos = new FileOutputStream(picFile);
				picture.writeImageContent(fos);
				fos.flush();
				fos.close();
			}
		}
	}

	/**
	 * 获取文档的所有段落,包含标题,文本段落,以及表格和图片的占位符,占位符占一个独立的段落,每行视为一个段落
	 * 
	 * @return 文档解析后的所有段落
	 */
	public List<DocParagraph> listParagraphs() {
		List<DocParagraph> paragraphs = new ArrayList<DocParagraph>();
		paragraphs.addAll(this.paragraphs);
		return paragraphs;
	}

	/**
	 * 获得文档中的所有列表对象,对该列表操作不会影响到原有数据
	 * 
	 * @return 所有列表对象
	 */
	public LinkedList<DocTable> listTables() {
		LinkedList<DocTable> tables = new LinkedList<DocTable>();
		tables.addAll(this.tables);
		return tables;
	}

	/**
	 * 获得文档中的所有图片对象,对该列表操作不会影响到原有数据
	 * 
	 * @return 所有图片对象
	 */
	public LinkedList<String> listPictures() {
		LinkedList<String> pictures = new LinkedList<String>();
		pictures.addAll(this.picturePaths);
		return pictures;
	}

	/**
	 * 解析doc文档,获取所有表格的内容
	 */
	private void parseTables() {
		LinkedList<DocTable> tables = new LinkedList<DocTable>();
		TableIterator tableIterator = new TableIterator(docRange);
		// 遍历表格
		while (tableIterator.hasNext()) {
			Table table = tableIterator.next();
			int numRows = table.numRows();
			TableRow tr = table.getRow(0);
			int numCells = tr.numCells();
			DocTable docTable = null;
			if (numRows == 1 && numCells == 1) {
				// 代码块
				docTable = new DocTable(numRows, numCells);
				TableCell cell = tr.getCell(0);
				docTable.setCell(0, 0, this.getCellTextContent(cell, true));
			} else {
				// 正常表格
				int maxNumCells = numCells;
				for (int i = 1; i < numRows; i++) {
					tr = table.getRow(i);
					numCells = tr.numCells();
					maxNumCells = maxNumCells >= numCells ? maxNumCells : numCells;
				}
				docTable = new DocTable(numRows, maxNumCells);
				for (int i = 0; i < numRows; i++) {
					tr = table.getRow(i);
					numCells = tr.numCells();
					for (int j = 0; j < maxNumCells; j++) {
						StringBuilder tdContent = new StringBuilder();
						if (j < numCells) {
							TableCell cell = tr.getCell(j);
							tdContent.append(this.getCellTextContent(cell, false));
						}
						docTable.setCell(i, j, tdContent.toString());
					}
				}
			}
			tables.add(docTable);
		}
		this.tables = tables;
	}

	/**
	 * 提取单元格的文本内容
	 * 
	 * @param cell
	 *            单元格
	 * @param isCodeBlock
	 *            是否是代码块
	 * @return 提取出来的文本内容
	 */
	private String getCellTextContent(TableCell cell, boolean isCodeBlock) {
		int numParagraphs = cell.numParagraphs();
		StringBuilder tdContent = new StringBuilder();
		for (int k = 0; k < numParagraphs; k++) {
			String text = WordExtractor.stripFields(cell.getParagraph(k).text());
			if (!isCodeBlock) {
				text = text.trim();
			}
			if (!"".equals(text)) {
				tdContent.append(text).append("\n");
			}
		}
		if (tdContent.length() > 0) {
			tdContent.deleteCharAt(tdContent.length() - 1);
		}
		return tdContent.toString();
	}

	/**
	 * 解析doc文档,获取所有图片内容
	 */
	private void parsePictures() {
		List<DocPicture> docPictures = new ArrayList<DocPicture>();
		PicturesTable picturesTable = doc.getPicturesTable();
		int numCharacterRuns = docRange.numCharacterRuns();
		for (int i = 0; i < numCharacterRuns; i++) {
			CharacterRun characterRun = docRange.getCharacterRun(i);
			if (picturesTable.hasPicture(characterRun)) {
				Picture picture = picturesTable.extractPicture(characterRun, false);
				docPictures.add(new DocPicture(characterRun, picture));
			}
		}
		this.pictures = docPictures;
		this.picturePaths = new LinkedList<String>();
	}

	/**
	 * 解析doc文档,获取所有段落,包括表格和图片,以占位字符串的形式表示
	 * 
	 * @throws Exception
	 *             解析时出现的异常
	 */
	private void parseParagraphs(Range range) throws Exception {
		LinkedList<DocParagraph> paragraphs = new LinkedList<DocParagraph>();
		int numParagraphs = range.numParagraphs();
		int picIndex = 0, listNum = 0, listIndex = 1;
		for (int i = 0; i < numParagraphs; i++) {
			Paragraph paragraph = range.getParagraph(i);
			if (hasPicture(paragraph, picIndex)) {
				// 图片
				picIndex++;
				paragraphs.add(new DocParagraph("{picture}"));
			} else if (paragraph.isInTable()) {
				// 表格或代码块
				if (paragraphs.isEmpty() || !"{table}".equals(paragraphs.getLast().getContent())) {
					paragraphs.add(new DocParagraph("{table}"));
				}
			} else if (paragraph.isInList()) {
				// 列表
				if (paragraph.getIlfo() != listNum) {
					listNum = paragraph.getIlfo();
					listIndex = 1;
				}
				paragraphs
						.add(new DocParagraph(listIndex++ + ". " + WordExtractor.stripFields(paragraph.text()).trim()));
			} else {
				// 文字
				int lvl = paragraph.getLvl();
				String text = paragraph.text();
				if (null == text || "".equals(text.trim())) {
					continue;
				}
				StringBuilder sb = new StringBuilder();
				if (lvl >= 0 && lvl < 6) {
					// 标题段落处理(由doc段落大纲级别定义)
					sb.append("{h").append(++lvl).append("}");
				}
				sb.append(WordExtractor.stripFields(text).trim());
				paragraphs.add(new DocParagraph(sb.toString()));
			}
		}
		this.paragraphs = paragraphs;
	}

	/**
	 * 判断段落中是否包含图片
	 * 
	 * @param paragraph
	 * @param picIndex
	 * @param index
	 *            已解析并识别的图片序号
	 * @return
	 */
	private boolean hasPicture(Paragraph paragraph, int picIndex) {
		if (picIndex >= pictures.size()) {
			return false;
		}
		DocPicture docPicture = pictures.get(picIndex);
		CharacterRun characterRun = docPicture.characterRun;
		return characterRun.getStartOffset() >= paragraph.getStartOffset()
				&& characterRun.getEndOffset() <= paragraph.getEndOffset();
	}
}

/**
 * 内部类,装载doc文件解析出的picture对象和对应的characterRun
 */
class DocPicture {
	CharacterRun characterRun;
	Picture picture;

	DocPicture(CharacterRun characterRun, Picture picture) {
		super();
		this.characterRun = characterRun;
		this.picture = picture;
	}
}