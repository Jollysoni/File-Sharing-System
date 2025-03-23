// ✅ Full updated FileClientGUI.java with compact height, click-to-highlight support for both local and server files
package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class FileClientGUI {

    private void applyLightTheme() {
        UIManager.put("Panel.background", new Color(255, 228, 237)); // Baby pink background
        UIManager.put("Label.foreground", Color.BLACK);
        UIManager.put("TextArea.background", new Color(255, 240, 245)); // Lighter pink for preview
        UIManager.put("TextArea.foreground", Color.BLACK);
        UIManager.put("Button.background", Color.BLACK);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("SansSerif", Font.BOLD, 12));
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void applyDarkTheme() {
        UIManager.put("Panel.background", new Color(30, 30, 30));
        UIManager.put("Label.foreground", Color.YELLOW);
        UIManager.put("TextArea.background", new Color(35, 35, 35));
        UIManager.put("TextArea.foreground", Color.YELLOW);
        UIManager.put("Button.background", new Color(255, 215, 0));
        UIManager.put("Button.foreground", Color.BLACK);
        SwingUtilities.updateComponentTreeUI(frame);
    }
    private JFrame frame;
    private JTextArea filePreviewArea;
    private final String serverHost;
    private final String localFolderPath;
    private JPanel clientListPanel, serverListPanel;
    private File selectedServerFile = null;
    private JPanel selectedServerRow = null;
    private File selectedClientFile = null;
    private JPanel selectedClientRow = null;

    public FileClientGUI(String serverHost, String localFolderPath) {
        String clientName = getClientComputerName();
        this.serverHost = serverHost;
        this.localFolderPath = localFolderPath;
        FileClient.configure(serverHost, localFolderPath);

        frame = new JFrame("File Sharer - " + clientName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 600);
        frame.setLayout(new BorderLayout());

        // Theme toggle button
        JMenuBar menuBar = new JMenuBar();
        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Theme");
        viewMenu.add(toggleThemeItem);
        menuBar.add(viewMenu);
        frame.setJMenuBar(menuBar);

        // Default light theme
        applyLightTheme();

        toggleThemeItem.addActionListener(e -> {
            Color bg = frame.getContentPane().getBackground();
            if (bg != null && bg.equals(Color.DARK_GRAY)) {
                applyLightTheme();
            } else {
                applyDarkTheme();
            }
        });

        JPanel topPanel = new JPanel();
        JButton uploadButton = new JButton("Upload");
        uploadButton.setBackground(Color.BLACK);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        uploadButton.setOpaque(true);
        uploadButton.setFocusPainted(false);
        uploadButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        JButton downloadButton = new JButton("Download");
        downloadButton.setBackground(Color.BLACK);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        downloadButton.setOpaque(true);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        topPanel.add(uploadButton);
        topPanel.add(downloadButton);
        frame.add(topPanel, BorderLayout.NORTH);

        clientListPanel = new JPanel();
        serverListPanel = new JPanel();
        clientListPanel.setLayout(new BoxLayout(clientListPanel, BoxLayout.Y_AXIS));
        serverListPanel.setLayout(new BoxLayout(serverListPanel, BoxLayout.Y_AXIS));

        JScrollPane clientScrollPane = new JScrollPane(clientListPanel);
        JScrollPane serverScrollPane = new JScrollPane(serverListPanel);

        clientScrollPane.setBorder(BorderFactory.createTitledBorder("Local Files"));
        serverScrollPane.setBorder(BorderFactory.createTitledBorder("Server Files"));

        JPanel filePanel = new JPanel(new GridLayout(1, 2));
        filePanel.add(clientScrollPane);
        filePanel.add(serverScrollPane);
        frame.add(filePanel, BorderLayout.CENTER);

        filePreviewArea = new JTextArea();
        filePreviewArea.setEditable(false);
        filePreviewArea.setLineWrap(true);
        filePreviewArea.setWrapStyleWord(true);
        JLabel previewLabel = new JLabel("📄 Preview of Selected File");
        previewLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        previewLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 0));

        JScrollPane previewScroll = new JScrollPane(filePreviewArea);
        previewScroll.setPreferredSize(new Dimension(700, 120));
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        previewPanel.add(previewLabel, BorderLayout.NORTH);
        previewPanel.add(previewScroll, BorderLayout.CENTER);
        frame.add(previewPanel, BorderLayout.SOUTH);

        uploadButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Select a file from your system to upload.");
            JFileChooser chooser = new JFileChooser(localFolderPath);
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();
                if (FileClient.uploadFile(selectedFile.getAbsolutePath())) {
                    JOptionPane.showMessageDialog(frame, "File uploaded successfully!");
                    loadServerFiles();
                } else {
                    JOptionPane.showMessageDialog(frame, "File upload failed. Please try again.");
                }
            }
        });

        downloadButton.addActionListener(e -> {
            if (selectedServerFile != null && selectedServerFile.exists()) {
                if (FileClient.downloadFile(selectedServerFile.getName())) {
                    loadClientFiles();
                    JOptionPane.showMessageDialog(frame, "File downloaded successfully!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to download file. Please try again.");
                }
            } else {
                ArrayList<String> serverFileNames = fetchServerFileList();
                String selectedFile = (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a file to download:",
                        "Download File",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        serverFileNames.toArray(),
                        serverFileNames.isEmpty() ? null : serverFileNames.get(0)
                );

                if (selectedFile != null) {
                    if (FileClient.downloadFile(selectedFile)) {
                        loadClientFiles();
                        JOptionPane.showMessageDialog(frame, "File downloaded successfully!");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to download file. Please try again.");
                    }
                }
            }
        });

        loadClientFiles();
        loadServerFiles();

        frame.setVisible(true);
    }

    private void renderFileList(JPanel panel, File[] files, boolean isLocal) {
        panel.removeAll();
        if (files != null) {
            for (File file : files) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                        BorderFactory.createEmptyBorder(2, 10, 2, 10)
                ));
                row.setBackground(Color.WHITE);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                JLabel nameLabel = new JLabel(file.getName());
                nameLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
                nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
                buttonPanel.setOpaque(false);
                buttonPanel.setBorder(BorderFactory.createEmptyBorder());

                JButton propertiesButton = new JButton("ℹ");
                propertiesButton.setFont(new Font("SansSerif", Font.BOLD, 12));
                propertiesButton.setBackground(new Color(220, 220, 220));
                propertiesButton.setForeground(Color.BLACK);
                propertiesButton.setFocusPainted(false);
                propertiesButton.setOpaque(true);
                propertiesButton.setBorderPainted(false);
                propertiesButton.setToolTipText("View file properties");
                propertiesButton.setMargin(new Insets(2, 6, 2, 6));
                propertiesButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
                propertiesButton.addActionListener(e -> showFileProperties(file));

                JButton deleteButton = new JButton("🗑");
                deleteButton.setFont(new Font("SansSerif", Font.BOLD, 12));
                deleteButton.setBackground(new Color(220, 220, 220));
                deleteButton.setForeground(Color.BLACK);
                deleteButton.setFocusPainted(false);
                deleteButton.setOpaque(true);
                deleteButton.setBorderPainted(false);
                deleteButton.setToolTipText("Delete file");
                deleteButton.setMargin(new Insets(2, 6, 2, 6));
                deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
                deleteButton.addActionListener(e -> {
                    if (file.delete()) {
                        JOptionPane.showMessageDialog(frame, "Deleted: " + file.getName());
                        if (isLocal) loadClientFiles();
                        else loadServerFiles();
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to delete file.");
                    }
                });

                nameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        if (isLocal) {
                            selectedClientFile = file;
                            if (selectedClientRow != null) {
                                selectedClientRow.setBackground(null);
                            }
                            selectedClientRow = row;
                            row.setBackground(new Color(200, 255, 200));
                            // Deselect server highlight when local is clicked
                            if (selectedServerRow != null) {
                                selectedServerRow.setBackground(null);
                                selectedServerRow = null;
                                selectedServerFile = null;
                            }
                        } else {
                            selectedServerFile = file;
                            if (selectedServerRow != null) {
                                selectedServerRow.setBackground(null);
                            }
                            selectedServerRow = row;
// Deselect local highlight when server is clicked
                            if (selectedClientRow != null) {
                                selectedClientRow.setBackground(null);
                                selectedClientRow = null;
                                selectedClientFile = null;
                            }
                            row.setBackground(new Color(200, 230, 255));
                        }
                        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                            filePreviewArea.setText("");
                            String line;
                            while ((line = reader.readLine()) != null) {
                                filePreviewArea.append(line + "\n");
                            }
                        } catch (IOException ex) {
                            filePreviewArea.setText("Error reading file.");
                        }
                    }
                });

                buttonPanel.add(propertiesButton);
                buttonPanel.add(deleteButton);

                row.add(nameLabel, BorderLayout.CENTER);
                row.add(buttonPanel, BorderLayout.EAST);

                row.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        row.setBackground(new Color(245, 245, 245));
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if ((isLocal && row != selectedClientRow) || (!isLocal && row != selectedServerRow)) {
                            row.setBackground(Color.WHITE);
                        }
                    }
                });
                panel.add(row);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void showFileProperties(File file) {
        if (file != null && file.exists()) {
            long size = file.length();
            long modified = file.lastModified();
            String preview = "";
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                preview = reader.readLine();
                if (preview != null && preview.length() > 100) {
                    preview = preview.substring(0, 100) + "...";
                }
            } catch (IOException e) {
                preview = "Could not read preview.";
            }
            JOptionPane.showMessageDialog(frame,
                    "File: " + file.getName() +
                            "\nSize: " + size + " bytes" +
                            "\nLast Modified: " + new java.util.Date(modified) +
                            "\nPreview: " + preview,
                    "File Properties", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(frame, "File not found.");
        }
    }

    private void loadClientFiles() {
        selectedClientFile = null;
        selectedClientRow = null;
        File folder = new File(localFolderPath);
        System.out.println("Local folder path: " + folder.getAbsolutePath());
        if (!folder.exists() && !folder.mkdir()) {
            JOptionPane.showMessageDialog(frame, "Could not create local folder!");
            return;
        }
        File[] files = folder.listFiles();
        renderFileList(clientListPanel, files, true);
    }

    private void loadServerFiles() {
        selectedServerFile = null;
        selectedServerRow = null;
        ArrayList<String> fileNames = fetchServerFileList();
        File[] files = fileNames.stream().map(name -> new File("server_files/" + name)).toArray(File[]::new);
        renderFileList(serverListPanel, files, false);
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

    private String getClientComputerName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            String hostname = inetAddress.getHostName();
            if (hostname.endsWith(".local")) {
                hostname = hostname.substring(0, hostname.length() - 6);
            }
            return hostname.replace("-", " ");
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FileClientGUI <server-hostname> <local-folder-path>. Add the configuration please.");
            System.exit(1);
        }
        String serverHost = args[0];
        String localFolder = args[1];
        SwingUtilities.invokeLater(() -> new FileClientGUI(serverHost, localFolder));
    }
}
