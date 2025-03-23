package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class FileClientGUI {
    // Main application frame
    private JFrame frame;

    // List models for client and server file lists
    private DefaultListModel<String> clientFilesModel;
    private DefaultListModel<String> serverFilesModel;

    // Hostname of the server and path to local shared folder
    private final String serverHost;
    private final String localFolderPath;

    // Constructor to initialize the GUI with server and folder information
    public FileClientGUI(String serverHost, String localFolderPath) {
        this.serverHost = serverHost;
        this.localFolderPath = localFolderPath;

        // Pass the configuration to the FileClient for use in uploads/downloads
        FileClient.configure(serverHost, localFolderPath);

        // Setup the main application window
        frame = new JFrame("File Sharer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Create the top panel with Upload and Download buttons
        JPanel topPanel = new JPanel();
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        topPanel.add(uploadButton);
        topPanel.add(downloadButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // Create list models and JLists to display client and server files
        clientFilesModel = new DefaultListModel<>();
        serverFilesModel = new DefaultListModel<>();
        JList<String> clientFilesList = new JList<>(clientFilesModel);
        JList<String> serverFilesList = new JList<>(serverFilesModel);

        // Add both lists side-by-side in a scrollable panel
        JPanel filePanel = new JPanel(new GridLayout(1, 2));
        filePanel.add(new JScrollPane(clientFilesList));
        filePanel.add(new JScrollPane(serverFilesList));
        frame.add(filePanel, BorderLayout.CENTER);

        // Load initial files from local folder and server
        loadClientFiles();
        loadServerFiles();

        // Action listener for Upload button
        uploadButton.addActionListener(e -> {
            String selectedFile = clientFilesList.getSelectedValue();
            if (selectedFile != null) {
                // Attempt to upload the selected file to the server
                if (FileClient.uploadFile(localFolderPath + "/" + selectedFile)) {
                    JOptionPane.showMessageDialog(frame, "File uploaded successfully!");

                    // Refresh server file list after upload
                    loadServerFiles();

                    // Highlight the uploaded file in the server list
                    serverFilesList.setSelectedValue(selectedFile, true);
                } else {
                    JOptionPane.showMessageDialog(frame, "File upload failed. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file from the left-hand list.");
            }
        });

        // Action listener for Download button
        downloadButton.addActionListener(e -> {
            String selectedFile = serverFilesList.getSelectedValue();
            if (selectedFile != null) {
                // Attempt to download the selected file from the server
                if (FileClient.downloadFile(selectedFile)) {
                    loadClientFiles(); // Refresh the client file list after download
                    JOptionPane.showMessageDialog(frame, "File downloaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to download file. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file to download.");
            }
        });

        // Show the window
        frame.setVisible(true);
    }

    // Loads the list of files from the client's local folder
    private void loadClientFiles() {
        clientFilesModel.clear();
        File folder = new File(localFolderPath);

        // Print the actual folder being accessed for debugging
        System.out.println("Local folder path: " + folder.getAbsolutePath());

        // Create the folder if it doesn't exist
        if (!folder.exists() && !folder.mkdir()) {
            JOptionPane.showMessageDialog(frame, "Could not create local folder!");
            return;
        }

        // Add all filenames in the folder to the list model
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                clientFilesModel.addElement(file.getName());
            }
        }
    }

    // Loads the list of files from the server (via socket)
    private void loadServerFiles() {
        serverFilesModel.clear();
        ArrayList<String> serverFiles = fetchServerFileList();
        for (String file : serverFiles) {
            serverFilesModel.addElement(file);
        }
    }

    // Retrieves the list of server files from FileClient (via DIR command)
    private ArrayList<String> fetchServerFileList() {
        ArrayList<String> serverFiles = new ArrayList<>();
        try {
            serverFiles = FileClient.listFilesOnServer();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error fetching server files: " + e.getMessage());
            e.printStackTrace();
        }
        return serverFiles;
    }

    // Main method: expects server host and local folder path as command-line args
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileClientGUI <server-hostname> <local-folder-path>");
            System.exit(1);
        }

        String serverHost = args[0];
        String localFolder = args[1];

        // Start the GUI on the Swing event thread
        SwingUtilities.invokeLater(() -> new FileClientGUI(serverHost, localFolder));
    }
}
