package com.transdoc.converter;

import java.util.List;
import java.util.Queue;

import com.transdoc.model.Article;
import com.transdoc.model.DocParagraph;
import com.transdoc.model.DocTable;

/**
 * MarkdownConverter
 *
 * @author Verils
 * @date 2017-10-20
 */
public class MarkdownConverter {

	private Article article;

	public MarkdownConverter(Article article) {
		super();
		this.article = article;
	}

	public String toMdString() {
		return article == null ? "" : toMdString(article);
	}

	private String toMdString(Article article) {
		if (article == null) {
			// 合并单元格时,可能存在空的单元格
			return "";
		}
		
		List<DocParagraph> paragraphs = article.listParagraphs();
		Queue<DocTable> tables = article.listTables();
		Queue<String> pictures = article.listPictures();

		// 将文档信息进行简单组装,获取md格式文本
		StringBuilder mdContent = new StringBuilder();
		for (DocParagraph paragraph : paragraphs) {
			String content = paragraph.getContent();
			boolean isInList = paragraph.isInList();
			if (isInList) {
				mdContent.append("\t");
			}
			if ("{picture}".equals(content)) {
				// 图片
				if (!pictures.isEmpty()) {
					content = "![](" + pictures.poll() + ")";
				}
			} else if ("{table}".equals(content)) {
				// 表格
				if (!tables.isEmpty()) {
					DocTable docTable = tables.poll();
					content = toTableMDText(docTable);
					if (isInList) {
						if (!docTable.isBlock()) {
							mdContent.deleteCharAt(mdContent.length() - 2);
						}
						content = content.replaceAll("\n", "\n\t");
					}
				}
			} else if (content.startsWith("{h")) {
				// 标题
				int lvl = content.charAt(2) - 48;
				if (lvl != 1) {
					// 题目前不添加空行
					mdContent.append("\n");
				}
				for (int i = 0; i < lvl; i++) {
					mdContent.append("#");
				}
				mdContent.append(" ");
				content = content.substring(4);
			} else if (content.startsWith("{l")) {
				// 列表
				char ch = content.charAt(2);
				mdContent.append(ch).append(". ");
				content = content.substring(4);
			}
			mdContent.append(content).append("\n\n");
		}
		String md = mdContent.toString();
		md = md.replaceAll("\n{2,}", "\n\n");
		return md;

	}

	/**
	 * 将表格数据对象转换为有效的md格式文本
	 * 
	 * @param table
	 *            表格对象
	 * @return md格式的表格内容
	 */
	private String toTableMDText(DocTable table) {
		StringBuilder tableContent = new StringBuilder();
		if (table == null) {
		} else if (table.isBlock()) {
			// 代码块
			Article cell = table.getCell(0, 0);
			List<DocParagraph> paragraphs = cell.getParagraphs();
			tableContent.append("```\n");
			for (DocParagraph docParagraph : paragraphs) {
				tableContent.append(docParagraph.getContent()).append("\n");
			}
			tableContent.append("```");
		} else {
			// 表格
			int rownum = table.getRownum();
			int colnum = table.getColnum();
			tableContent.append("\n");
			for (int i = 0; i < rownum; i++) {
				tableContent.append("|");
				for (int j = 0; j < colnum; j++) {
					String mdString = this.toMdString(table.getCell(i, j));
					mdString = mdString.trim().replaceAll("\n", "<br>");
					tableContent.append(mdString).append("|");
				}
				if (i == 0) {
					tableContent.append("\n|");
					for (int j = 0; j < colnum; j++) {
						tableContent.append("----|");
					}
				}
				tableContent.append("\n");
			}
			tableContent.deleteCharAt(tableContent.length() - 1);
		}
		return tableContent.toString();
	}
}
