package poli.persistenciadatostransaccionales.chatapplication.server;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * Creates an instance of the Server class.
 * This default constructor is used to initialize a server ready to accept client connections.
 */
public class Server {
	/**
	 * The maximum number of threads in the thread pool. This sets the limit on the number of concurrent client connections the server can handle.
	 */
	private static final int MAX_THREADS = 10;

	/**
	 * The default server IP address. This is used when the server IP is not specified by the user.
	 */
	private static final String DEFAULT_SERVER_IP = "localhost";

	/**
	 * A BufferedReader for reading input from the console. This is used to read user input for server IP and port configurations.
	 */
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	/**
	 * The entry point of the server application.
	 * 
	 * @param args Command-line arguments, not used in this application.
	 */
	public static void main(String[] args) {
		String serverIp = getServerIp();
		int port = getServerPort();

		ExecutorService clientPool = Executors.newFixedThreadPool(MAX_THREADS);

		try (ServerSocket serverSocket = new ServerSocket(port, 50, InetAddress.getByName(serverIp))) {
			System.out.println("\nServidor iniciado y escuchando en " + serverIp + ":" + port + "\n");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				Runnable clientHandler = new ClientHandler(clientSocket);
				clientPool.execute(clientHandler);
			}
		} catch (UnknownHostException e) {
			System.err.println("No se ha podido encontrar el host: '" + serverIp
					+ "'. Asegúrese de que la dirección IP es correcta.");
		} catch (IOException e) {
			System.err.println("Ha ocurrido un error de E/S al iniciar el servidor: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Reads the server IP from the user input. Defaults to localhost if no input is
	 * provided.
	 * 
	 * @return The IP address of the server.
	 */
	private static String getServerIp() {
		System.out.print("Ingrese la IP del servidor (deje en blanco para localhost): ");
		try {
			String serverIp = reader.readLine();
			return serverIp.isEmpty() ? DEFAULT_SERVER_IP : serverIp;
		} catch (IOException e) {
			throw new RuntimeException("Error al leer la IP del servidor", e);
		}
	}

	/**
	 * Reads the server port from the user input.
	 * 
	 * @return The port number of the server.
	 */
	private static int getServerPort() {
		System.out.print("Ingrese el puerto del servidor: ");
		try {
			return Integer.parseInt(reader.readLine());
		} catch (IOException e) {
			throw new RuntimeException("Error al leer el puerto del servidor", e);
		} catch (NumberFormatException e) {
			throw new RuntimeException(
					"Error al analizar el puerto del servidor. Por favor, introduzca un número entero válido.", e);
		}
	}
}
