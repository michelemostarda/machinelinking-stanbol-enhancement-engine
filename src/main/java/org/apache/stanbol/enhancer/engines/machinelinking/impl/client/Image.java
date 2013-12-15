package org.apache.stanbol.enhancer.engines.machinelinking.impl.client;

import java.net.URL;

/**
 * Defines an image associated to a resource.
 *
 * @author Michele Mostarda (michele@machinelinking.com)
 */
public class Image {

    private final URL image;
    private final URL thumb;

    public Image(URL image, URL thumb) {
        this.image = image;
        this.thumb = thumb;
    }

    public URL getImage() {
        return image;
    }

    public URL getThumb() {
        return thumb;
    }

    @Override
    public String toString() {
        return String.format("image: %s, thumb: %s", image, thumb);
    }

}
