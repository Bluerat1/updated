import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SpotifyLoginGUI extends JFrame {

    private static final String JDBC_URL = "jdbc:sqlite:C:\\Users\\Rakk\\Desktop\\music.db";
    private static final String TABLE_NAME = "login";
    private static final String[] MUSIC_GENRES = {"Pop", "Rock", "Hip Hop", "Electronic"};
    private static final String[] ALBUMS = {"Album 1", "Album 2", "Album 3"};
    private static final String[] MUSIC_LIST = {"Song 1", "Song 2", "Song 3", "Song 4", "Song 5", "Song 6"};


    private JList<String> musicList;
    private JPasswordField passwordEntry;

    public SpotifyLoginGUI() {

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "SQLite JDBC driver not found");
            System.exit(1);
        }

        setTitle("Music Player - Spotify Style");
        setSize(500, 453);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        ImageIcon background = new ImageIcon("C:\\Users\\Rakk\\Desktop\\spotify-playlist.jpeg");
        JLabel backgroundLabel = new JLabel(background);
        add(backgroundLabel, BorderLayout.CENTER);
        backgroundLabel.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        JLabel labelUsername = new JLabel("Username:");
        labelUsername.setForeground(Color.WHITE);
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(labelUsername, constraints);

        JTextField usernameEntry = new JTextField(15);
        constraints.gridx = 1;
        constraints.gridy = 0;
        panel.add(usernameEntry, constraints);

        JLabel labelPassword = new JLabel("Password:");
        labelPassword.setForeground(Color.WHITE);
        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(labelPassword, constraints);

        JPasswordField passwordEntry = new JPasswordField(15);
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(passwordEntry, constraints);

        JPanel showPasswordCheckBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showPasswordCheckBoxPanel.setBackground(Color.BLACK);

        ImageIcon eyeIcon = new ImageIcon("C:\\Users\\Rakk\\Desktop\\eye.png");
        ImageIcon closedEyeIcon = new ImageIcon("C:\\Users\\Rakk\\Desktop\\close-eye.png");

        JCheckBox showPasswordCheckBox = new JCheckBox(closedEyeIcon); // Start with the closed eye icon
        showPasswordCheckBox.setForeground(Color.WHITE);
        showPasswordCheckBox.setBackground(Color.BLACK);

        showPasswordCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    passwordEntry.setEchoChar((char) 0);
                    showPasswordCheckBox.setIcon(eyeIcon);
                } else {
                    passwordEntry.setEchoChar('\u2022'); // Use a bullet character for hiding
                    showPasswordCheckBox.setIcon(closedEyeIcon);
                }
            }
        });

        showPasswordCheckBoxPanel.add(showPasswordCheckBox);
        constraints.gridx = 2;
        constraints.gridy = 1;
        panel.add(showPasswordCheckBoxPanel, constraints);

        ((AbstractDocument) passwordEntry.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text, attrs);
                if (passwordEntry.getPassword().length > 15) {
                    passwordEntry.setText(String.valueOf(passwordEntry.getPassword()).substring(0, 15));
                }
            }
        });

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameEntry.getText();
                String password = new String(passwordEntry.getPassword());

                if (authenticateUser(username, password)) {
                    openMusicListWindow();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed. Invalid username or password.");
                }
            }
        });

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleCreateAccount();
            }
        });

        
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.gridwidth = 2;

        constraints.gridx = 1;
        constraints.gridy = 4;  // Move to the next row
        constraints.gridwidth = 2;
        panel.add(createAccountButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        panel.add(loginButton, constraints);

        backgroundLabel.add(panel);

        setLocationRelativeTo(null);
        setVisible(true);
        setLocation(getLocation().x + 200, getLocation().y);
    }

    private void handleCreateAccount() {
        String username = JOptionPane.showInputDialog("Enter username:");
        char[] password = promptForPassword();

        if (username != null && password != null) {
            if (createAccount(username, new String(password))) {
                JOptionPane.showMessageDialog(null, "Account created successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Failed to create account. Please try again.");
            }
        }
    }

    private boolean createAccount(String username, String password) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + TABLE_NAME + " (username, password) VALUES (?, ?)")) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private char[] promptForPassword() {
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showOptionDialog(null, passwordField, "Enter password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);

        if (option == JOptionPane.OK_OPTION) {
            return passwordField.getPassword();
        } else {
            return null;
        }
    }

    private boolean authenticateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND password = ?")) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void openMusicListWindow() {
        JFrame musicListFrame = new JFrame("Music List");
        musicListFrame.setSize(1000, 400);

        JPanel searchPanel = new JPanel();
        JTextField searchBar = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JComboBox<String> genreComboBox = new JComboBox<>(MUSIC_GENRES);
        JComboBox<String> albumComboBox = new JComboBox<>(ALBUMS);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Logout Confirmation", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    musicListFrame.dispose();
                    new SpotifyLoginGUI();
                }
            }
        });

        searchPanel.add(searchBar);
        searchPanel.add(searchButton);
        searchPanel.add(new JLabel("Genre:"));
        searchPanel.add(genreComboBox);
        searchPanel.add(new JLabel("Album:"));
        searchPanel.add(albumComboBox);
        searchPanel.add(logoutButton);

        DefaultListModel<String> musicListModel = new DefaultListModel<>();
        for (String song : MUSIC_LIST) {
            musicListModel.addElement(song);
        }

        musicList = new JList<>(musicListModel);
        musicList.setBackground(Color.BLACK);
        musicList.setForeground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(musicList);
        musicList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = musicList.locationToIndex(e.getPoint());
                    musicList.setSelectedIndex(index);
                    showPopupMenu(e.getX(), e.getY());
                }
            }
        });

        musicListFrame.add(searchPanel, BorderLayout.NORTH);
        musicListFrame.add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchBar.getText();
                String selectedGenre = (String) genreComboBox.getSelectedItem();
                String selectedAlbum = (String) albumComboBox.getSelectedItem();

                musicListModel.clear();
                for (String song : MUSIC_LIST) {
                    musicListModel.addElement(song + " (Search: " + searchTerm +
                            ", Genre: " + selectedGenre +
                            ", Album: " + selectedAlbum + ")");
                }
            }
        });

        musicListFrame.setLocationRelativeTo(null);
        musicListFrame.setVisible(true);
        musicListFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void showPopupMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem playItem = new JMenuItem("Play");
        JMenuItem addToPlaylistItem = new JMenuItem("Add to Playlist");
        JMenuItem removeFromPlaylistItem = new JMenuItem("Remove from Playlist");

        playItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Playing song: " + musicList.getSelectedValue());
            }
        });

        addToPlaylistItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Adding to playlist: " + musicList.getSelectedValue());
            }
        });

        removeFromPlaylistItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Removing from playlist: " + musicList.getSelectedValue());
            }
        });

        popupMenu.add(playItem);
        popupMenu.add(addToPlaylistItem);
        popupMenu.add(removeFromPlaylistItem);

        popupMenu.show(musicList, x, y);
    }

   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpotifyLoginGUI::new);
    }
}
