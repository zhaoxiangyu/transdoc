package com.transdoc.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * InfoFormatter
 *
 * @author Verils
 * @date 2017-10-20
 */
public class InfoFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		return record.getMessage() + "\n";
	}
}
