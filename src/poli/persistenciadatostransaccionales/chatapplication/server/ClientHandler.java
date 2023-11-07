package poli.persistenciadatostransaccionales.chatapplication.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * This class is responsible for handling communication with an individual
 * client.
 */
public class ClientHandler implements Runnable {
	/**
	 * Socket to manage the client's connection. This socket represents the connection to the client.
	 */
	private final Socket clientSocket;

	/**
	 * The username of the client. This is set after the client successfully connects and authenticates.
	 */
	private String username;

	/**
	 * The username of the other client with whom this client is currently chatting.
	 * If null, it means the client is not currently engaged in a private chat.
	 */
	private String currentChatWithUser = null;

	/**
	 * A thread-safe map of active client handlers, keyed by username.
	 * This allows the server to access the handler for each connected client.
	 */
	private static final ConcurrentHashMap<String, ClientHandler> clientHandlers = new ConcurrentHashMap<>();

	/**
	 * Constructor that initializes the client handler with the client's socket.
	 *
	 * @param socket the socket connected to the client
	 */
	public ClientHandler(Socket socket) {
		this.clientSocket = socket;
	}

	/**
	 * The main run method that handles incoming messages from the client.
	 */
	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

			registerUsername(in, out);
			broadcastClientList();

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if ("chao".equalsIgnoreCase(inputLine)) {
					break;
				}
				handlePrivateMessage(inputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cleanup();
		}
	}

	/**
	 * Registers the username for the client ensuring it is unique and valid.
	 *
	 * @param in  the input stream reader
	 * @param out the output stream writer
	 * @throws IOException if an I/O error occurs
	 */
	private void registerUsername(BufferedReader in, PrintWriter out) throws IOException {
		while (true) {
			username = in.readLine();
			if (username == null || username.equalsIgnoreCase("chao") || username.length() < 3
					|| isUsernameTaken(username)) {
				out.println(getInvalidUsernameMessage());
				continue;
			}
			break;
		}
		logUserActivity(username, true);
		clientHandlers.put(username, this);
	}

	/**
	 * Cleans up resources and notifies other clients when a user disconnects.
	 */
	private void cleanup() {
		try {
			clientHandlers.remove(username);
			logUserActivity(username, false);
			broadcastClientList();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs the user's connection or disconnection.
	 *
	 * @param username username for the client
	 * @param isConnected true if the user is connecting, false if disconnecting
	 */
	public static void logUserActivity(String username, boolean isConnected) {
		String logMessage = isConnected ? " se ha conectado." : " se ha desconectado.";
		System.out.println("El usuario " + username + logMessage);
	}

	/**
	 * Broadcasts the list of currently connected users to all clients.
	 */
	private void broadcastClientList() {
		String clientsList = "\nUsuarios conectados:\n" + String.join("\n", clientHandlers.keySet()) + "\n";
		clientHandlers.values().forEach(handler -> handler.sendMessage(clientsList));
	}

	/**
	 * Handles private messages between clients. If a current chat partner is not
	 * set, it attempts to set one. If a chat partner is set, it forwards the
	 * message to them.
	 *
	 * @param inputLine the input message from the client
	 */
	private void handlePrivateMessage(String inputLine) {
		if (inputLine.equalsIgnoreCase(username)) {
			sendMessage(
					"No puedes chatear contigo mismo. Ingrese otro nombre de usuario con el que desea chatear:");
			return;
		}
		
		if (currentChatWithUser == null) {
			attemptToSetChatPartner(inputLine);
		} else {
			forwardMessageToCurrentChatPartner(inputLine);
		}
	}

	/**
	 * Attempts to set a chat partner if one is not currently set. If the user does
	 * not exist or is not connected, prompts for a valid username.
	 *
	 * @param potentialPartner the potential chat partner's username
	 */
	private void attemptToSetChatPartner(String potentialPartner) {
		if (clientHandlers.containsKey(potentialPartner)) {
			currentChatWithUser = potentialPartner;
			sendMessage("\nAhora estás chateando con " + currentChatWithUser + ". Escribe tu mensaje a continuación.");
		} else {
			promptForValidUsername(potentialPartner);
		}
	}

	/**
	 * Prompts the user to enter a different username because the provided one is
	 * invalid.
	 *
	 * @param invalidUsername the invalid username that was entered
	 */
	private void promptForValidUsername(String invalidUsername) {
		sendMessage(
				"El usuario " + invalidUsername + " no existe o no está conectado. Ingrese otro nombre de usuario:");
	}

	/**
	 * Forwards a message to the current chat partner. If the chat partner has
	 * disconnected, prompts to start a chat with a new user.
	 *
	 * @param message the message to forward
	 */
	private void forwardMessageToCurrentChatPartner(String message) {
		if (clientHandlers.containsKey(currentChatWithUser)) {
			sendMessageToUser(currentChatWithUser, username + ": " + message);
		} else {
			promptForValidUsername(currentChatWithUser);
			currentChatWithUser = null; // Reset the current chat partner
		}
	}

	/**
	 * Sends a message to a specified user by looking up their ClientHandler and
	 * invoking sendMessage on it.
	 *
	 * @param user    the username of the recipient
	 * @param message the message to be sent
	 */
	private void sendMessageToUser(String user, String message) {
		ClientHandler handler = clientHandlers.get(user);
		if (handler != null) {
			handler.sendMessage(message);
		}
	}

	/**
	 * Sends a message to the client connected to this handler by writing to the
	 * client's output stream.
	 *
	 * @param message the message to send
	 */
	private void sendMessage(String message) {
		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if a username is already taken by another client.
	 *
	 * @param username the username to check
	 * @return true if the username is already taken, false otherwise
	 */
	private boolean isUsernameTaken(String username) {
		String userLower = username.toLowerCase();
		return clientHandlers.keySet().stream()
				.anyMatch(existingUsername -> existingUsername.equalsIgnoreCase(userLower));
	}

	/**
	 * Returns an appropriate message if the username is invalid.
	 *
	 * @return a string indicating the reason the username is invalid
	 */
	private String getInvalidUsernameMessage() {
		if (username == null) {
			return "El nombre de usuario no puede estar vacío. Por favor elige otro nombre.";
		}
		if (username.equalsIgnoreCase("chao")) {
			return "El nombre de usuario 'chao' no es válido. Por favor elige otro nombre.";
		}
		if (username.length() < 3) {
			return "El nombre de usuario debe tener al menos 3 caracteres. Por favor elige otro nombre.";
		}
		if (isUsernameTaken(username)) {
			return "El nombre de usuario ya está en uso. Por favor elige otro nombre.";
		}
		return "El nombre de usuario es invaldio. Por favor elige otro nombre.";
	}
}