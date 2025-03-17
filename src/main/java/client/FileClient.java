package client;

import java.io.*;
import java.util.ArrayList;

public class FileClient {
    private static final String SERVER_FOLDER = "server_files"; // Simulated server folder

    public static boolean uploadFile(String filePath) {
        File sourceFile = new File(filePath);
        if (!sourceFile.exists()) {
            return false;
        }
        File serverFolder = new File(SERVER_FOLDER);
        if (!serverFolder.exists() && !serverFolder.mkdir()) {
            return false;
        }

        try {
            File destFile = new File(serverFolder, sourceFile.getName());
            try (InputStream in = new FileInputStream(sourceFile);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean downloadFile(String fileName) {
        File serverFile = new File(SERVER_FOLDER, fileName);
        if (!serverFile.exists()) {
            return false;
        }

        File localFolder = new File("local_folder");
        if (!localFolder.exists() && !localFolder.mkdir()) {
            return false;
        }

        try {
            File destFile = new File(localFolder, serverFile.getName());
            try (InputStream in = new FileInputStream(serverFile);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ArrayList<String> listFilesOnServer() {
        ArrayList<String> filesList = new ArrayList<>();
        File serverFolder = new File(SERVER_FOLDER);
        if (serverFolder.exists()) {
            File[] files = serverFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    filesList.add(file.getName());
                }
            }
        }
        return filesList;
    }
}