package com.transdoc;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.transdoc.exception.WordTransformingException;
import com.transdoc.logging.TransdocLogger;
import com.transdoc.util.FileUtils;

/**
 * Transdoc
 *
 * @author Verils
 * @date 2017-10-20
 */
public class Transdoc {

	private static final TransdocLogger LOGGER = TransdocLogger.getLog(Transdoc.class);

	static final FileFilter WORD_FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String filename = file.getName();
			return file.isFile() && (filename.endsWith(".doc") || filename.endsWith(".docx"))
					&& !filename.startsWith("~");
		}
	};

	static final String DEFAULT_DIR = "./docs";

	/**
	 * 程序入口函数,通过cmd执行传入word文档的路径,可直接读取文件进行转换操作.<br>
	 * 
	 * @param paths
	 *            带转换的doc文件路径
	 * @throws IOException
	 */
	public static void main(String[] paths) throws IOException {
		LOGGER.info("========== Transdoc ==========");
		Transformer transformer = new Transformer();

		List<File> files = null;
		if (paths.length > 0) {
			files = getFilesFromPathParams(paths);
		} else {
			files = getFilesFromDirectory(DEFAULT_DIR);
		}

		if (files != null) {
			process(transformer, files);
		}
	}

	/**
	 * 获取命令执行参数中的文件
	 * 
	 * @param paths
	 * @return 文件列表
	 */
	private static List<File> getFilesFromPathParams(String[] paths) {
		List<File> files = new ArrayList<File>();
		for (String path : paths) {
			files.add(new File(path));
		}
		return files;
	}

	/**
	 * 从默认文件夹中获取文件
	 * 
	 * @param dirPath
	 * @return 文件列表
	 */
	private static List<File> getFilesFromDirectory(String dirPath) {
		List<File> files = new ArrayList<File>();
		File srcDir = new File(dirPath);
		String dirName = srcDir.getName();
		if (!srcDir.exists()) {
			LOGGER.error(dirName + "目录不存在,未执行文件扫描");
		} else if (!srcDir.isDirectory()) {
			LOGGER.error(dirName + "不是有效的目录,未执行文件扫描");
		} else {
			File[] listFiles = srcDir.listFiles(WORD_FILE_FILTER);
			if (listFiles != null && listFiles.length > 0) {
				files = Arrays.asList(listFiles);
			} else {
				LOGGER.error(dirName + "目录中没有可转换的文件");
			}
		}
		return files;
	}

	private static void process(final Transformer transformer, List<File> files) {
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		ExecutorService executor = new ThreadPoolExecutor(4, 50, 5L, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), threadFactory);

		for (final File file : files) {
			executor.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long startTime = System.currentTimeMillis();

						String docFilename = file.getName();
						docFilename = docFilename.substring(0, docFilename.lastIndexOf("."));

						File destDir = new File(DEFAULT_DIR, docFilename);
						destDir.mkdir();

						File pictureDir = new File(destDir, "pictures");
						String markdown = transformer.toMarkdown(file, true, pictureDir);

						File mdFile = new File(destDir, docFilename + ".md");
						FileUtils.writeStringToFile(mdFile, markdown, "UTF-8");

						long spendTime = System.currentTimeMillis() - startTime;
						LOGGER.info(file.getName() + " - 转换成功，用时: " + spendTime + "ms");
					} catch (WordTransformingException e) {
						LOGGER.warn(e.getMessage(), e);
					} catch (IOException e) {
						LOGGER.warn(e.getMessage(), e);
					}
				}
			});
		}

		executor.shutdown();
	}
}
