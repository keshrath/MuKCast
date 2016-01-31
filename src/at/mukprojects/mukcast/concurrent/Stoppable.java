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

package at.mukprojects.mukcast.concurrent;

/**
 * Classes implementing {@code Stoppable} interface are intended to also
 * implement the {@code Runnable} interface.
 * 
 * The Stoppable interface should be implemented by any class whose instances
 * are intended to be executed by a thread. The class must define a method of no
 * arguments called stop. This method is used to stop the thread.
 * 
 * @see Runnable
 * 
 * @author Mathias Markl
 */
public interface Stoppable {

    /**
     * Stops the thread.
     */
    public void stop();
}
