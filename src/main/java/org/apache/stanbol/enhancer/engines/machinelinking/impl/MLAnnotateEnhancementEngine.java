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

import com.machinelinking.api.client.APIClient;
import com.machinelinking.api.client.AnnotationResponse;
import com.machinelinking.api.client.Clazz;
import com.machinelinking.api.client.Keyword;
import com.machinelinking.api.client.NGram;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.stanboltools.offline.OnlineMode;
import org.apache.stanbol.enhancer.engines.machinelinking.MLConstants;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_RELATION;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_CONFIDENCE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_END;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_LABEL;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_REFERENCE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_TYPE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTED_TEXT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTION_CONTEXT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_START;

/**
 * {@link MLAnnotateEnhancementEngine} provides functionality to
 * enhance a document using the <i>MachineLinking</i> <b>/annotate</b> REST endpoint.
 */
@Component(
        metatype = true,
        immediate = true,
        label = "%stanbol.MLAnnotateEnhancementEngine.name",
        description = "%stanbol.MLAnnotateEnhancementEngine.description"
)
@Service
@Properties(value = {
        @Property(name = EnhancementEngine.PROPERTY_NAME, value = "machinelinkingAnnotate"),
        @Property(name = MLConstants.APP_ID),
        @Property(name = MLConstants.APP_KEY),
        @Property(name = MLConstants.CONNECTION_TIMEOUT, intValue = MLConstants.DEFAULT_CONNECTION_TIMEOUT)
})
public class MLAnnotateEnhancementEngine extends
        AbstractEnhancementEngine<IOException, RuntimeException> implements EnhancementEngine, ServiceProperties {

    /**
     * Ensures this engine is deactivated in {@link org.apache.stanbol.commons.stanboltools.offline.OfflineMode}
     */
    @SuppressWarnings("unused")
    @Reference
    private OnlineMode onlineMode;

    /**
     * The default value for the Execution of this Engine.
     */
    public static final Integer defaultOrder = ORDERING_CONTENT_EXTRACTION - 27;

    /**
     * Internal literal factory.
     */
    private static final LiteralFactory literalFactory = LiteralFactory.getInstance();

    /**
     * holds the logger.
     */
    private static final Logger log = LoggerFactory.getLogger(MLAnnotateEnhancementEngine.class);

    /**
	 * This contains the only MIME type directly supported by this enhancement
	 * engine.
	 */
	private static final String TEXT_PLAIN_MIMETYPE = "text/plain";

    /**
   	 * Set containing the only supported mime type {@link #TEXT_PLAIN_MIMETYPE}
   	 */
   	private static final Set<String> SUPPORTED_MIMTYPES = Collections.singleton(TEXT_PLAIN_MIMETYPE);

    /**
     * Internal MachineLinking client.
     */
    private APIClient client;

    /**
     * Default constructor used by OSGI. Expects {@link #activate(org.osgi.service.component.ComponentContext)}
     * to be called before the instance is used.
     */
    public MLAnnotateEnhancementEngine() {}

    /**
     * Initialize all parameters from the configuration panel, or with their
     * default values
     *
     * @param ctx the {@link org.osgi.service.component.ComponentContext}
     */
    @SuppressWarnings("unchecked")
    protected void activate(ComponentContext ctx) throws ConfigurationException, IOException {
        super.activate(ctx);
        Dictionary<String, Object> properties = ctx.getProperties();
        final String appId = Util.getPropertyOrFail(this.getClass(), properties, MLConstants.APP_ID);
        final String appKey = Util.getPropertyOrFail(this.getClass(), properties, MLConstants.APP_KEY);
        final int connTimeout;
        try {
            connTimeout = Util.getPropertyOrFail(
                    this.getClass(), properties, MLConstants.CONNECTION_TIMEOUT, Integer.class
            );
        } catch (NumberFormatException nfe) {
            throw new ConfigurationException(
                    MLConstants.CONNECTION_TIMEOUT,
                    "Connection timeout must be an integer.",
                    nfe
            );
        }
        this.client = new APIClient(appId, appKey, connTimeout);
    }

    /**
     * Check if the content can be enhanced
     *
     * @param ci the {@link org.apache.stanbol.enhancer.servicesapi.ContentItem}
     */
    public int canEnhance(ContentItem ci) throws EngineException {
        if (ContentItemHelper.getBlob(ci, SUPPORTED_MIMTYPES) != null) {
      			return ENHANCE_ASYNC;
      		} else {
      			return CANNOT_ENHANCE;
      		}
    }

    /**
     * Calculate the enhancements by doing a POST request to the MachineLinking
     * endpoint and processing the results.
     *
     * @param ci the {@link org.apache.stanbol.enhancer.servicesapi.ContentItem}
     */
    public void computeEnhancements(ContentItem ci) throws EngineException {
        final String text;
        try {
            text = Util.getInputText(ci);
        } catch (Exception e) {
            throw new EngineException(this, ci, e);
        }

        final AnnotationResponse annotation;
		try {
            annotation = this.client.annotate(text);
		} catch (Exception e) {
		    throw new EngineException(
                    "Error while calling the MachineLinking language annotation service.",
                    e
            );
        }

        MGraph g = ci.getMetadata();
        ci.getLock().writeLock().lock();
        try {
            createStatements(ci, annotation, text, g);
        } finally {
            ci.getLock().writeLock().unlock();
        }
    }


    /**
     * This generates annotation statements for the entities detected within the annotation.
     * For each entity a TextAnnotation and an EntityAnnotation are created. An EntityAnnotation
     * can relate to several TextAnnotations.
     *
     * @param annotation the generated annotation.
     * @param writer the statement writer.
     */
    protected void createStatements(
            ContentItem ci, AnnotationResponse annotation, String text, MGraph writer
    ) {
        final String lang = annotation.getLang();
        final Language textLang = new Language(lang);

        // Text annotation.
        final UriRef textAnnotation = EnhancementEngineHelper.createTextEnhancement(ci, this);
        Util.addLanguageProperty(textAnnotation, writer, lang);

        for(Keyword keyword : annotation.getKeywords()) {
            final UriRef entityAnnotation = EnhancementEngineHelper.createEntityEnhancement(ci, this);

            // Single Ngram annotation.
            for (NGram nGram : keyword.getNGrams()) {
                final UriRef ngramTextAnnotation = EnhancementEngineHelper.createTextEnhancement(ci, this);

                writer.add(new TripleImpl(entityAnnotation, DC_RELATION, ngramTextAnnotation));

                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_START,
                        literalFactory.createTypedLiteral(nGram.getStart())
                ));
                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_END,
                        literalFactory.createTypedLiteral(nGram.getEnd())
                ));

                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_SELECTED_TEXT,
                        //new PlainLiteralImpl(text.substring(nGram.getStart(), nGram.getEnd()), textLang)
                        new PlainLiteralImpl(keyword.getForm(), textLang)
                ));
                final String selectionContext = EnhancementEngineHelper.getSelectionContext(
                        text, keyword.getForm(), nGram.getStart()
                );
                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_SELECTION_CONTEXT,
                        new PlainLiteralImpl(
                                selectionContext,
                                textLang
                        )
                ));
            }

            // Entity annotation.
            Literal label = new PlainLiteralImpl(keyword.getForm(), textLang);
            writer.add(new TripleImpl(entityAnnotation, ENHANCER_ENTITY_LABEL, label));
            writer.add(new TripleImpl(
                    entityAnnotation,
                    ENHANCER_ENTITY_REFERENCE,
                    new UriRef(MLConstants.DBPEDIA_PREFIX + keyword.getSensePage())
            ));
            for (Clazz clazz : keyword.getClasses()){
                UriRef annotationType = new UriRef(clazz.getUrl().toExternalForm());
                writer.add(new TripleImpl(entityAnnotation, ENHANCER_ENTITY_TYPE, annotationType));
            }
            writer.add(new TripleImpl(
                    entityAnnotation,
                    ENHANCER_CONFIDENCE,
                    literalFactory.createTypedLiteral(
                            normalizeProbability((double) keyword.getSenseProbability())
                    )
            ));
        }
    }

    @Override
   	public Map<String, Object> getServiceProperties() {
   		return Collections.unmodifiableMap(Collections.singletonMap(ENHANCEMENT_ENGINE_ORDERING, (Object) defaultOrder));
   	}

    private double normalizeProbability(double prob) {
        if(prob < 0) return 0;
        if(prob > 1) return 1;
        return prob;
    }

}
