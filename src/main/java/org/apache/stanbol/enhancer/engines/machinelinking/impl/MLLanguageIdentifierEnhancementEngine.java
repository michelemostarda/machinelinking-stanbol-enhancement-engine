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

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.stanbol.commons.stanboltools.offline.OnlineMode;
import org.apache.stanbol.enhancer.engines.machinelinking.MLConstants;
import org.apache.stanbol.enhancer.engines.machinelinking.impl.client.APIClient;
import org.apache.stanbol.enhancer.engines.machinelinking.impl.client.GuessedLanguageResponse;
import org.apache.stanbol.enhancer.servicesapi.ContentItem;
import org.apache.stanbol.enhancer.servicesapi.EngineException;
import org.apache.stanbol.enhancer.servicesapi.EnhancementEngine;
import org.apache.stanbol.enhancer.servicesapi.ServiceProperties;
import org.apache.stanbol.enhancer.servicesapi.helper.EnhancementEngineHelper;
import org.apache.stanbol.enhancer.servicesapi.impl.AbstractEnhancementEngine;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;


/**
 * Implementation of {@link EnhancementEngine} to provide text enhancement based on
 * <i>MachineLinking</i> <b>/lang</b> API.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
@Component(
        immediate = true,
        metatype = true,
        label = "%stanbol.MLLanguageIdentifierEnhancementEngine.name",
        description = "%stanbol.MLLanguageIdentifierEnhancementEngine.description"
)
@Service
@Properties(value = { 
    @Property(name = EnhancementEngine.PROPERTY_NAME, value = "machinelinkingLangId"),
    @Property(name = MLConstants.APP_ID),
    @Property(name = MLConstants.APP_KEY),
    @Property(name = MLConstants.CONNECTION_TIMEOUT, intValue=MLConstants.DEFAULT_CONNECTION_TIMEOUT)
})
public class MLLanguageIdentifierEnhancementEngine extends AbstractEnhancementEngine<IOException, RuntimeException>
implements EnhancementEngine, ServiceProperties {

    /**
     * The default value for the Execution of this Engine. Currently set to
     * {@link org.apache.stanbol.enhancer.servicesapi.ServiceProperties#ORDERING_PRE_PROCESSING}-2 to ensure that it is
     * executed before "normal" pre-processing engines.<p>
     * NOTE: this information is used by the default and weighed {@link org.apache.stanbol.enhancer.servicesapi.Chain}
     * implementation to determine the processing order of
     * {@link org.apache.stanbol.enhancer.servicesapi.EnhancementEngine}s.
     * Other {@link org.apache.stanbol.enhancer.servicesapi.Chain} implementation do not
     * use this information.
     */
	public static final Integer defaultOrder = ServiceProperties.ORDERING_PRE_PROCESSING -2;

    /**
	 * This ensures that no connections to external services are made if Stanbol is started in offline mode
	 * as the OnlineMode service will only be available if OfflineMode is deactivated.
	 */
	@SuppressWarnings("unused")
    @Reference
    private OnlineMode onlineMode;

	private APIClient client;

	@Override
	@Activate
	public void activate(ComponentContext ctx) throws IOException, ConfigurationException {
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
	
	@Override
	@Deactivate
	protected void deactivate(ComponentContext ce) {
		super.deactivate(ce);
	}

	@Override
	public int canEnhance(ContentItem ci) throws EngineException {
		return Util.supportedEnhancementType(ci);
	}
	
	@Override
	public void computeEnhancements(ContentItem ci) throws EngineException {
        final String text;
        try {
            text = Util.getInputText(ci);
        } catch (Exception e) {
            throw new EngineException(this, ci, e);
        }

        final GuessedLanguageResponse guessedLanguage;
		try {
            guessedLanguage = this.client.guessLanguage(text);
		} catch (Exception e) {
		    throw new EngineException(
                    "Error while calling the MachineLinking language identifier service.",
                    e
            );
        }

        MGraph g = ci.getMetadata();
        ci.getLock().writeLock().lock();
        try {
            UriRef textEnhancement = EnhancementEngineHelper.createTextEnhancement(ci, this);
            Util.addLanguageProperty(textEnhancement, g, guessedLanguage.getLang());
        } finally {
            ci.getLock().writeLock().unlock();
        }
    }

	@Override
	public Map<String, Object> getServiceProperties() {
		return Collections.unmodifiableMap(
                Collections.singletonMap(ENHANCEMENT_ENGINE_ORDERING, (Object) defaultOrder)
        );
	}

}