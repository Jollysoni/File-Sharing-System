package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class FileClient {

    private static String serverHost = "localhost";
    private static String localFolderPath = "local_folder";

    public static void configure(String host, String localFolder) {
        serverHost = host;
        localFolderPath = localFolder;
    }

    public static boolean uploadFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }

        try (Socket socket = new Socket(serverHost, 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new FileReader(file));
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send command: UPLOAD filename
            writer.println("UPLOAD " + file.getName());

            // Send file content line by line
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean downloadFile(String fileName) {
        File localFolder = new File(localFolderPath);
        if (!localFolder.exists() && !localFolder.mkdir()) {
            return false;
        }

        File destFile = new File(localFolder, fileName);

        try (Socket socket = new Socket(serverHost, 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter fileWriter = new PrintWriter(new FileWriter(destFile))) {

            // Send the download command
            writer.println("DOWNLOAD " + fileName);

            // Read the content of the file line by line and save it
            String line;
            while ((line = reader.readLine()) != null) {
                fileWriter.println(line);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> listFilesOnServer() {
        ArrayList<String> filesList = new ArrayList<>();

        try (Socket socket = new Socket(serverHost, 12345);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the DIR command
            writer.println("DIR");

            // Read the list of files from the server
            String line;
            while ((line = reader.readLine()) != null) {
                filesList.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return filesList;
    }
}
