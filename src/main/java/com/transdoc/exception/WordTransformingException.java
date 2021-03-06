package com.transdoc.exception;

/**
 * WordTransformingException
 *
 * @author Verils
 * @date 2017-10-20
 */
public class WordTransformingException extends RuntimeException {

	private static final long serialVersionUID = -7103413496472730725L;

	public WordTransformingException(String message) {
		super(message);
	}

	public WordTransformingException(Throwable cause) {
		super(cause);
	}

	public WordTransformingException(String message, Throwable cause) {
		super(message, cause);
	}

}
