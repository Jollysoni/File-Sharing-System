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
                if (clientMessage.startsWith("DIR")) {
                    File sharedFolder = new File("server_files");
                    File[] files = sharedFolder.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            writer.println(file.getName());
                        }
                    }
                    // Done responding, break the connection
                    break;
                }
                else if (clientMessage.startsWith("UPLOAD ")) {
                    String fileName = clientMessage.substring(7).trim();
                    File targetFile = new File("server_files", fileName);

                    try (PrintWriter fileWriter = new PrintWriter(new FileWriter(targetFile))) {
                        String fileLine;
                        while ((fileLine = reader.readLine()) != null) {
                            fileWriter.println(fileLine);
                        }
                        System.out.println("Uploaded file: " + fileName);
                    } catch (IOException e) {
                        System.out.println("Error writing uploaded file: " + e.getMessage());
                    }

                    break; // Disconnect after handling one command
                }
                else if (clientMessage.startsWith("DOWNLOAD ")) {
                    String fileName = clientMessage.substring(9).trim();
                    File sourceFile = new File("server_files", fileName);

                    try (BufferedReader fileReader = new BufferedReader(new FileReader(sourceFile))) {
                        String line;
                        while ((line = fileReader.readLine()) != null) {
                            writer.println(line);
                        }
                        System.out.println("Sent file: " + fileName);
                    } catch (IOException e) {
                        System.out.println("Error reading file for download: " + e.getMessage());
                    }

                    break; // Disconnect after handling one command
                }



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