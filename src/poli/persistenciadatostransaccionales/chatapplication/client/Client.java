package poli.persistenciadatostransaccionales.chatapplication.client;

import java.io.*;
import java.net.*;

/**
 * Creates an instance of the Client class.
 * This default constructor is used to create a client that can connect to a chat server.
 */
public class Client {

	/**
	 * Main entry point for the chat client. Sets up the connection and handles I/O.
	 * It prompts the user for the server IP and port, then attempts to handle
	 * the server connection which includes listening for server messages and
	 * sending user input to the server.
	 *
	 * @param args Command-line arguments, not used in this application.
	 */
	public static void main(String[] args) {
		try {
			String host = prompt("Ingrese la IP del servidor: ");
			int port = Integer.parseInt(prompt("Ingrese el puerto del servidor: "));

			handleServerConnection(host, port);
		} catch (IOException e) {
			System.err.println("Error al leer de la entrada estándar: " + e.getMessage());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println("El puerto debe ser un número entero. Por favor, reintente.");
		}
	}

	/**
	 * Establishes the connection to the server and handles bi-directional
	 * communication.
	 *
	 * @param host IP address or hostname of the server.
	 * @param port Server port.
	 */
	private static void handleServerConnection(String host, int port) {
		try (Socket socket = new Socket(host, port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

			System.out.println("\nConectado al servidor en " + host + ":" + port + ".");
			System.out.println("Para salir, escriba <chao> en cualquier momento.");

			String username;
			System.out.print("\nIngrese su nombre de usuario: ");
			username = stdIn.readLine();
			out.println(username);

			startServerListener(in);
			performChat(out, stdIn);

		} catch (UnknownHostException e) {
			System.err.println("No se puede encontrar el host: " + host);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("No se pudo obtener I/O para la conexión con: " + host);
			System.exit(1);
		}
	}

	/**
	 * Starts a thread to listen for messages from the server.
	 *
	 * @param in BufferedReader to read messages from the server.
	 */
	private static void startServerListener(BufferedReader in) {
		new Thread(() -> {
			String fromServer;
			try {
				while ((fromServer = in.readLine()) != null) {
					System.out.println(fromServer);
				}
			} catch (IOException e) {
				System.err.println("Error al leer del servidor: " + e.getMessage());
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Handles the chat process with the server.
	 *
	 * @param out   PrintWriter to send messages to the server.
	 * @param stdIn BufferedReader to read user input.
	 * @throws IOException If there is an I/O error.
	 */
	private static void performChat(PrintWriter out, BufferedReader stdIn) throws IOException {
		System.out.println("\nIngrese el nombre del usuario con el que desea chatear:");
		String chatWithUser = stdIn.readLine();
		out.println(chatWithUser);

		String fromUser;
		do {
			fromUser = stdIn.readLine();
			out.println(fromUser);
			if (fromUser.equalsIgnoreCase("chao")) {
				break;
			}
		} while (true);
	}

	/**
	 * Prompts the user for input and returns the input as a string.
	 *
	 * @param prompt The message to display to the user.
	 * @return The user's input as a string.
	 * @throws IOException If there is an I/O error.
	 */
	private static String prompt(String prompt) throws IOException {
		System.out.print(prompt);
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		return reader.readLine();
	}
}
