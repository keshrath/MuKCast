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

package at.mukprojects.mukcast.client;

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
public class MuKCastServerHandler implements Runnable, Stoppable {

    private static final Logger logger = LoggerFactory.getLogger(MuKCastServerHandler.class);

    private MuKCastClient client;
    private Socket socket;

    private InputStream inputStream;
    private OutputStream outputStream;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;

    private AtomicBoolean running;

    /**
     * Constructs a new MuKCastServerHandler
     * 
     * @param client
     *            The MuKCastClient.
     * @param socket
     *            The socket.
     */
    public MuKCastServerHandler(MuKCastClient client, Socket socket) {
	this.client = client;
	this.socket = socket;
	this.running = new AtomicBoolean(true);
    }

    @Override
    public void run() {
	logger.info("Handler has started to listen for messages.");

	try {
	    outputStream = socket.getOutputStream();
	    inputStream = socket.getInputStream();
	    objectOutputStream = new ObjectOutputStream(outputStream);
	    objectInputStream = new ObjectInputStream(inputStream);

	    while (running.get()) {
		Object message = objectInputStream.readUnshared();

		if (message instanceof Message) {
		    if (message instanceof DisconnectMessage) {
			logger.info("Client got disconnected by the server.");
			stop();
		    } else {
			client.handleMessage((Message) message);
		    }
		}
	    }

	} catch (IOException | ClassNotFoundException e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    try {
		running.set(false);
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
     * Checks if the client is connected to a server.
     * 
     * @return Returns true or false depending on whether the client is
     *         connected or not.
     */
    public boolean isConnected() {
	return running.get();
    }

    /**
     * Sends a message to the server.
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

    @Override
    public void stop() {
	running.set(false);
    }

}
