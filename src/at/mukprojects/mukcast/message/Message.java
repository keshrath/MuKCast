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

import java.io.Serializable;

/**
 * Classes implementing {@code Message} interface are intended to have
 * <i>message</i> semantics. <br/>
 * A message is an object that is sent to a remote machine in order to trigger
 * actions or retrieve data.
 * <p>
 * Once a {@code Message} is created, it should not be modifiable anymore. Thus
 * it is recommended but not required that {@code Message}s are immutable and
 * prevent intentional as well as unintentional modifications of their state. *
 * </p>
 * <p>
 * Furthermore, they implement the {@link Serializable} allowing implementations
 * to be serialized.
 * </p>
 * 
 * @author Mathias Markl
 */
public interface Message extends Serializable {
}
