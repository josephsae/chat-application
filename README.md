# Chat Application

## Overview
This Java-based chat application provides a simple yet functional text-based interface for multiple users to communicate in real-time. It is designed using a client-server architecture where users can connect to a central server, register with a unique username, and start private conversations with other users.

## Features
- **Multi-threaded server:** Handles concurrent client connections seamlessly.
- **Client-server architecture:** Separate modules for client and server allow for easy scalability.
- **Private messaging:** Users can engage in private conversations with other users.
- **Dynamic user registration:** New users can join and existing users can leave the chat session at any time.

## Prerequisites
- Java SE Development Kit (JDK) 8 or higher.

## Installation
1. Clone the repository to your local machine:
```bash
git clone https://github.com/yourusername/chatapplication.git
```
2. Navigate to the src directory of the project.
3. Compile the Java files:
```bash
javac poli/persistenciadatostransaccionales/chatapplication/client/Client.java
javac poli/persistenciadatostransaccionales/chatapplication/server/Server.java
javac poli/persistenciadatostransaccionales/chatapplication/server/ClientHandler.java
```
## Usage
1. Start the server by running the Server class. You can specify the server IP and port, or use defaults:
```bash
java poli.persistenciadatostransaccionales.chatapplication.server.Server
```
2. On a separate terminal window, start the client by running the Client class. Connect to the server using the provided IP and port:
```bash
java poli.persistenciadatostransaccionales.chatapplication.client.Client
```
3. Follow the on-screen prompts to enter the server IP, port, and your chosen username.
