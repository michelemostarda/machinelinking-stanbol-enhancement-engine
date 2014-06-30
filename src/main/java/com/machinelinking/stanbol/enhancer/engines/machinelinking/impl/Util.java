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

import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_LANGUAGE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.DC_TYPE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.Properties.ENHANCER_CONFIDENCE;
import static org.apache.stanbol.enhancer.servicesapi.rdf.TechnicalClasses.DCTERMS_LINGUISTIC_SYSTEM;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.stanbol.enhancer.servicesapi.Blob;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.helper.ContentItemHelper;
import org.osgi.service.cm.ConfigurationException;

import com.machinelinking.stanbol.enhancer.engines.machinelinking.MLConstants;

/**
 * General utility methods.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Util {

    /**
	 * This contains the only MIME type directly supported by this enhancement
	 * engine.
	 */
	private static final String TEXT_PLAIN_MIMETYPE = "text/plain";

    /**
	 * The literal factory
	 */
    private static final LiteralFactory literalFactory = LiteralFactory.getInstance();

	/**
	 * Set containing the only supported mime type {@link #TEXT_PLAIN_MIMETYPE}
	 */
	private static final Set<String> SUPPORTED_MIMTYPES = Collections.singleton(TEXT_PLAIN_MIMETYPE);

    private Util(){}

    /**
     * Gets the property from a dictionary casting value to the provided type.
     *
     * @param origin
     * @param properties
     * @param propertyName
     * @param type
     * @param <T>
     * @return property value.
     * @throws ConfigurationException
     */
    public static  <T> T getPropertyOrFail(
            Class origin, Dictionary<String, Object> properties, String propertyName, Class<T> type
    ) throws ConfigurationException {
        final T propertyValue = type.cast(properties.get(propertyName));
        if (propertyValue == null || (propertyValue instanceof String && ((String)propertyValue).isEmpty())) {
            throw new ConfigurationException(propertyName,
                    String.format("%s : please configure the property [%s] for the MachineLinking Service.",
                            origin.getSimpleName(), propertyName)
            );
        }
        return propertyValue;
    }

    /**
     * Gets the property from a dictionary casting value to the provided type.
     *
     * @param origin
     * @param properties
     * @param propertyName
     * @return property value.
     * @throws ConfigurationException
     */
    public static String getPropertyOrFail(Class origin, Dictionary<String, Object> properties, String propertyName)
    throws ConfigurationException {
        return getPropertyOrFail(origin, properties, propertyName, String.class);
    }

    /**
     * Extracts text from the content item.
     *
     * @param ci
     * @return
     * @throws IOException
     */
    public static String getInputText(ContentItem ci) throws IOException {
        Map.Entry<UriRef, Blob> contentPart = ContentItemHelper.getBlob(ci, SUPPORTED_MIMTYPES);
        if (contentPart == null) {
            throw new IllegalStateException("No ContentPart with Mimetype '" + TEXT_PLAIN_MIMETYPE
                    + "' found for ContentItem "
                    + ci.getUri() + ": This is also checked in the canEnhance method! -> This "
                    + "indicated an Bug in the implementation of the " + "EnhancementJobManager!");
        }

        final String text = ContentItemHelper.getText(contentPart.getValue());
        if (text.trim().length() == 0) {
            throw new IllegalArgumentException(
                    "No text contained in ContentPart {" + contentPart.getKey() +
                    "} of ContentItem {" +
                    ci.getUri() + "}"
            );
        }
        return text;
    }

    /**
     * Cheks whether the mimetype can be enhanced.
     *
     * @param ci
     * @return the detected enhancement support.
     */
    public static int supportedEnhancementType(ContentItem ci) {
        if (ContentItemHelper.getBlob(ci, SUPPORTED_MIMTYPES) != null) {
            return EnhancementEngine.ENHANCE_ASYNC;
        } else {
            return EnhancementEngine.CANNOT_ENHANCE;
        }
    }

    /**
     * Adds language specific properties.
     *
     * @param entityAnnotation
     * @param g
     * @param lang
     */
    public static void addLanguageProperty(UriRef entityAnnotation, MGraph g, String lang) {
        g.add(new TripleImpl(entityAnnotation, DC_LANGUAGE, new PlainLiteralImpl(lang)));
        g.add(new TripleImpl(entityAnnotation, ENHANCER_CONFIDENCE, literalFactory.createTypedLiteral(1.0)));
        g.add(new TripleImpl(entityAnnotation, DC_TYPE, DCTERMS_LINGUISTIC_SYSTEM));
    }

    /**
     * Parses a request option from the configuration parameters. <p>
     * <b>IMPORTANT</b>This method assumes that the parsed confParam is
     * '<code>ml.{parameter}</code> See {@link MLConstants} for defined
     * constants (e.g. {@link MLConstants#CATEGORY}).<p>
     * 
     * Supported configuration values are <ul>
     * <li><code>true/false</code> ({@link Boolean})
     * <li><code>0/1</code> ({@link Number})
     * <li><code>"0"/"1"</code> and <code>"true"/"false"</code> ({@link String})
     * </ul>
     * 
     * If the requested confParam is not present in the configuration no request
     * parameter is added to the request options
     * 
     * @param conf The configuration
     * @param confParam The configuration parameter - '<code>ml.{parameter}</code>' 
     * where <code>{parameter}</code> is one of the 
     * <a href="http://www.machinelinking.com/wp/documentation/text-annotation/#parameters"> 
     * parameters </a> supported by Machinelinking
     * @param requestOptions the request options map to add the parsed '<code>{parameter}</code>' value
     * @see MLConstants
     * @see <a href="http://www.machinelinking.com/wp/documentation/text-annotation/#parameters"> 
     * Parameters</a> supported by Machinelinking.
     */
    public static void parseRequestOption(Dictionary<String,Object> conf, String confParam,
            Map<String,Object> requestOptions) {
        Boolean state = getState(conf, confParam);
        if(state != null){
            String reqParam = confParam.substring(3); //remove the 'ml.{option}'
            //NOTE: The ML APIClient converts Boolean to the integer 0/1 used
            //      in the RESTful API
            //requestOptions.put(reqParam, categoryState ? 1 : 0);
            requestOptions.put(reqParam, state);
        } // else  state not set ... do not add request option
    }

    /**
     * Getter for the state of a boolean type property. Supported configuration values are <ul>
     * <li><code>true/false</code> ({@link Boolean})
     * <li><code>0/1</code> ({@link Number})
     * <li><code>"0"/"1"</code> and <code>"true"/"false"</code> ({@link String})
     * </ul>
     * @param conf the configuration
     * @param property the property
     * @return the state or <code>null</code> if the property was not present
     */
    public static Boolean getState(Dictionary<String,Object> conf, String property) {
        Object value = conf.get(property);
        if(value instanceof String){
            String strVal = ((String) value).trim();
            if("0".equals(strVal)){
                return false;
            } else if("1".equals(strVal)){
                return true;
            } else {
                return new Boolean(strVal);
            }
        } else if(value instanceof Number){
            int state = ((Number)value).intValue();
            return state == 1 ? Boolean.TRUE : state == 0 ? Boolean.FALSE : null;
        } else if(value instanceof Boolean){
            return (Boolean)value;
        } else {
            return null;
        }
    }
    /**
     * Parses an Integer property from the parsed configuration and property
     * @param conf the configuration
     * @param property the property
     * @return the value or <code>null</code> if the property was not present
     * @throws ConfigurationException if the property could not be parsed as an {@link Integer}
     */
    public static Integer getIngegerProperty(Dictionary<String,Object> conf, String property) throws ConfigurationException {
        Object value = conf.get(property);
        if(value instanceof Number){
            return ((Number)value).intValue();
        } else if(value != null){
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e){
                throw new ConfigurationException(property, "Unable to parse Ingeger from "
                    + value + "(type: " + value.getClass() + ")!", e);
            }
        } else {
            return null;
        }
    }
}
