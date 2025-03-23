package client;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

public class FileClientGUI {
    private JFrame frame;
    private DefaultListModel<String> clientFilesModel;
    private DefaultListModel<String> serverFilesModel;

    public FileClientGUI() {

        String clientName = getClientComputerName();
        frame = new JFrame("File Sharer - " + clientName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Button panel
        JPanel topPanel = new JPanel();
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        topPanel.add(uploadButton);
        topPanel.add(downloadButton);
        frame.add(topPanel, BorderLayout.NORTH);

        // File lists
        clientFilesModel = new DefaultListModel<>();
        serverFilesModel = new DefaultListModel<>();
        JList<String> clientFilesList = new JList<>(clientFilesModel);
        JList<String> serverFilesList = new JList<>(serverFilesModel);

        JPanel filePanel = new JPanel(new GridLayout(1, 2));
        filePanel.add(new JScrollPane(clientFilesList));
        filePanel.add(new JScrollPane(serverFilesList));
        frame.add(filePanel, BorderLayout.CENTER);

        loadClientFiles();
        loadServerFiles();

        // Add button actions
        uploadButton.addActionListener(e -> {
            String selectedFile = clientFilesList.getSelectedValue();
            if (selectedFile != null) {
                if (FileClient.uploadFile("local_folder/" + selectedFile)) {
                    JOptionPane.showMessageDialog(frame, "File uploaded successfully!");

                    // Refresh the server file list
                    loadServerFiles();

                    // Select the uploaded file on the server list
                    serverFilesList.setSelectedValue(selectedFile, true);
                } else {
                    JOptionPane.showMessageDialog(frame, "File upload failed. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file from the left-hand list.");
            }
        });

        downloadButton.addActionListener(e -> {
            String selectedFile = serverFilesList.getSelectedValue();
            if (selectedFile != null) {
                if (FileClient.downloadFile(selectedFile)) {
                    loadClientFiles(); // Refresh client list
                    JOptionPane.showMessageDialog(frame, "File downloaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to download file. Please try again.");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a file to download.");
            }
        });

        frame.setVisible(true);
    }

    private String getClientComputerName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String hostName = inetAddress.getHostName();
            if (hostName.endsWith(".local")) {
                hostName = hostName.substring(0, hostName.length() - 6);
            }
            return hostName.replace("-", " ");// Returns the client's computer name
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown"; // Return "Unknown" if the name can't be fetched
        }
    }
    private void loadClientFiles() {
        clientFilesModel.clear();
        File folder = new File("local_folder");
        if (!folder.exists() && !folder.mkdir()) {
            JOptionPane.showMessageDialog(frame, "Could not create local folder!");
            return;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                clientFilesModel.addElement(file.getName());
            }
        }
    }

    private void loadServerFiles() {
        serverFilesModel.clear();
        ArrayList<String> serverFiles = fetchServerFileList();
        for (String file : serverFiles) {
            serverFilesModel.addElement(file);
        }
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileClientGUI::new);
    }
}