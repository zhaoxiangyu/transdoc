package com.transdoc;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * DocTransformerTest
 *
 * @author Verils
 * @date 2017-10-20
 */
public class TransformerTest {

	private Transformer transformer;

	@Before
	public void setUp() throws Exception {
		transformer = new Transformer();
	}

	@Test
	@Ignore
	public void testToMardkown() throws IOException {
		File file = new File(".");
		transformer.toMarkdown(file);
	}

}
