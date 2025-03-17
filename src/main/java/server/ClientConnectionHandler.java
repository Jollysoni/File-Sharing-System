package server;

import java.io.*;
import java.net.Socket;

public class ClientConnectionHandler implements Runnable {

    private Socket clientSocket;

    public ClientConnectionHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream input = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                OutputStream output = clientSocket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)
        ) {
            String clientMessage;

            // Read messages from the client
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println("Received: " + clientMessage);

                // Sample response logic (echo server)
                writer.println("Echo: " + clientMessage);

                // Break if "exit" is received
                if ("exit".equalsIgnoreCase(clientMessage)) {
                    System.out.println("Client disconnected");
                    break;
                }
            }

        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                System.out.println("Error closing client socket: " + ex.getMessage());
            }
        }
    }
}