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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Mock implementation of {@link ComponentContext}.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
class MockComponentContext implements ComponentContext {

    protected final Dictionary<String, Object> properties;

    public MockComponentContext() {
        properties = new Hashtable<String, Object>();
    }

    public MockComponentContext(Dictionary<String, Object> properties) {
        this.properties = properties;
    }

    public void disableComponent(String name) {
    }

    public void enableComponent(String name) {
    }

    public BundleContext getBundleContext() {
        throw new UnsupportedOperationException();
    }

    public ComponentInstance getComponentInstance() {
        throw new UnsupportedOperationException();
    }

    public Dictionary<String, Object> getProperties() {
        return properties;
    }

    public ServiceReference getServiceReference() {
        throw new UnsupportedOperationException();
    }

    public Bundle getUsingBundle() {
        throw new UnsupportedOperationException();
    }

    public Object locateService(String name) {
        throw new UnsupportedOperationException();
    }

    public Object locateService(String name, ServiceReference reference) {
        throw new UnsupportedOperationException();
    }

    public Object[] locateServices(String name) {
        throw new UnsupportedOperationException();
    }

}

