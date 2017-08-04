package me.verils.transdoc;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import me.verils.transdoc.model.DocContent;

public class Transdoc {

	private static final String DEFAULT_PROPERTIES_FILE_NAME = "config.properties";

	private String srcDirPath;
	private String dstDirPath;
	private String picDirName;
	private String picFilenamePattern;

	private FileFilter docFileFilter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			String filename = file.getName();
			return file.isFile() && filename.endsWith(".doc") && !filename.startsWith("~");
		}
	};

	/**
	 * 读取配置文件文件进行基本信息配置
	 * 
	 * @param propFilePath
	 *            配置文件名
	 * 
	 * @throws IOException
	 *             读取配置文件失败,则抛出IO异常
	 */
	public Transdoc(String propFilePath) throws IOException {
		propFilePath = propFilePath == null ? DEFAULT_PROPERTIES_FILE_NAME : propFilePath;
		File propFile = new File(propFilePath);
		InputStream input = null;
		if (propFile.exists()) {
			// 读取外部配置文件
			input = new FileInputStream(propFile);
		} else {
			// 读取自带的配置文件
			input = Transdoc.class.getResourceAsStream("/" + DEFAULT_PROPERTIES_FILE_NAME);
		}
		Properties properties = new Properties();
		properties.load(input);
		srcDirPath = properties.getProperty("srcDir");
		dstDirPath = properties.getProperty("dstDir");
		picDirName = properties.getProperty("pictureDirName");
		picFilenamePattern = properties.getProperty("pictureNamePattern");
		input.close();
	}

	/**
	 * 扫描配置文件中设置的源目录,解析并转换特定文档文件的内容,输出目录同样在源目录中,以文档名称命名
	 * 
	 * @throws IOException
	 */
	public void convertToMD() throws IOException {
		System.out.println("========== Transdoc ==========");
		File srcDir = new File(srcDirPath);
		String canonicalPath = srcDir.getCanonicalPath();
		if (!srcDir.exists()) {
			System.err.println(canonicalPath + "目录不存在,程序已结束");
		} else if (srcDir.isDirectory()) {
			File[] listFiles = srcDir.listFiles(docFileFilter);
			if (listFiles.length == 0) {
				System.out.println(canonicalPath + "目录中没有可转换的文件,程序已结束");
				return;
			}
			for (final File file : listFiles) {
				// 开启线程处理文件
				new Thread() {
					@Override
					public void run() {
						doConvertToMD(file);
					}
				}.start();
			}
		}
	}

	/**
	 * 转换函数,从word中提取内容并转换为MD.该函数应提取为单独对象的函数
	 * 
	 * @param file
	 *            doc文档文件
	 */
	private void doConvertToMD(File file) {
		// 开始
		String filename = file.getName();
		try {
			// 创建目录和文件
			String docName = filename.substring(0, filename.lastIndexOf("."));
			File dstDir = new File(dstDirPath, docName);
			dstDir.mkdirs();
			File mdFile = new File(dstDir, docName + ".md");
			File picDir = new File(dstDir, picDirName);

			// 解析并获取doc解析的通用数据对象
			DocParser docParser = new DocParser(file);
			docParser.extractPicturesToFiles(picDir, picFilenamePattern);
			DocContent docContent = docParser.getDocContent();

			// 将通用数据对象内容转换为md格式
			MarkdownConverter mdConvertor = new MarkdownConverter(docContent);
			String mdString = mdConvertor.toMdString();

			// 输出到文件
			PrintWriter pw = new PrintWriter(mdFile, "UTF-8");
			pw.print(mdString);
			pw.flush();
			pw.close();

			// 结束
			System.out.println(filename + " - 转换成功!!");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(filename + " - 转换失败!!");
		}
	}

	/**
	 * 程序入口
	 * 
	 * @param args
	 *            运行参数,暂未使用
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String propFilePath = args.length > 0 && args[0] != null ? args[0] : null;
		Transdoc transdoc = new Transdoc(propFilePath);
		transdoc.convertToMD();
	}

}
