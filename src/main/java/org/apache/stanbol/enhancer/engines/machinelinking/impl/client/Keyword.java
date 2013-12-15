package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Models a keyword from the <b>/annotate</b> service.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Keyword {

    private final String form;

    private final float rel;

    private final String sensePage;
    private final float senseProbability;

    private final String  _abstract;
    private final Clazz[] classes;
    private final Category[] categories;
    private final External[] externals;
    private final Alt[] alts;
    private final Cross[] crosses;
    private final Image[] images;

    private NGram[] nGrams;

    public Keyword(
            String form,
            float rel,
            String sensePage,
            float senseProbability,
            String _abstract,
            Clazz[] classes,
            Category[] categories,
            External[] externals,
            Alt[] alts,
            Cross[] crosses,
            NGram[] nGrams,
            Image[] images
    ) {
        this.form = form;
        this.rel = rel;
        this.sensePage = sensePage;
        this.senseProbability = senseProbability;
        this._abstract = _abstract;
        this.classes = classes;
        this.categories = categories;
        this.alts = alts;
        this.crosses = crosses;
        this.externals = externals;
        this.nGrams = nGrams;
        this.images = images;
    }

    public String getForm() {
        return form;
    }

    public float getRel() {
        return rel;
    }

    public String getSensePage() {
        return sensePage;
    }

    public float getSenseProbability() {
        return senseProbability;
    }

    public String getAbstract() {
        return _abstract;
    }

    public Clazz[] getClasses() {
        return classes;
    }

    public Category[] getCategories() {
        return categories;
    }

    public External[] getExternals() {
        return externals;
    }

    public Alt[] getAlts() {
        return alts;
    }

    public Cross[] getCrosses() {
        return crosses;
    }

    public NGram[] getNGrams() {
        return nGrams;
    }

    public Image[] getImages() {
        return images;
    }

    @Override
    public String toString() {
        return String.format(
                "form: [%s], rel: %f, sense page: [%s], sense probability: %f, " +
                        "images: %s, classes: %s, categories: %s",
                form, rel, sensePage, senseProbability,
                Arrays.toString(images), Arrays.toString(classes), Arrays.toString(categories)
        );
    }

    private URL parseURLorFail(String urlTxt) {
        try {
            return new URL(urlTxt);
        } catch (MalformedURLException murle) {
            throw new IllegalStateException(
                    String.format("Illegal field content in API response. Expected URL, found: [%s]", urlTxt )
            );
        }
    }

}
