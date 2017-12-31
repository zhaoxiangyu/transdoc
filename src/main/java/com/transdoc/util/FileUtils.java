package com.transdoc.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class FileUtils {

	public static void writeStringToFile(File destFile, String content, String charsetName)
			throws UnsupportedEncodingException, IOException {
		writeByteArrayToFile(destFile, content.getBytes(charsetName));
	}

	public static void writeByteArrayToFile(File destFile, byte[] content) throws IOException {
		FileOutputStream fos = new FileOutputStream(destFile);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		bos.write(content);
		bos.close();
		closeQuietly(fos);
	}

	public static void closeQuietly(Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (IOException e) {
		}
	}
}
