package com.transdoc.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PropertiesUtils
 *
 * @author Verils
 * @date 2017-10-20
 */
public class PropertiesUtils {

	public static Properties load(String path) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(path));
		return properties;
	}

	public static Properties load(InputStream input) throws IOException {
		Properties properties = new Properties();
		properties.load(input);
		return properties;
	}
}
