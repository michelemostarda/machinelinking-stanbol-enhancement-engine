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
package com.machinelinking.stanbol.enhancer.engines.machinelinking.impl;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.serializer.JenaSerializerProvider;
import org.apache.stanbol.enhancer.contentitem.inmemory.InMemoryContentItemFactory;
import org.apache.stanbol.enhancer.nlp.model.AnalysedText;
import org.apache.stanbol.enhancer.nlp.model.AnalysedTextFactory;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.ContentItemFactory;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.impl.StringSource;
import org.apache.stanbol.enhancer.servicesapi.rdf.Properties;
import org.apache.stanbol.enhancer.test.helper.EnhancementStructureHelper;
import org.apache.stanbol.enhancer.test.helper.RemoteServiceHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinelinking.stanbol.enhancer.engines.machinelinking.MLConstants;
import com.machinelinking.stanbol.enhancer.engines.machinelinking.impl.MLLanguageIdentifierEnhancementEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

/**
 *  Test for {@link MLLanguageIdentifierEnhancementEngine} class.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class MLLanguageIdentifierEnhancementEngineTest {

    /**
     * This contains the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MLAnnotateEnhancementEngineTest.class);

    private static final ContentItemFactory ciFactory = InMemoryContentItemFactory.getInstance();
    private static final AnalysedTextFactory atFactory = AnalysedTextFactory.getDefaultInstance();
    private static JenaSerializerProvider serializer = new JenaSerializerProvider();

    private MLLanguageIdentifierEnhancementEngine engine;

    @Before
    public void activate() throws IOException, ConfigurationException {
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(EnhancementEngine.PROPERTY_NAME, "machinelinkingLangId");
        
        properties.put(MLConstants.APP_ID, System.getProperty(
            MLConstants.APP_ID, MLTestConstants.APP_ID));
        properties.put(MLConstants.APP_KEY, System.getProperty(
            MLConstants.APP_KEY,MLTestConstants.APP_KEY));
        properties.put(MLConstants.CONNECTION_TIMEOUT, 30 * 1000);
        engine = new MLLanguageIdentifierEnhancementEngine();
        engine.activate(new MockComponentContext(properties));
    }

    @After
    public void deactivate() {
        engine.deactivate(null);
        engine = null;
    }

    @Test
    public void testEngine() throws EngineException, IOException {
        final String text = "President Barack Obama pushes Senate for military action in Syria.";
        ContentItem ci = ciFactory.createContentItem(new StringSource(text));
        Assert.assertNotNull(ci);
        AnalysedText at = atFactory.createAnalysedText(ci, ci.getBlob());
        Assert.assertNotNull(at);

        Assert.assertEquals(
                "Cannot enhance Test ContentItem", EnhancementEngine.ENHANCE_ASYNC, engine.canEnhance(ci)
        );

        try {
            engine.computeEnhancements(ci);
        } catch (EngineException e) {
            RemoteServiceHelper.checkServiceUnavailable(e);
        }
        
        logEnhancements(ci);
        
        HashMap<UriRef,Resource> expectedValues = new HashMap<UriRef,Resource>();
        expectedValues.put(Properties.ENHANCER_EXTRACTED_FROM, ci.getUri());
        expectedValues.put(
                Properties.DC_CREATOR,
                LiteralFactory.getInstance().createTypedLiteral(engine.getClass().getName())
        );
        expectedValues.put(Properties.DC_LANGUAGE, LiteralFactory.getInstance().createTypedLiteral("en"));
		EnhancementStructureHelper.validateAllTextAnnotations(
                ci.getMetadata(), text, expectedValues
        );
		EnhancementStructureHelper.validateAllEntityAnnotations(
				ci.getMetadata(), expectedValues
        );
    }
    /**
     * Logs the enhancements as TURTLE on DEBUG level
     * @param ci the contentItem
     */
    private void logEnhancements(ContentItem ci){
        if(LOG.isDebugEnabled()){
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            serializer.serialize(bout, ci.getMetadata(), SupportedFormat.TURTLE);
            LOG.debug("Enhancements of {}",ci.getUri().getUnicodeString());
            LOG.debug(new String(bout.toByteArray(),Charset.forName("UTF8")));
        }
    }

}
