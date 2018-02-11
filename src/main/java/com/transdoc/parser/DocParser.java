package com.transdoc.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.PicturesTable;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;

import com.transdoc.model.Article;
import com.transdoc.model.DocParagraph;
import com.transdoc.model.Image;
import com.transdoc.model.DocTable;
import com.transdoc.util.StringUtils;

/**
 * DocParser
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocParser extends BaseWordParser {

	/** doc文档对象 */
	private HWPFDocument doc;
	/** 文档整体范围 */
	private Range docRange;
	/** 文档内容 */
	private Article article;
	/** 文档的图片对象 */
	private List<DocPicture> pictures;

	DocParser(InputStream input) throws IOException {
		doc = new HWPFDocument(input);
		input.close();
		docRange = doc.getRange();
		article = new Article();
		parsePictures();
		parseTables();
		parseParagraphs();
	}

	@Override
	public Article getArticle() {
		return article;
	}

	@Override
	public List<Image> getImages() {
		List<Image> images = new ArrayList<Image>();
		if (!pictures.isEmpty()) {
			for (DocPicture docPicture : pictures) {
				Picture picture = docPicture.picture;

				Image image = new Image();
				image.setContent(picture.getContent());
				image.setExtension(picture.suggestFileExtension());
				images.add(image);
			}
		}
		return images;
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
				Article cellContent = this.getCellContent(tr.getCell(0), true);
				docTable.setCell(0, 0, cellContent);
			} else {
				// 表格
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
						Article cellContent = new Article();
						if (j < numCells) {
							cellContent = this.getCellContent(tr.getCell(j), false);
						}
						docTable.setCell(i, j, cellContent);
					}
				}
			}
			tables.add(docTable);
		}
		article.setTables(tables);
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
	private Article getCellContent(TableCell cell, boolean isCodeBlock) {
		int numParagraphs = cell.numParagraphs();
		List<DocParagraph> paragraphs = new ArrayList<DocParagraph>();
		for (int k = 0; k < numParagraphs; k++) {
			Paragraph paragraph = cell.getParagraph(k);
			String text = paragraph.text();
			text = cleanText(text);
			if (!"".equals(text.trim())) {
				text = isCodeBlock ? StringUtils.rtrim(text) : text.trim();
				if (isCodeBlock) {
					if (paragraph.getFirstLineIndent() >= 400) {
						text = "\t" + text;
					}
				}
				paragraphs.add(new DocParagraph(text));
			}
		}
		return new Article(paragraphs);
	}

	/**
	 * 解析doc文档,获取所有段落,包括表格和图片,以占位字符串的形式表示
	 */
	private void parseParagraphs() {
		LinkedList<DocParagraph> paragraphs = new LinkedList<DocParagraph>();
		int numParagraphs = docRange.numParagraphs();
		int picIndex = 0, listNum = 0, listIndex = 0;
		boolean isInList = false;
		for (int i = 0; i < numParagraphs; i++) {
			Paragraph paragraph = docRange.getParagraph(i);
			if (hasPicture(paragraph, picIndex)) {
				// 图片
				picIndex++;
				paragraphs.add(new DocParagraph("{picture}", isInList));
			} else if (paragraph.isInTable()) {
				// 表格或代码块
				if (paragraphs.isEmpty() || !"{table}".equals(paragraphs.getLast().getContent())) {
					paragraphs.add(new DocParagraph("{table}", isInList));
				}
			} else if (paragraph.isInList()) {
				// 列表
				StringBuilder content = new StringBuilder();
				// 标题段落处理(由doc段落大纲级别定义)
				if (paragraph.getIlfo() != listNum) {
					listNum = paragraph.getIlfo();
					listIndex = 0;
				}
				content.append("{l").append(++listIndex).append("}")
						.append(cleanText(paragraph.text()).trim());
				paragraphs.add(new DocParagraph(content.toString(), false));
				isInList = true;
			} else {
				// 文字
				String text = paragraph.text();
				if (null == text || "".equals(text.trim())) {
					continue;
				}
				StringBuilder content = new StringBuilder();
				int lvl = paragraph.getLvl();
				if (lvl >= 0 && lvl < 6) {
					// 标题段落处理(由doc段落大纲级别定义)
					content.append("{h").append(++lvl).append("}")
							.append(cleanText(paragraph.text()).trim());
					isInList = false;
				} else {
					int numCharacterRuns = paragraph.numCharacterRuns();
					for (int j = 0; j < numCharacterRuns; j++) {
						// 检测文本样式
						CharacterRun characterRun = paragraph.getCharacterRun(j);
						String prefix = "", suffix = "";
						String characterRunText = characterRun.text();
						if (!"".equals(characterRunText.trim())) {
							if (characterRun.isBold()) {
								prefix = suffix = "**";
								characterRunText = StringUtils.rtrim(characterRunText);
							} else if (characterRun.isItalic()) {
								prefix = suffix = "*";
								characterRunText = StringUtils.rtrim(characterRunText);
							}
						}
						if (content.length() > 0 && content.charAt(content.length() - 1) == '*') {
							content.append(" ");
						}
						content.append(prefix).append(characterRunText).append(suffix);
					}
				}
				String contentStr = cleanText(content.toString()).trim();
				paragraphs.add(new DocParagraph(contentStr, isInList));
			}
		}
		article.setParagraphs(paragraphs);
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

	/**
	 * 强制识别去除非法字符
	 * 
	 * @param text
	 *            解析的文本内容
	 * @return 去除非法字符后的文本
	 */
	private String cleanText(String text) {
		return Range.stripFields(text).replaceAll("||HYPERLINK \".+\"", "");
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