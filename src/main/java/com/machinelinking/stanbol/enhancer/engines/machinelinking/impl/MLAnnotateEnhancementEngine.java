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

import com.machinelinking.api.client.APIClient;
import com.machinelinking.api.client.AnnotationResponse;
import com.machinelinking.api.client.Clazz;
import com.machinelinking.api.client.Image;
import com.machinelinking.api.client.Keyword;
import com.machinelinking.api.client.NGram;
import com.machinelinking.api.client.Topic;
import com.machinelinking.stanbol.enhancer.engines.machinelinking.MLConstants;

import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.stanboltools.offline.OnlineMode;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.rdf.NamespaceEnum;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.apache.stanbol.enhancer.servicesapi.rdf.OntologicalClasses.SKOS_CONCEPT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_RELATION;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_TYPE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_CONFIDENCE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_END;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_LABEL;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_REFERENCE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_ENTITY_TYPE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTED_TEXT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_SELECTION_CONTEXT;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_START;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.RDFS_LABEL;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.RDF_TYPE;

/**
 * {@link MLAnnotateEnhancementEngine} provides functionality to
 * enhance a document using the <i>MachineLinking</i> <b>/annotate</b> REST endpoint.
 */
@Component(
        metatype = true,
        immediate = true,
        label = "%stanbol.MLAnnotateEnhancementEngine.name",
        description = "%stanbol.MLAnnotateEnhancementEngine.description",
        policy=ConfigurationPolicy.REQUIRE //APP_ID and APP_KEY are required
)
@Service
@Properties(value = {
        @Property(name = EnhancementEngine.PROPERTY_NAME, value = "machinelinkingAnnotate"),
        @Property(name = MLConstants.APP_ID),
        @Property(name = MLConstants.APP_KEY),
        @Property(name = MLConstants.CONNECTION_TIMEOUT, intValue = MLConstants.DEFAULT_CONNECTION_TIMEOUT),
        @Property(name = MLConstants.TOPIC, boolValue=false),
        @Property(name = MLConstants.INCLUDE_ENTITY_DATA, boolValue=MLConstants.DEFAULT_INCLUDE_ENTITY_DATA_STATE)
})
public class MLAnnotateEnhancementEngine extends
        AbstractEnhancementEngine<IOException, RuntimeException> implements EnhancementEngine, ServiceProperties {

    /**
     * Ensures this engine is deactivated in {@link org.apache.stanbol.commons.stanboltools.offline.OfflineMode}
     */
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
   	 * This is the {@link MLConstants#DBPEDIA_RESOURCE_PREFIX} without the
   	 * '<code>http://</code>' prefix. This is required to construct entity URIs
   	 * for the language specific versions of DBPedia
   	 * @see #createDbpediaResourceURI(String, String)
   	 */
    private static final Object DBPEDIA_RESOURCE_HOST_AND_PATH = 
            MLConstants.DBPEDIA_RESOURCE_PREFIX.substring(7);

    /**
     * Holds special mappings from the <a herf="http://www.airpedia.org/topic.txt">Airpedia topics</a>
     * to DBPedia resources. For all the other the rules as implemented by
     * {@link #createDbpediaTopicUri(Topic)} apply.<p>
     * This assumes the <code>topic.txt</code> version as included in this jar file as resource.
     * @see #createDbpediaTopicUri(Topic)
     */
    private static final Map<String,UriRef> SPECIAL_TOPIC_MAPPINGS;

    static {
        Map<String,UriRef> mappings = new HashMap<String,UriRef>();
        mappings.put("Philosophy/Psychology", new UriRef(MLConstants.DBPEDIA_RESOURCE_PREFIX+"Philosophy"));
        mappings.put("Sex/gossip",new UriRef(MLConstants.DBPEDIA_RESOURCE_PREFIX+"Gossip"));
        mappings.put("Science/technology",new UriRef(MLConstants.DBPEDIA_RESOURCE_PREFIX+"Science"));
        SPECIAL_TOPIC_MAPPINGS = Collections.unmodifiableMap(mappings);
    }
    
    private static final UriRef RDFS_COMMENT = new UriRef(NamespaceEnum.rdfs + "comment");

    private static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
    private static final UriRef FOAF_DEPICTION = new UriRef(FOAF_NS + "depiction");
    private static final UriRef FOAF_THUMBNAIL = new UriRef(FOAF_NS + "thumbnail");

    /**
     * Internal MachineLinking client.
     */
    private APIClient client;

    Map<String,Object> requestOptions;
    
    boolean includeEntityData = MLConstants.DEFAULT_INCLUDE_ENTITY_DATA_STATE;
    
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
    @Activate
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
        requestOptions = new HashMap<String,Object>();
        
        //parse includeEntityData state
        Object value = ctx.getProperties().get(MLConstants.INCLUDE_ENTITY_DATA);
        if(value != null){
            includeEntityData = Boolean.parseBoolean(value.toString());
        }
        //parse supported request parameters
        Util.parseRequestOption(ctx.getProperties(),MLConstants.CATEGORY, requestOptions);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx){
        requestOptions = null;
        client = null;
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
            annotation = this.client.annotate(text, requestOptions);
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
            // Entity annotation.
            log.debug("> keyword '{}'({})",keyword.getForm(), keyword.getSensePage());
            final UriRef entityAnnotation = EnhancementEngineHelper.createEntityEnhancement(ci, this);
            UriRef dcType = null;
            Literal label = new PlainLiteralImpl(keyword.getForm(), textLang);
            log.debug(" - label: {}",label);
            writer.add(new TripleImpl(entityAnnotation, ENHANCER_ENTITY_LABEL, label));
            UriRef dbpediaResource = createDbpediaResourceURI(lang,keyword.getSensePage());
            log.debug(" - dbpedia resource: {}",dbpediaResource);
            writer.add(new TripleImpl(
                    entityAnnotation,
                    ENHANCER_ENTITY_REFERENCE,
                    dbpediaResource
            ));
            if (keyword.getClasses().length > 0) {
                Clazz type = keyword.getClasses()[0];
                log.debug(" - type:", type.getUrl());
                UriRef dbpediaType = createDbpediaTypeUri(type);
                if(dbpediaType != null){
                    log.debug(" - dbpedia type: ", dbpediaType);
                    writer.add(new TripleImpl(entityAnnotation, ENHANCER_ENTITY_TYPE, 
                        dbpediaType));
                    dcType = dbpediaType;
                } else {
                    writer.add(new TripleImpl(entityAnnotation, ENHANCER_ENTITY_TYPE, 
                        new UriRef(type.getUrl().toString())));
                }
            }
            log.debug(" - probability:", keyword.getSenseProbability());
            writer.add(new TripleImpl(
                    entityAnnotation,
                    ENHANCER_CONFIDENCE,
                    literalFactory.createTypedLiteral(
                            normalizeProbability((double) keyword.getSenseProbability())
                    )
            ));
            // Single Ngram annotation.
            for (NGram nGram : keyword.getNGrams()) {
                log.debug(" - NGram [start:{}, end:{}]", nGram.getStart(),nGram.getEnd());
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

                log.debug("   - form: {}",keyword.getForm());
                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_SELECTED_TEXT,
                        //new PlainLiteralImpl(text.substring(nGram.getStart(), nGram.getEnd()), textLang)
                        new PlainLiteralImpl(keyword.getForm(), textLang)
                ));
                
                if(dcType != null){
                    writer.add(new TripleImpl(ngramTextAnnotation, DC_TYPE, dcType));
                }
                
                final String selectionContext = EnhancementEngineHelper.getSelectionContext(
                        text, keyword.getForm(), nGram.getStart()
                );
                log.debug("   - context: {}",selectionContext);
                writer.add(new TripleImpl(
                        ngramTextAnnotation,
                        ENHANCER_SELECTION_CONTEXT,
                        new PlainLiteralImpl( selectionContext, textLang)));
            }
            if(includeEntityData){
                writeEntityInformation(writer,keyword, dbpediaResource,textLang);
            }

        }
        Topic[] topics = annotation.getTopics();
        log.debug("> write {} Topics", topics == null ? 0 : topics.length);
        if(topics != null && topics.length > 0){
            //add fise:TextAnnotation for the topic classifications
            UriRef topicsAnno = EnhancementEngineHelper.createTextEnhancement(ci, this);
            writer.add(new TripleImpl(topicsAnno, DC_TYPE, SKOS_CONCEPT));
            for(Topic topic : topics){
                if(topic != null){
                    log.debug(" - {}[conf:{}]", topic.getUrl(), topic.getProbability());
                    UriRef topicAnno = EnhancementEngineHelper.createTopicEnhancement(ci, this);
                    writer.add(new TripleImpl(topicAnno, ENHANCER_ENTITY_TYPE, SKOS_CONCEPT));
                    writer.add(new TripleImpl(topicAnno, ENHANCER_ENTITY_REFERENCE, 
                        createDbpediaTopicUri(topic)));
                    writer.add(new TripleImpl(topicAnno, ENHANCER_ENTITY_LABEL,
                            new PlainLiteralImpl(topic.getLabel())));
                    writer.add(new TripleImpl(topicAnno, ENHANCER_CONFIDENCE,
                        literalFactory.createTypedLiteral(
                            normalizeProbability((double) topic.getProbability()))));
                    //finally link this TopicAnnotation to the TextAnnotation
                    writer.add(new TripleImpl(topicAnno, DC_RELATION, topicsAnno));
                }
            }
        }
    }

    /**
     * @param type
     * @return
     */
    private UriRef createDbpediaTypeUri(Clazz type) {
        String typeUri = type.getUrl().toString();
        UriRef dbpediaType;
        if(type.getResource().equals("airpedia")){
            //convert Airpedia type to dbpedia type
            String localName = typeUri.substring(MLConstants.AIRPEDIA_CLASS_PREFIX.length());
            dbpediaType = new UriRef(new StringBuilder(MLConstants.DBPEDIA_ONTOLOGY_PREFIX)
            .append(localName).toString());
            //DBpedia types are also used as dc:type value for the fise:TextAnnotation(s)
        } else { //no Airpedia type ... add the type as parsed
            dbpediaType = null;
        }
        return dbpediaType;
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
    /**
     * Creates language specific entity URIs for DBPedia. Those URIs do follow
     * the following pattern:<ul>
     * <li> for '<code>en</code>': <code>http://dbpedia.org/resource/{sensePage}</code>
     * <li> for all other languages: <code>http://{lang}.dbpedia.org/resource/{sensePage}</code>
     * </ul>
     * @param lang the language. if <code>null</code> English is assumed
     * @param sensePage the sense Page (local name of the entity)
     * @return the {@link UriRef} for the URI of the Entity
     */
    private UriRef createDbpediaResourceURI(String lang, String sensePage){
        if(lang == null || "en".equalsIgnoreCase(lang)){
            return new UriRef(new StringBuilder(MLConstants.DBPEDIA_RESOURCE_PREFIX)
                .append(sensePage).toString());
        } else {
            return new UriRef(new StringBuilder("http://")
                .append(lang.toLowerCase(Locale.ROOT))
                .append(DBPEDIA_RESOURCE_HOST_AND_PATH)
                .append(sensePage).toString());
        }
    }
    /**
     * Maps the Airpedia topic to a DBPedia Resource <p>
     * <i>NOTE</i>: This maps to the DBPedia resource instead of the Category. This
     * because the assumption is that usually the resource in more useful as the
     * category concept.
     * 
     * @param topic the Topic
     * @return the DBPedia resource for the parsed topic
     */
    private UriRef createDbpediaTopicUri(Topic topic) {
        //cut away the namespace (NOTE: this assumes that all topics us the
        //airpedia topic namespace)
        String topicName = topic.getUrl().toString().substring(MLConstants.AIRPEDIA_TOPIC_PREFIX.length());
        //first check for a special mapping
        UriRef topicUri = SPECIAL_TOPIC_MAPPINGS.get(topicName);
        if(topicUri == null){ //normal mapping
            //some union topics do use '/' as separator. For now we simple use the
            //first part of the union as name for the dbpedia category.
            int slashIdx = topicName.indexOf('/');
            if(slashIdx > 0){
                topicName = topicName.substring(0, slashIdx);
            }
            topicName = topicName.replace(' ', '_');
            topicUri = new UriRef(new StringBuilder(MLConstants.DBPEDIA_RESOURCE_PREFIX)
            .append(topicName).toString());
        }
        return topicUri; 
    }
    
    private void writeEntityInformation(MGraph writer, Keyword keyword, UriRef entity, Language lang) {
        //The rdfs:label
        writer.add(new TripleImpl(entity, RDFS_LABEL, 
                new PlainLiteralImpl(keyword.getForm(), lang)));
        //the rdf:type
        for(Clazz type : keyword.getClasses()){
            writer.add(new TripleImpl(entity, RDF_TYPE, new UriRef(type.getUrl().toString())));
            UriRef dbpediaType = createDbpediaTypeUri(type);
            if(dbpediaType != null){
                writer.add(new TripleImpl(entity, RDF_TYPE, dbpediaType));
            }
        }
        if(keyword.getAbstract() != null){
            writer.add(new TripleImpl(entity, RDFS_COMMENT, 
                new PlainLiteralImpl(keyword.getAbstract(),lang)));
        }
        if(keyword.getImages() != null && keyword.getImages().length > 0){
            Image image = keyword.getImages()[0];
//            for(Image image : keyword.getImages()){
            writer.add(new TripleImpl(entity, FOAF_DEPICTION, new UriRef(image.getImage().toString())));
            writer.add(new TripleImpl(entity, FOAF_THUMBNAIL, new UriRef(image.getThumb().toString())));
//            }
        }
    }

}
