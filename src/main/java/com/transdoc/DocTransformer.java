package com.transdoc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import com.transdoc.converter.MarkdownConverter;
import com.transdoc.exception.WordParsingException;
import com.transdoc.exception.WordTransformingException;
import com.transdoc.model.DocContent;
import com.transdoc.model.DocPictureData;
import com.transdoc.parser.BaseWordParser;

/**
 * DocTransformer
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocTransformer {

	/** 转换文档结果存放目录 */
	private String dstDirPath;

	/** 从文档中提取的图片存放目录名称 */
	private String picDirName = "pic";

	/** 从文档中提取的图片命名模式 */
	private String picFilenamePattern = "pic_%d";

	/**
	 * 默认构造函数
	 * 
	 * @param dstDirPath
	 *            转换文档结果存放目录
	 */
	public DocTransformer(String dstDirPath) {
		super();
		this.dstDirPath = dstDirPath;
		checkAndMkDirs();
	}

	/**
	 * 将文档转换为Markdown文档,并计算处理所消耗的时长
	 * 
	 * @param docFiles
	 *            准备进行转换的文档文件
	 * @return 处理所消耗的时间毫秒数(ms),如果转换失败,返回-1
	 * @exception WordTransformingException
	 */
	public long toMardkown(File file) throws WordTransformingException {
		try {
			return tryConvertToMarkdown(file.getName(), new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return -1;
		}
	}

	/**
	 * 返回输出目录
	 * 
	 * @return 输出目录
	 */
	public String getOutputDir() {
		return dstDirPath;
	}

	private void checkAndMkDirs() {
		File dstDir = new File(dstDirPath);
		if (!dstDir.exists()) {
			dstDir.mkdirs();
		}
	}

	/**
	 * 执行转换并捕获异常,返回执行时长
	 * 
	 * @param filename
	 *            源word文档的文件名
	 * @param input
	 *            源word文档的读取输入流
	 * @return 执行转换所消耗的时长
	 * @throws WordTransformingException
	 */
	private long tryConvertToMarkdown(String filename, InputStream input)
			throws WordTransformingException {
		try {
			long startTime = System.currentTimeMillis();
			doConvertToMarkdown(filename, input);
			long spendTime = System.currentTimeMillis() - startTime;
			return spendTime;
		} catch (WordParsingException e) {
			throw new WordTransformingException(filename + " - 转换失败!文件格式错误", e);
		}
	}

	/**
	 * 转换函数,从word中提取内容并转换为MD.该函数应提取为单独对象的函数
	 * 
	 * @param filename
	 *            源word文档的文件名
	 * @param input
	 *            源word文档的读取输入流
	 * @throws WordTransformingException
	 *             转换异常
	 */
	private void doConvertToMarkdown(String filename, InputStream input)
			throws WordTransformingException {
		// 创建目录和文件
		String docName = filename.substring(0, filename.lastIndexOf("."));
		File dstDir = new File(dstDirPath, docName);
		dstDir.mkdir();
		File mdFile = new File(dstDir, docName + ".md");
		File picDir = new File(dstDir, picDirName);

		// 解析并获取word解析的通用数据对象
		BaseWordParser wordParser = BaseWordParser.parse(input);
		DocContent docContent = wordParser.getDocContent();
		List<DocPictureData> picturesDatas = wordParser.getPictureDatas();
		this.extractPictures(picturesDatas, picDir, docContent);

		// 将通用数据对象内容转换为md格式
		MarkdownConverter mdConvertor = new MarkdownConverter(docContent);
		String mdString = mdConvertor.toMdString();

		// 输出到文件
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(mdFile, "UTF-8");
			pw.print(mdString);
		} catch (FileNotFoundException e) {
		} catch (UnsupportedEncodingException e) {
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	private void extractPictures(List<DocPictureData> picturesDatas, File picDir,
			DocContent docContent) {
		if (picturesDatas.isEmpty()) {
			return;
		}
		picDir.mkdir();
		for (int i = 0, len = picturesDatas.size(); i < len; i++) {
			DocPictureData pictureData = picturesDatas.get(i);
			String picFilename = picFilenamePattern.replace("%d", (i < 10 ? "0" : "") + i) + "."
					+ pictureData.getExtension();
			docContent.getPicturePaths().add(picDirName + "/" + picFilename);

			File picFile = new File(picDir, picFilename);
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(picFile);
				FileChannel channel = fos.getChannel();
				ByteBuffer buffer = ByteBuffer.wrap(pictureData.getContent());
				channel.write(buffer);
				channel.close();
			} catch (IOException e) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
}
