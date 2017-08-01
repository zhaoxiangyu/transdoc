package me.verils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

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

			// 解析并获取基本信息内容
			DocParser docParser = new DocParser(file);
			docParser.extractPicturesToFiles(picDir, picFilenamePattern);
			List<DocParagraph> paragraphs = docParser.listParagraphs();
			Queue<DocTable> tables = docParser.listTables();
			Queue<String> pictures = docParser.listPictures();

			// 将文档信息进行简单组装,获取md格式文本
			StringBuilder mdContent = new StringBuilder();
			for (DocParagraph paragraph : paragraphs) {
				String content = paragraph.getContent();
				System.out.println(content);
				content = content.replace("{h1}", "\n# ");
				content = content.replace("{h2}", "\n## ");
				content = content.replace("{h3}", "\n### ");
				content = content.replace("{h4}", "\n#### ");
				content = content.replace("{h5}", "\n##### ");
				content = content.replace("{h6}", "\n###### ");
				content = "{table}".equals(content) ? toTableMDText(tables.poll()) : content;
				content = "{picture}".equals(content) ? "![](" + pictures.poll() + ")" : content;
				mdContent.append(content).append("\n");
			}

			// 输出到文件
			PrintWriter pw = new PrintWriter(mdFile, "UTF-8");
			pw.print(mdContent.toString().trim());
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
			tableContent.append("\n```\n").append(table.getCell(0, 0)).append("\n```\n");
		} else {
			int rownum = table.getRownum();
			int colnum = table.getColnum();
			tableContent.append("\n");
			for (int i = 0; i < rownum; i++) {
				tableContent.append("|");
				for (int j = 0; j < colnum; j++) {
					String text = table.getCell(i, j).replaceAll("\\n", "<br>");
					tableContent.append(text).append("|");
				}
				if (i == 0) {
					tableContent.append("\n|");
					for (int j = 0; j < colnum; j++) {
						tableContent.append("----|");
					}
				}
				tableContent.append("\n");
			}
		}
		return tableContent.toString();
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
