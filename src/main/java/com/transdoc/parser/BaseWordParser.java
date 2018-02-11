package com.transdoc.parser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.transdoc.exception.WordParsingException;
import com.transdoc.model.Article;
import com.transdoc.model.Image;

/**
 * BaseWordParser
 *
 * @author Verils
 * @date 2017-10-20
 */
public abstract class BaseWordParser {

	public static BaseWordParser parse(InputStream input) {
		byte[] data;
		try {
			// 从流中读取数据并缓存
			BufferedInputStream bis = new BufferedInputStream(input);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			byte[] bs = new byte[1024];
			while ((len = bis.read(bs)) != -1) {
				baos.write(bs, 0, len);
			}
			data = baos.toByteArray();
		} catch (IOException e) {
			throw new WordParsingException("无法识别的文件类型", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
			}
		}

		try {
			// 尝试以Docx文件格式解析
			input = new ByteArrayInputStream(data);
			return new DocxParser(input);
		} catch (Exception e) {
			// 此处极可能为UnsupportedFileFormatException
			try {
				// 尝试以Doc文件格式解析
				input = new ByteArrayInputStream(data);
				return new DocParser(input);
			} catch (IOException ex) {
				throw new WordParsingException("不是有效的doc文件或docx文件", ex);
			}
		}

	}

	BaseWordParser() {
	}

	/**
	 * 读取文件准备进行解析
	 * 
	 * @param file
	 *            文档文件
	 * @throws IOException
	 */
	BaseWordParser(File file) {
	}

	/**
	 * 读取流准备进行解析
	 * 
	 * @param input
	 *            输入流
	 * @throws IOException
	 */
	BaseWordParser(InputStream input) {
	}

	/**
	 * 获取文档内容
	 * 
	 * @return 解析文档得到的通用对象
	 */
	public abstract Article getArticle();

	/**
	 * 提取doc文档中所有图片的二进制数据
	 * 
	 * @return 图片二进制数据列表
	 */
	public abstract List<Image> getImages();
}
