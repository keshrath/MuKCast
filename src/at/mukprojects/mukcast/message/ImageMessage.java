/**
 * This code is copyright (c) Mathias Markl 2016
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.mukprojects.mukcast.message;

import processing.core.PImage;

/**
 * ImageMessage represents a message which contains a {@link PImage}.
 * 
 * @see Message
 * 
 * @author Mathias Markl
 */
public class ImageMessage implements Message {

    private static final long serialVersionUID = -4187341013212556291L;

    private int width;
    private int height;
    private int[] pixels;

    /**
     * Constructs a new ImageMessage.
     * 
     * @param image
     *            The image.
     */
    public ImageMessage(PImage image) {
	if (!image.isLoaded()) {
	    image.loadPixels();
	}

	this.width = image.width;
	this.height = image.height;
	this.pixels = image.pixels;
    }

    /**
     * Returns the image width.
     * 
     * @return The image width.
     */
    public int getWidth() {
	return width;
    }

    /**
     * Returns the image height.
     * 
     * @return The image height.
     */
    public int getHeight() {
	return height;
    }

    /**
     * Returns the image pixel array.
     * 
     * @return The image pixel array.
     */
    public int[] getPixels() {
	return pixels;
    }

    /**
     * Returns the image data as a PImage.
     * 
     * @return The image.
     */
    public PImage getImage(PImage img) {
	if (img != null) {

	    img.loadPixels();
	    
	    if (width != img.width || height != img.height) {
		img.resize(width, height);
	    }
	    
	    for(int i = 0; i < img.pixels.length; i++) {
		img.pixels[i] = this.pixels[i];
	    }
	    
	    img.updatePixels();

	}
	return img;
    }
}
