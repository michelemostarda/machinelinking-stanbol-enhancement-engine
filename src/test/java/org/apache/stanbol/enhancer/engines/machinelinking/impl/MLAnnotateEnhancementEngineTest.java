/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.enhancer.engines.machinelinking.impl;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.stanbol.enhancer.contentitem.inmemory.InMemoryContentItemFactory;
import org.apache.stanbol.enhancer.engines.machinelinking.MLConstants;
import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.StringSource;
import org.apache.stanbol.enhancer.servicesapi.rdf.Properties;
import org.apache.stanbol.enhancer.test.helper.EnhancementStructureHelper;
import org.apache.stanbol.enhancer.test.helper.RemoteServiceHelper;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import static org.apache.stanbol.enhancer.servicesapi.EnhancementEngine.ENHANCE_ASYNC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class provides a JUnit test for the {@link MLAnnotateEnhancementEngine}.
 * EnhancementEngine.
 * 
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class MLAnnotateEnhancementEngineTest {

	/**
	 * This contains the logger.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(MLAnnotateEnhancementEngineTest.class);

    private static ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();

    private static String TEST_TEXT = "President Obama is meeting Angela Merkel in Berlin on Monday";

    private MLAnnotateEnhancementEngine annotateEngine;

    private ContentItem ci;

    @Before
	public void setUp() throws IOException, ConfigurationException {
        annotateEngine = new MLAnnotateEnhancementEngine();

        ci = ciFactory.createContentItem(new StringSource(TEST_TEXT));
		assertNotNull(ci);
        Entry<UriRef, Blob> textContentPart = ContentItemHelper.getBlob(
                ci, Collections.singleton("text/plain")
        );
		assertNotNull(textContentPart);

        // Activation.
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EnhancementEngine.PROPERTY_NAME, "machinelinkingLangId");
        properties.put(MLConstants.APP_ID, MLTestConstants.APP_ID);
        properties.put(MLConstants.APP_KEY, MLTestConstants.APP_KEY);
        properties.put(MLConstants.CONNECTION_TIMEOUT, 30 * 1000);
        annotateEngine.activate(new MockComponentContext(properties));
	}

    @Test
	public void testCanEnhance() throws EngineException {
		assertEquals(ENHANCE_ASYNC, annotateEngine.canEnhance(ci));
	}

	/**
	 * Validates the Enhancements created by this engine
	 * @throws org.apache.stanbol.enhancer.servicesapi.EngineException
	 */
	@Test
	public void testEnhancement() throws EngineException {
	    try {
	        annotateEngine.computeEnhancements(ci);
	    } catch (EngineException e) {
            RemoteServiceHelper.checkServiceUnavailable(e);
        }
        HashMap<UriRef,Resource> expectedValues = new HashMap<UriRef,Resource>();
        expectedValues.put(
                Properties.ENHANCER_EXTRACTED_FROM,
                ci.getUri()
        );
        expectedValues.put(
                Properties.DC_CREATOR,
                LiteralFactory.getInstance().createTypedLiteral(annotateEngine.getClass().getName())
        );
		EnhancementStructureHelper.validateAllTextAnnotations(ci.getMetadata(), TEST_TEXT, expectedValues);
		EnhancementStructureHelper.validateAllEntityAnnotations(ci.getMetadata(), expectedValues);
	}

}
