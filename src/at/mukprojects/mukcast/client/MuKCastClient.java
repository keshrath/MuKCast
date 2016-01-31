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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;

import at.mukprojects.mukcast.message.DisconnectMessage;
import at.mukprojects.mukcast.message.Message;
import at.mukprojects.mukcast.server.MuKCastServer;
import processing.core.PApplet;

/**
 * This class represents the {@code MuKCastClient}. The client manages the server
 * connection and has methods to send and receive message.
 * 
 * @see MuKCastServer
 * 
 * @author Mathias Markl
 */
public class MuKCastClient {

    /**
     * Logger Settings
     */
    private static final String PATTERN = "%5p (%C{1}) - %m%n";
    private static final String DEFAULT_APPENDER = "DefaultConsoleAppender";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MuKCastClient.class);

    private PApplet parent;
    private Method handleMessage;

    private Socket clientSocket;
    private String clientInfo;

    private Thread clientThread;
    private MuKCastServerHandler serverHandler;

    private String host;
    private int port;

    /**
     * Constructs a new MuKCastClient.
     * 
     * @param parent
     *            The PApplet.
     * @param host
     *            The host can be an IP address or a server name.
     * @param port
     *            The server port.
     * @throws IOException
     *             If the client can't connect to the server an
     *             {@code IOException} is thrown.
     */
    public MuKCastClient(PApplet parent, String host, int port) {
	this.parent = parent;
	this.host = host;
	this.port = port;

	/*
	 * Logger configuration
	 */
	if (!Logger.getRootLogger().getAllAppenders().hasMoreElements()) {
	    Logger.getRootLogger().setLevel(Level.INFO);

	    ConsoleAppender appender = new ConsoleAppender(new PatternLayout(PATTERN));
	    appender.setName(DEFAULT_APPENDER);
	    Logger.getRootLogger().addAppender(appender);
	}

	/*
	 * Register dispose method
	 */
	parent.registerMethod("dispose", this);

	/*
	 * PApplet method
	 */
	try {
	    handleMessage = parent.getClass().getMethod("handleMessage",
		    new Class[] { MuKCastClient.class, Message.class });
	} catch (Exception e) {
	    logger.warn("No method called \"handleMessage(MuKCastClient client, Message message)\" could"
		    + " be found in the PApplet class.");
	}
    }

    /**
     * Connects the client to the server.
     * 
     * @throws IOException
     *             If the client can't be connected an {@code IOException} is
     *             thrown.
     */
    public void connect() throws IOException {
	clientSocket = new Socket(host, port);
	clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getLocalPort();

	serverHandler = new MuKCastServerHandler(this, clientSocket);
	clientThread = new Thread(serverHandler);
	clientThread.start();
    }

    /**
     * Returns the client information as an String.
     * 
     * @return The client information.
     */
    public String getClientInfo() {
	return clientInfo;
    }

    /**
     * Handles an incoming message.
     * 
     * @param message
     *            The incoming message.
     */
    public void handleMessage(Message message) {
	if (handleMessage != null) {
	    try {
		handleMessage.invoke(parent, this, message);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Sends an message to the server.
     * 
     * @param message
     *            The message.
     * @throws IOException
     *             If the message couldn't be send an {@code IOExction} is
     *             thrown.
     */
    public void sendMessage(Message message) throws IOException {
	serverHandler.sendMessage(message);
    }

    /**
     * Disconnects the client: internal use only.
     */
    public void dispose() {
	logger.info("Client is disposed.");
	
	try {
	    disconnect();
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Disconnects the client from the server.
     * 
     * @throws IOException
     *             If the client couldn't be disconnected from the server an
     *             {@code IOExction} is thrown.
     */
    public void disconnect() throws IOException {
	if (serverHandler.isConnected()) {
	    logger.info("Disconnect client: " + clientInfo);
	    serverHandler.sendMessage(new DisconnectMessage());
	    serverHandler.stop();
	} else {
	    logger.info("Client isn't connected to a server.");
	}
    }
}
