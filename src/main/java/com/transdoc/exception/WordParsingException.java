package com.transdoc.exception;

/**
 * WordParsingException
 *
 * @author Verils
 * @date 2017-10-20
 */
public class WordParsingException extends WordTransformingException {

	private static final long serialVersionUID = -532689023986569011L;

	public WordParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WordParsingException(String message) {
		super(message);
	}

	public WordParsingException(Throwable cause) {
		super(cause);
	}

}
