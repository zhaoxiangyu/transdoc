package com.transdoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.transdoc.converter.MarkdownConverter;
import com.transdoc.model.DocContent;
import com.transdoc.model.DocPictureData;
import com.transdoc.parser.BaseWordParser;
import com.transdoc.util.FileUtils;

public class Transformer {

	public String toMarkdown(File docFile) throws IOException {
		return this.toMarkdown(new FileInputStream(docFile), docFile.getName());
	}

	public String toMarkdown(File docFile, boolean extractPictures, File pictureDir)
			throws IOException {
		return this.toMarkdown(new FileInputStream(docFile), docFile.getName(), extractPictures,
				pictureDir);
	}

	public String toMarkdown(InputStream docStream, String filename) throws IOException {
		return this.toMarkdown(docStream, filename, false, null);
	}

	public String toMarkdown(InputStream docStream, String filename, boolean extractPictures,
			File pictureDir) throws IOException {
		// 解析并获取word解析的通用数据对象
		BaseWordParser wordParser = BaseWordParser.parse(docStream);
		DocContent docContent = wordParser.getDocContent();

		if (extractPictures) {
			List<DocPictureData> picturesDatas = wordParser.getPictureDatas();
			this.extractPictures(picturesDatas, pictureDir, docContent);
		}

		// 将通用数据对象内容转换为md格式
		MarkdownConverter mdConvertor = new MarkdownConverter(docContent);
		return mdConvertor.toMdString();
	}

	private void extractPictures(List<DocPictureData> picturesDatas, File pictureDir,
			DocContent docContent) throws IOException {
		if (picturesDatas.isEmpty()) {
			return;
		}
		pictureDir.mkdir();
		for (int i = 0, len = picturesDatas.size(); i < len; i++) {
			DocPictureData pictureData = picturesDatas.get(i);
			String picFilename = "pic_" + (i < 10 ? "0" : "") + i + "." + pictureData.getExtension();
			docContent.getPicturePaths().add(pictureDir.getName() + "/" + picFilename);

			File picFile = new File(pictureDir, picFilename);
			FileUtils.writeByteArrayToFile(picFile, pictureData.getContent());
		}
	}
}
