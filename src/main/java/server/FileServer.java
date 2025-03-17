package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileServer {

    // Port number on which the server will listen for client connections
    private static final int PORT = 12345;

    // Define a fixed thread pool size for optimizing thread usage
    private static final int THREAD_POOL_SIZE = 10;

    public static void main(String[] args) {
        // Create a thread pool with a fixed number of threads
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Start the server and listen for incoming client connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            // Infinite loop to accept and handle client connections
            while (true) {
                // Accept a new connection from a client
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                /*
                 * Instead of creating a new thread manually for each client,
                 * submit the client-handling task (ClientConnectionHandler)
                 * to the thread pool. This ensures threads will be reused
                 * and limits the total number of threads.
                 */
                threadPool.execute(new ClientConnectionHandler(clientSocket));
            }

        } catch (IOException ex) {
            // Handle exceptions related to the ServerSocket or client connections
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            /*
             * Ensure that the thread pool is shut down when the server stops.
             * This releases any system resources allocated to the pool.
             */
            threadPool.shutdown();
        }
    }
}