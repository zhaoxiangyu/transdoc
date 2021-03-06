package com.transdoc.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;

import com.transdoc.model.DocContent;
import com.transdoc.model.DocParagraph;
import com.transdoc.model.DocPictureData;
import com.transdoc.model.DocTable;
import com.transdoc.util.StringUtils;

/**
 * DocxParser
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocxParser extends BaseWordParser {

	/** doc文档对象 */
	private XWPFDocument doc;
	/** 文档内容 */
	private DocContent docContent;
	/** 文档的图片对象 */
	private List<DocxPicture> pictures;

	DocxParser(InputStream input) throws IOException {
		doc = new XWPFDocument(input);
		input.close();
		docContent = new DocContent();
		parse();
	}

	@Override
	public DocContent getDocContent() {
		return docContent;
	}

	@Override
	public List<DocPictureData> getPictureDatas() {
		List<DocPictureData> docPictureDatas = new ArrayList<DocPictureData>();
		if (!pictures.isEmpty()) {
			for (DocxPicture docPicture : pictures) {
				XWPFPictureData picture = docPicture.picture;

				DocPictureData docPictureData = new DocPictureData();
				docPictureData.setContent(picture.getData());
				docPictureData.setExtension(picture.suggestFileExtension());
				docPictureDatas.add(docPictureData);
			}
		}
		return docPictureDatas;
	}

	private void parse() {
		List<DocxPicture> docxPictures = new LinkedList<DocxPicture>();
		LinkedList<DocParagraph> docParagraphs = new LinkedList<DocParagraph>();
		List<DocTable> docTables = new LinkedList<DocTable>();

		Iterator<IBodyElement> bodyElementsIterator = doc.getBodyElementsIterator();
		StringBuilder content = null;
		int outlineLvl = 0;
		int numIndex = 0, lastNumId = 0, numId = 0;
		boolean isInList = false, isListStart = false;
		while (bodyElementsIterator.hasNext()) {
			IBodyElement bodyElement = bodyElementsIterator.next();
			BodyElementType elementType = bodyElement.getElementType();
			switch (elementType) {
			case PARAGRAPH: {
				isListStart = false;
				// 段落
				content = new StringBuilder();
				XWPFParagraph paragraph = (XWPFParagraph) bodyElement;
				CTPPr pPr = paragraph.getCTP().getPPr();

				// 提取图片
				List<XWPFRun> runs = paragraph.getRuns();
				for (XWPFRun run : runs) {
					List<XWPFPicture> embeddedPictures = run.getEmbeddedPictures();
					for (XWPFPicture xwpfPicture : embeddedPictures) {
						docParagraphs.add(new DocParagraph("{picture}", !isListStart && isInList));
						XWPFPictureData pictureData = xwpfPicture.getPictureData();
						docxPictures.add(new DocxPicture(pictureData));
					}
				}

				// 段落文本
				String paragraphText = paragraph.getParagraphText().trim();
				if (!paragraph.isEmpty() && !"".equals(paragraphText)) {
					CTDecimalNumber ctDecimalNumber = pPr.getOutlineLvl();
					outlineLvl = 0;
					if (ctDecimalNumber != null) {
						outlineLvl = ctDecimalNumber.getVal().intValue() + 1;
					}
					CTNumPr numPr = pPr.getNumPr();
					numId = 0;
					if (numPr != null) {
						numId = numPr.getNumId().getVal().intValue();
						if (numId != lastNumId) {
							lastNumId = numId;
							numIndex = 0;
						}
					}
					if (outlineLvl != 0) {
						// 段落标题
						content.append("{h").append(outlineLvl).append("}");
						isInList = false;
					} else if (numId != 0) {
						// 列表
						content.append("{l").append(++numIndex).append("}");
						isListStart = true;
						isInList = true;
					}
					// 文本内容
					content.append(paragraphText);
					docParagraphs.add(new DocParagraph(content.toString(), !isListStart && isInList));
				}
				break;
			}
			case TABLE: {
				// 表格或代码块
				if (docParagraphs.isEmpty()
						|| !"{table}".equals(docParagraphs.getLast().getContent())) {
					docParagraphs.add(new DocParagraph("{table}", isInList));
				}

				DocTable docTable = null;
				XWPFTable table = (XWPFTable) bodyElement;
				int numRows = table.getNumberOfRows();
				XWPFTableRow tr = table.getRow(0);
				List<XWPFTableCell> tds = tr.getTableCells();
				int numCells = tds.size();
				if (numRows == 1 && numCells == 1) {
					// 代码块
					docTable = new DocTable(numRows, numCells);
					DocContent cellContent = this.getCellContent(tds.get(0), true);
					docTable.setCell(0, 0, cellContent);
				} else {
					// 表格
					int maxNumCells = numCells;
					for (int i = 1; i < numRows; i++) {
						tr = table.getRow(i);
						numCells = tr.getTableICells().size();
						maxNumCells = maxNumCells >= numCells ? maxNumCells : numCells;
					}

					docTable = new DocTable(numRows, maxNumCells);
					for (int i = 0; i < numRows; i++) {
						tr = table.getRow(i);
						tds = tr.getTableCells();
						numCells = tds.size();
						for (int j = 0; j < numCells; j++) {
							DocContent cellContent = new DocContent();
							if (j < numCells) {
								cellContent = this.getCellContent(tr.getCell(j), false);
							}
							docTable.setCell(i, j, cellContent);
						}
					}
				}
				docTables.add(docTable);
				break;
			}
			default:
				break;
			}
		}
		pictures = docxPictures;
		docContent.setParagraphs(docParagraphs);
		docContent.setTables(docTables);
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
	private DocContent getCellContent(XWPFTableCell tableCell, boolean isCodeBlock) {
		List<DocParagraph> paragraphs = new ArrayList<DocParagraph>();
		List<XWPFParagraph> xwpfParagraphs = tableCell.getParagraphs();
		for (XWPFParagraph paragraph : xwpfParagraphs) {
			String text = paragraph.getParagraphText();
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
		return new DocContent(paragraphs);
	}
}

/**
 * 内部类,装载docx文件解析出的picture对象
 */
class DocxPicture {
	XWPFPictureData picture;

	DocxPicture(XWPFPictureData picture) {
		super();
		this.picture = picture;
	}
}