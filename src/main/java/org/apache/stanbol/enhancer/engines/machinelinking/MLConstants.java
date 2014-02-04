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
package org.apache.stanbol.enhancer.engines.machinelinking;

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
    public static final String DBPEDIA_PREFIX = "http://dbpedia.org/";

}
