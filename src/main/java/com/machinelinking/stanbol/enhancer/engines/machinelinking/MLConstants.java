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
package com.machinelinking.stanbol.enhancer.engines.machinelinking;

import com.machinelinking.api.client.ParamsValidator;

/**
 * Set of <i>MachineLinking</i> module properties.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public interface MLConstants {

    /**
     * Application id used to access to the <i>MachineLinking</i> service.
     */
    String APP_ID  = "ml.appid";

    /**
     * Application key used to access to the <i>MachineLinking</i> service.
     */
    String APP_KEY = "ml.appkey";

    /**
     * HTTP connection timeout.
     */
    String CONNECTION_TIMEOUT = "ml.connectionTimeout";

    /**
     * Default HTTP connection timeout (30sec).
     */
    int DEFAULT_CONNECTION_TIMEOUT = 30 * 1000;

    /**
     * DBpedia URL prefix.
     */
    String DBPEDIA_RESOURCE_PREFIX = "http://dbpedia.org/resource/";
    /**
     * DBpedia Category prefix.
     */
    String DBPEDIA_CATEGORY_PREFIX = DBPEDIA_RESOURCE_PREFIX+"Category:";
    /**
     * DBpedia Ontology prefix.
     */
    String DBPEDIA_ONTOLOGY_PREFIX = "http://dbpedia.org/ontology/";

    /**
     * The Airpedia class namespace
     */
    String AIRPEDIA_CLASS_PREFIX = "http://www.airpedia.org/ontology/class/";
    /**
     * The Airpedia topic namespace
     */
    String AIRPEDIA_TOPIC_PREFIX = "http://www.airpedia.org/topic/class/";
    
    /**
     * If additional information about the entity should be added to the 
     * Enhancement results
     */
    String INCLUDE_ENTITY_DATA = "ml.entitydata";
    /**
     * The default for the {@link #INCLUDE_ENTITY_DATA} state (default: <code>false</code>)
     */
    boolean DEFAULT_INCLUDE_ENTITY_DATA_STATE = false;
    
    /**
     * If enabled the Engine will try to create DBPedia resource URIs for the
     * English DBPedia version (even for non English texts). This feature
     * requires {@link #CROSS} to be enabled.
     */
    String PREF_ENGLISH_RESORUCE_URIS = "ml.prefEnglishEntityUris";
    /**
     * By default the {@link #PREF_ENGLISH_RESORUCE_URIS} is disabled
     */
    boolean DEFAUTL_PREF_ENGLISH_RESORUCE_URIS_STATE = false;
    
    
    /**
     * If <code>fise:EntityAnnotation</code> are written for <code>Keyword</code>
     * detected by Machinelinking. If disabled all keyword result specific
     * parameters (e.g. {@link #LINK}, {@link #ABSTRACT}, ...) will be deactivated.<p>
     * Enabling this is useful if one only wants to have {@link #TOPIC}.
     */
    String KEYWORD = "ml.keyword";
    /**
     * By default {@link #KEYWORD} is enabled
     */
    boolean DEFAULT_KEYWORD_STATE = true;
    
    /* REQUEST PARAMETERS */
    /**
     * whether to add external links to identified keywords (optional, requires “disambiguation=1″)
     */
    String LINK = "ml."+ParamsValidator.link;
    /**
     * whether to add alternative terms (e.g., synonyms) to identified keywords (optional, requires “disambiguation=1″)
     */
    String FORM = "ml."+ParamsValidator.form;
    /*
     * NOTE: CROSS is not supported. Its state depends on the PREF_ENGLISH_RESORUCE_URIS state
     */
//    String CROSS = "ml."+ParamsValidator.cross;
    /**
     * whether to add categories from Wikipedia (optional, requires “disambiguation=1″)
     */
    String CATEGORY = "ml."+ParamsValidator.category;
    /**
     * whether to add abstract from Wikipedia (optional, requires “disambiguation=1″)
     */
    String ABSTRACT = "ml."+ParamsValidator._abstract;
    /**
     * whether to add DBpedia/Airpedia type (optional, requires “disambiguation=1″)
     */
    String CLASS = "ml."+ParamsValidator._class;
    /**
     * whether to add DBpedia/Airpedia topic (optional, requires “disambiguation=1″)
     */
    String TOPIC = "ml."+ParamsValidator.topic;
    /**
     * whether to add DBpedia/Airpedia external links (optional, requires “disambiguation=1″)
     */
    String EXTRENAL = "ml."+ParamsValidator.external;
    /**
     * whether to add DBpedia/Airpedia external links (optional, requires “disambiguation=1″)
     */
    String IMAGE = "ml."+ParamsValidator.image;
    
    
}
