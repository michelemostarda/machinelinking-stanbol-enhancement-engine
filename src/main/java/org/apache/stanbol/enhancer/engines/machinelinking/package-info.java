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

/**
 * This package defines the enhancement engines implemented on the
 * <a href="http://www.machinelinking.com/">Machine Linking API</a>.
 * Two {@link org.apache.stanbol.enhancer.servicesapi.EnhancementEngine}s has been implemented:
 * <ul>
 *     <li>
 *         {@link org.apache.stanbol.enhancer.engines.machinelinking.impl.MLLanguageIdentifierEnhancementEngine}
 *     which provides <a href="http://www.machinelinking.com/wp/documentation/language-recognition/">language recognition</a> features.
 *     </li>
 *     <li>
 *         {@link org.apache.stanbol.enhancer.engines.machinelinking.impl.MLAnnotateEnhancementEngine}
 *     which provides <a href="http://www.machinelinking.com/wp/documentation/text-annotation/">text annotation</a> features.
 *     </li>
 * </ul>
 */
package org.apache.stanbol.enhancer.engines.machinelinking;