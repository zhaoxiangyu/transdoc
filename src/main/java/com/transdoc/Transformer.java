package com.transdoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.transdoc.converter.MarkdownConverter;
import com.transdoc.model.Article;
import com.transdoc.model.Image;
import com.transdoc.parser.BaseWordParser;
import com.transdoc.util.FileUtils;

public class Transformer {

	public String toMarkdown(File docFile) throws IOException {
		return this.toMarkdown(new FileInputStream(docFile), docFile.getName());
	}

	public String toMarkdown(File docFile, File pictureDir) throws IOException {
		return this.toMarkdown(new FileInputStream(docFile), docFile.getName(), pictureDir);
	}

	public String toMarkdown(InputStream docStream, String filename) throws IOException {
		return this.toMarkdown(docStream, filename, null);
	}

	public String toMarkdown(InputStream docStream, String filename, File pictureDir)
			throws IOException {
		// 解析并获取word解析的通用数据对象
		BaseWordParser wordParser = BaseWordParser.parse(docStream);
		Article article = wordParser.getArticle();

		if (pictureDir != null) {
			List<Image> images = wordParser.getImages();
			this.extractPictures(images, pictureDir, article);
		}

		// 将通用数据对象内容转换为md格式
		MarkdownConverter mdConvertor = new MarkdownConverter(article);
		return mdConvertor.toMdString();
	}

	private void extractPictures(List<Image> picturesDatas, File pictureDir, Article article)
			throws IOException {
		if (picturesDatas.isEmpty()) {
			return;
		}
		pictureDir.mkdir();
		for (int i = 0, len = picturesDatas.size(); i < len; i++) {
			Image pictureData = picturesDatas.get(i);
			String picFilename = "pic_" + (i < 10 ? "0" : "") + i + "." + pictureData.getExtension();
			article.getPicturePaths().add(pictureDir.getName() + "/" + picFilename);

			File picFile = new File(pictureDir, picFilename);
			FileUtils.writeByteArrayToFile(picFile, pictureData.getData());
		}
	}
}
