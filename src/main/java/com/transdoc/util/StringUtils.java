package com.transdoc.util;

/**
 * StringUtils
 *
 * @author Verils
 * @date 2017-10-20
 */
public class StringUtils {

	private static final char WHITE_SPACE_CHAR = ' ';

	/**
	 * 去除字符尾部的空格和换行
	 * 
	 * @param str
	 *            需要去除尾部空格和换行的字符串
	 * @return 去除尾部空格和换行后的字符串
	 */
	public static String rtrim(String str) {
		int len = str.length();
		char[] val = str.toCharArray();
		while (len > 0 && val[len - 1] <= WHITE_SPACE_CHAR) {
			len--;
		}
		return str.substring(0, len);
	}
}
