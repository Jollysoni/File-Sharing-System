package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class FileClient {

    // Hostname/IP of the server and local folder path
    private static String serverHost = "localhost";
    private static String localFolderPath = "local_folder";

    // Method to configure the server host and local folder path from GUI
    public static void configure(String host, String localFolder) {
        serverHost = host;
        localFolderPath = localFolder;
    }

    // Uploads a file from the local folder to the server via socket
    public static boolean uploadFile(String filePath) {
        File file = new File(filePath);

        // Check if file exists before attempting upload
        if (!file.exists()) {
            return false;
        }

        try (
                // Connect to the server
                Socket socket = new Socket(serverHost, 12345);

                // Writer to send data to the server
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                // Reader to read the contents of the file to upload
                BufferedReader reader = new BufferedReader(new FileReader(file));

                // Reader to receive potential server responses (currently unused)
                BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            // Send the UPLOAD command followed by the file name
            writer.println("UPLOAD " + file.getName());

            // Send each line of the file to the server
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }

            return true;

        } catch (IOException e) {
            // Print any connection or file I/O errors
            e.printStackTrace();
            return false;
        }
    }

    // Downloads a file from the server and saves it into the local folder
    public static boolean downloadFile(String fileName) {
        File localFolder = new File(localFolderPath);
        System.out.println("Downloading into: " + localFolderPath);

        // Create the local folder if it doesn't exist
        if (!localFolder.exists() && !localFolder.mkdir()) {
            return false;
        }

        // File where downloaded content will be saved
        File destFile = new File(localFolder, fileName);

        try (
                // Connect to the server
                Socket socket = new Socket(serverHost, 12345);

                // Writer to send the DOWNLOAD command to the server
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                // Reader to receive file content from the server
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Writer to save received content to the local file
                PrintWriter fileWriter = new PrintWriter(new FileWriter(destFile))
        ) {

            // Send the DOWNLOAD command followed by the file name
            writer.println("DOWNLOAD " + fileName);

            // Receive and write each line of the file from the server
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.println(line);
            }

            return true;

        } catch (IOException e) {
            // Handle connection or I/O errors
            e.printStackTrace();
            return false;
        }
    }

    // Retrieves the list of files on the server by sending the DIR command
    public static ArrayList<String> listFilesOnServer() {
        ArrayList<String> filesList = new ArrayList<>();

        try (
                // Connect to the server
                Socket socket = new Socket(serverHost, 12345);

                // Writer to send the DIR command
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

                // Reader to receive the list of files
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {

            // Send the DIR command to the server
            writer.println("DIR");

            // Read and collect each filename from the server response
            String line;
            while ((line = reader.readLine()) != null) {
                filesList.add(line);
            }

        } catch (IOException e) {
            // Handle connection or read errors
            e.printStackTrace();
        }

        return filesList;
    }
}
