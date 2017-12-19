package com.transdoc;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.transdoc.DocTransformer;

/**
 * DocTransformerTest
 *
 * @author Verils
 * @date 2017-10-20
 */
public class DocTransformerTest {

	private DocTransformer transformer;

	@Before
	public void setUp() throws Exception {
		transformer = new DocTransformer("./docs");
	}

	@Test
	@Ignore
	public void testToMardkown() {
		transformer.toMardkown(null);
	}

}
