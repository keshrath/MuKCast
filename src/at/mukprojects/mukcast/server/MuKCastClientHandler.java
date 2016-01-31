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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.mukprojects.mukcast.concurrent.Stoppable;
import at.mukprojects.mukcast.message.DisconnectMessage;
import at.mukprojects.mukcast.message.Message;

/**
 * This class handles the incoming and outgoing messages of the client server
 * connection.
 * <p>
 * It has implemented the interfaces {@link Runnable} and {@link Stoppable} and
 * is designed as a thread.
 * </p>
 * 
 * @author Mathias Markl
 */
public class MuKCastClientHandler implements Runnable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(MuKCastClientHandler.class);

    private MuKCastServer server;
    private Socket socket;
    private String clientKey;

    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private AtomicBoolean running;

    /**
     * Constructs a new MuKCastClientHandler.
     * 
     * @param server
     *            The MuKCastServer.
     * @param socket
     *            The client socket.
     * @param clientKey
     *            The client key.
     */
    public MuKCastClientHandler(MuKCastServer server, Socket socket, String clientKey) {
	this.server = server;
	this.socket = socket;
	this.clientKey = clientKey;
	this.running = new AtomicBoolean(true);
    }

    @Override
    public void run() {
	logger.info("Handler has started to listen for messages.");

	try {
	    inputStream = socket.getInputStream();
	    outputStream = socket.getOutputStream();
	    objectInputStream = new ObjectInputStream(inputStream);
	    objectOutputStream = new ObjectOutputStream(outputStream);

	    while (running.get()) {
		Object message = objectInputStream.readUnshared();

		if (message instanceof Message) {
		    if (message instanceof DisconnectMessage) {
			logger.info("Client gets disconnected.");
			stop();
		    } else {
			server.handleMessage(clientKey, (Message) message);
		    }
		}
	    }

	} catch (IOException | ClassNotFoundException e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    try {
		running.set(false);
		server.disconnectClientWithoutNotification(clientKey);

		if (objectInputStream != null) {
		    objectInputStream.close();
		}
		if (objectOutputStream != null) {
		    objectOutputStream.close();
		}
		if (inputStream != null) {
		    inputStream.close();
		}
		if (outputStream != null) {
		    outputStream.close();
		}
		if (socket != null) {
		    socket.close();
		}
	    } catch (IOException e) {
		logger.error(e.getMessage(), e);
	    }
	}

	logger.info("Handler has stopped to listen.");
    }

    /**
     * Sends a message to the client.
     * 
     * @param message
     *            The message.
     * @throws IOException
     *             If the message couldn't be send an {@code IOExction} is
     *             thrown.
     */
    public void sendMessage(Message message) throws IOException {
	if (isConnected() && objectOutputStream != null) {
	    objectOutputStream.writeUnshared(message);
	    objectOutputStream.reset();
	} else {
	    throw new IOException("Client is not connected!");
	}
    }

    /**
     * Checks if the client is connected to the server.
     * 
     * @return Returns true or false depending on whether the client is
     *         connected or not.
     */
    public boolean isConnected() {
	return running.get();
    }

    @Override
    public void stop() {
	running.set(false);
    }
}
