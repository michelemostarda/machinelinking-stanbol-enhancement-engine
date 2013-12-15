package org.apache.stanbol.enhancer.engines.machinelinking.impl;

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

