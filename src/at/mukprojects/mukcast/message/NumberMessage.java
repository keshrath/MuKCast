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

/**
 * ImageMessage represents a message which contains a number.
 * 
 * @see Message
 * 
 * @author Mathias Markl
 */
public class NumberMessage implements Message {

    private static final long serialVersionUID = 7008194328334575834L;

    private float number;

    /**
     * Constructs a new NumberMessage.
     * 
     * @param number
     *            The number.
     */
    public NumberMessage(float number) {
	this.number = number;
    }

    /**
     * Constructs a new NumberMessage.
     * 
     * @param number
     *            The number.
     */
    public NumberMessage(int number) {
	this.number = number;
    }

    /**
     * Returns the number as a Float.
     * 
     * @param number
     *            The number.
     */
    public float getNumberAsFloat() {
	return number;
    }

    /**
     * Returns the number as an Integer.
     * 
     * @param number
     *            The number.
     */
    public int getNumberAsInteger() {
	return (int) number;
    }
}
