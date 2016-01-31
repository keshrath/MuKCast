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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.slf4j.LoggerFactory;

import at.mukprojects.mukcast.client.MuKCastClient;
import at.mukprojects.mukcast.concurrent.Stoppable;
import at.mukprojects.mukcast.message.DisconnectMessage;
import at.mukprojects.mukcast.message.Message;
import processing.core.PApplet;

/**
 * This class represents the {@code MuKCastServer}. The server manages the client
 * connections and has methods to send and receive message.
 * 
 * @see MuKCastClient
 * 
 * @author Mathias Markl
 */
public class MuKCastServer {

    /**
     * Logger Settings
     */
    private static final String PATTERN = "%5p (%C{1}) - %m%n";
    private static final String DEFAULT_APPENDER = "DefaultConsoleAppender";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MuKCastServer.class);

    /**
     * Default server port
     */
    public static final int DEFAULT_PORT = 4242;

    private PApplet parent;
    private Method handleMessage;
    private Method handleClientConnection;
    private Method handleClientDisconnect;

    private String serverInfo;
    private ServerSocket serverSocket;
    private String host;
    private int port;

    private ExecutorService executor;
    private List<Stoppable> threads;

    private ConcurrentHashMap<String, MuKCastClientHandler> clients;

    private boolean started;

    /**
     * Constructs a new MuKCastServer.
     * 
     * @param parent
     *            The PApplet.
     * @throws IOException
     *             If the server can't be created an {@code IOException} is
     *             thrown.
     */
    public MuKCastServer(PApplet parent) {
	this(parent, DEFAULT_PORT, null);
    }

    /**
     * Constructs a new MuKCastServer.
     * 
     * @param parent
     *            The PApplet.
     * @param port
     *            The server port.
     * @throws IOException
     *             If the server can't be created an {@code IOException} is
     *             thrown.
     */
    public MuKCastServer(PApplet parent, int port) {
	this(parent, port, null);
    }

    /**
     * Constructs a new MuKCastServer.
     * 
     * @param parent
     *            The PApplet.
     * @param port
     *            The server port.
     * @param host
     *            The server host.
     */
    public MuKCastServer(PApplet parent, int port, String host) {
	this.parent = parent;
	this.host = host;
	this.port = port;
	this.started = false;

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
	 * PApplet methods
	 */
	try {
	    handleMessage = parent.getClass().getMethod("handleMessage", new Class[] { String.class, Message.class });
	} catch (Exception e) {
	    logger.warn("No method called \"handleMessage(String clientKey, Message message)\" could"
		    + " be found in the PApplet class.");
	}

	try {
	    handleClientConnection = parent.getClass().getMethod("handleClientConnection",
		    new Class[] { String.class });
	} catch (Exception e) {
	    logger.warn("No method called \"handleClientConnection(String clientKey)\" could be found in"
		    + " the PApplet class.");
	}

	try {
	    handleClientDisconnect = parent.getClass().getMethod("handleClientDisconnect",
		    new Class[] { String.class });
	} catch (Exception e) {
	    logger.warn("No method called \"handleClientDisconnect(String clientKey)\" could be found in"
		    + " the PApplet class.");
	}

	executor = Executors.newCachedThreadPool();
	threads = new ArrayList<Stoppable>();

	clients = new ConcurrentHashMap<String, MuKCastClientHandler>();
    }

    /**
     * Starts the server.
     * 
     * @throws IOException
     *             If the server can't be created an {@code IOException} is
     *             thrown.
     */
    public void startServer() throws IOException {
	if (started) {
	    logger.warn("Server is already started.");
	} else {
	    serverInfo = InetAddress.getLocalHost().getHostAddress() + ":" + port;

	    if (host == null) {
		serverSocket = new ServerSocket(port);
	    } else {
		serverSocket = new ServerSocket(port, 10, InetAddress.getByName(host));
	    }

	    logger.info("Server starts on port " + serverSocket.getLocalPort() + "...");

	    MuKCastClientListener clientListener = new MuKCastClientListener(this, serverSocket);
	    threads.add(clientListener);
	    executor.execute(clientListener);
	    started = true;

	    logger.info("Server has started.");
	}

    }

    /**
     * Returns the server information as an String.
     * 
     * @return The server information.
     */
    public String getServerInfo() {
	return serverInfo;
    }

    /**
     * Handles a new client connection.
     * 
     * @param socket
     *            The client socket.
     */
    public synchronized void handleClientConnection(Socket socket) {
	logger.info("A new client has connected to the server.");
	logger.info("Client: " + socket.getInetAddress().getHostAddress());

	String clientKey = "#1_" + socket.getInetAddress().getHostAddress();
	if (clients.containsKey(clientKey)) {
	    int index = 2;
	    boolean contains = true;
	    while (contains) {
		clientKey = "#" + index + "_" + socket.getInetAddress().getHostAddress();
		if (clients.containsKey(clientKey)) {
		    index++;
		} else {
		    contains = false;
		}
	    }
	}

	MuKCastClientHandler clientHandler = new MuKCastClientHandler(this, socket, clientKey);

	threads.add(clientHandler);
	executor.execute(clientHandler);

	if (handleClientConnection != null) {
	    try {
		handleClientConnection.invoke(parent, clientKey);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		logger.error(e.getMessage(), e);
	    }
	}

	clients.put(clientKey, clientHandler);
    }

    /**
     * Handles an incoming message.
     * 
     * @param message
     *            The incoming message.
     */
    public synchronized void handleMessage(String clientKey, Message message) {
	if (handleMessage != null) {
	    try {
		handleMessage.invoke(parent, clientKey, message);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Sends an message to the client.
     * 
     * @param client
     *            The client.
     * @param message
     *            The message.
     * @throws IOException
     *             If the message couldn't be send an {@code IOExction} is
     *             thrown.
     */
    public synchronized void sendMessage(String client, Message message) throws IOException {
	MuKCastClientHandler clientHandler = clients.get(client);
	if (clientHandler != null) {
	    clientHandler.sendMessage(message);
	} else {
	    logger.error("Client: (" + client + ") doesn't exist.");
	}
    }

    /**
     * Sends an message to all clients.
     * 
     * @param message
     *            The message.
     * @throws IOException
     *             If the message couldn't be send an {@code IOExction} is
     *             thrown.
     */
    public synchronized void broadcastMessage(Message message) throws IOException {
	Enumeration<MuKCastClientHandler> clientList = clients.elements();
	while (clientList.hasMoreElements()) {
	    MuKCastClientHandler client = clientList.nextElement();
	    if (client != null && client.isConnected()) {
		client.sendMessage(message);
	    }
	}
    }

    /**
     * Returns a list of all connected clients.
     * 
     * @return A list of all connected clients.
     */
    public synchronized Enumeration<String> getClientList() {
	return clients.keys();
    }

    /**
     * Disconnects the client from the server.
     * 
     * @param client
     *            The client.
     * @throws IOException
     *             If the client couldn't be disconnected from the server an
     *             {@code IOExction} is thrown.
     */
    public synchronized void disconnectClient(String client) throws IOException {
	logger.info("Disconnect client: " + client);

	clients.remove(client);

	MuKCastClientHandler clientHandler = clients.get(client);
	if (clientHandler != null) {
	    clientHandler.sendMessage(new DisconnectMessage());
	    clientHandler.stop();
	}

	if (handleClientDisconnect != null) {
	    try {
		handleClientDisconnect.invoke(parent, client);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Disconnects the client from the server. The client won't receive an
     * message.
     * 
     * @param client
     *            The client.
     */
    public synchronized void disconnectClientWithoutNotification(String client) {
	logger.info("Disconnect client: " + client);

	clients.remove(client);

	MuKCastClientHandler clientHandler = clients.get(client);
	if (clientHandler != null) {
	    clientHandler.stop();
	}

	if (handleClientDisconnect != null) {
	    try {
		handleClientDisconnect.invoke(parent, client);
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    /**
     * Disconnects all clients from the server.
     * 
     * @throws IOException
     *             If the clients couldn't be disconnected from the server an
     *             {@code IOExction} is thrown.
     */
    public synchronized void disconnectAllClient() throws IOException {
	logger.info("Disconnect all clients.");

	Enumeration<String> clientList = clients.keys();
	while (clientList.hasMoreElements()) {
	    disconnectClient(clientList.nextElement());
	}
    }

    /**
     * Stops the server: internal use only.
     */
    public void dispose() {
	logger.info("Server is disposed.");
	
	try {
	    stopServer();
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    /**
     * Stops the server.
     * 
     * @throws IOException
     *             If the client couldn't be disconnected from the server an
     *             {@code IOExction} is thrown.
     */
    public void stopServer() throws IOException {
	logger.info("Server is stopping...");

	disconnectAllClient();

	for (Stoppable thread : threads) {
	    thread.stop();
	}
	threads.clear();
	executor.shutdown();
	started = false;

	logger.info("Server has stopped.");
    }
}
