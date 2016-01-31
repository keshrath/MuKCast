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

package at.mukprojects.mukcast.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.mukprojects.mukcast.concurrent.Stoppable;

/**
 * This class listens for new connections.
 * <p>
 * It has implemented the interfaces {@link Runnable} and {@link Stoppable} and
 * is designed as a thread.
 * </p>
 * 
 * @author Mathias Markl
 */
public class MuKCastClientListener implements Runnable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(MuKCastClientListener.class);

    private MuKCastServer server;
    private ServerSocket serverSocket;

    private AtomicBoolean running;

    /**
     * Constructs a new MuKCastClientListener.
     * 
     * @param server
     *            The MuKCastServer.
     * @param serverSocket
     *            The server socket.
     */
    public MuKCastClientListener(MuKCastServer server, ServerSocket serverSocket) {
	this.server = server;
	this.serverSocket = serverSocket;
	this.running = new AtomicBoolean(true);
    }

    @Override
    public void run() {
	logger.info("Listener has started to listen for connections.");

	try {
	    while (running.get()) {
		Socket clientSocket = serverSocket.accept();
		server.handleClientConnection(clientSocket);
	    }
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    try {
		running.set(false);
		if (serverSocket != null) {
		    serverSocket.close();
		}
	    } catch (IOException e) {
		logger.error(e.getMessage(), e);
	    }

	}

	logger.info("Listener has stopped to listen.");
    }

    @Override
    public void stop() {
	running.set(false);
    }

}
