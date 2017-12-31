package com.transdoc;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
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
	public void testToMardkown() throws IOException {
		File dir = new File(Transdoc.DEFAULT_DIR);
		File[] files = dir.listFiles(Transdoc.WORD_FILE_FILTER);
		if (files.length > 0) {
			String markdown = transformer.toMarkdown(files[0]);
			System.out.println(markdown);
			assertNotNull(markdown);
		}
	}

}
