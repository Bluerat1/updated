package javaapplication2;

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
    private static final String SONGS_JDBC_URL = "jdbc:sqlite:C:\\Users\\Rakk\\Desktop\\music.db";

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
        
        DefaultListModel<String> musicListModel = new DefaultListModel<>();
        loadSongsFromDatabase(musicListModel);

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
        
        JButton addSongsButton = new JButton("Add Songs");
        addSongsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddSongs();
            }
        });
        
        JButton createPlaylistButton = new JButton("Create Playlist");
createPlaylistButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        handleCreatePlaylist();
    }
});
    
     JButton showPlaylistsButton = new JButton("Show Playlists");
    showPlaylistsButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            handleShowPlaylists();
        }
    });
    
        
        
        searchPanel.add(searchBar);
        searchPanel.add(searchButton);
        searchPanel.add(logoutButton);
        searchPanel.add(addSongsButton);
        searchPanel.add(createPlaylistButton);
        searchPanel.add(showPlaylistsButton);
        
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

        musicListModel.clear();
        searchSongsInDatabase(musicListModel, searchTerm);
    }
    
    
});

        musicListFrame.setLocationRelativeTo(null);
        musicListFrame.setVisible(true);
        musicListFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void searchSongsInDatabase(DefaultListModel<String> musicListModel, String searchTerm) {
    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM songs WHERE title LIKE ? OR artist LIKE ? OR genre LIKE ? OR album LIKE ?")) {

        preparedStatement.setString(1, "%" + searchTerm + "%");
        preparedStatement.setString(2, "%" + searchTerm + "%");
        preparedStatement.setString(3, "%" + searchTerm + "%");
        preparedStatement.setString(4, "%" + searchTerm + "%");

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String genre = resultSet.getString("genre");
                String album = resultSet.getString("album");

                musicListModel.addElement(title + " - " + artist + " (" + genre + ", " + album + ")");
            }
        }

    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}
    
    private void handleAddSongs() {
    JTextField titleField = new JTextField();
    JTextField artistField = new JTextField();
    JTextField genreField = new JTextField();
    JTextField albumField = new JTextField();

    Object[] fields = {
            "Title:", titleField,
            "Artist:", artistField,
            "Genre:", genreField,
            "Album:", albumField
    };

    int result = JOptionPane.showConfirmDialog(null, fields, "Add Songs", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String title = titleField.getText();
        String artist = artistField.getText();
        String genre = genreField.getText();
        String album = albumField.getText();

        if (!title.isEmpty() && !artist.isEmpty() && !genre.isEmpty() && !album.isEmpty()) {
            addSongToDatabase(title, artist, genre, album);
            loadSongsFromDatabase((DefaultListModel<String>) musicList.getModel());
        } else {
            JOptionPane.showMessageDialog(null, "All fields must be filled in.");
        }
    }
}
    private void addSongToDatabase(String title, String artist, String genre, String album) {
    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO songs (title, artist, genre, album) VALUES (?, ?, ?, ?)")) {

        preparedStatement.setString(1, title);
        preparedStatement.setString(2, artist);
        preparedStatement.setString(3, genre);
        preparedStatement.setString(4, album);

        preparedStatement.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to add the song to the database.");
    }
}
    
private void handleCreatePlaylist() {
    String playlistName = JOptionPane.showInputDialog("Enter playlist name:");

    if (playlistName != null && !playlistName.isEmpty()) {
        createPlaylist(playlistName);
        // Optionally, you can refresh the playlist list or perform other actions
        // based on your application's requirements.
    } else {
        JOptionPane.showMessageDialog(null, "Playlist name cannot be empty.");
    }
}

private void createPlaylist(String playlistName) {
    // Implement the logic to add the playlist to your database or perform
    // any other actions needed for playlist creation.
    // You can use JDBC to interact with your database and store the playlist information.
    // Example:
    try (Connection connection = DriverManager.getConnection(JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO playlists (name) VALUES (?)")) {

        preparedStatement.setString(1, playlistName);
        preparedStatement.executeUpdate();

        JOptionPane.showMessageDialog(null, "Playlist created successfully!");

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to create the playlist. Please try again.");
    }
}

private void showAddToPlaylistDialog() {
    String selectedSong = musicList.getSelectedValue();

    DefaultListModel<String> playlistListModel = new DefaultListModel<>();
    loadPlaylistsFromDatabase(playlistListModel);

    JComboBox<String> playlistComboBox = new JComboBox<>();
    for (int i = 0; i < playlistListModel.size(); i++) {
    playlistComboBox.addItem(playlistListModel.getElementAt(i));
}

    int result = JOptionPane.showConfirmDialog(null, playlistComboBox, "Add to Playlist", JOptionPane.OK_CANCEL_OPTION);
    if (result == JOptionPane.OK_OPTION) {
        String selectedPlaylist = (String) playlistComboBox.getSelectedItem();
        addSongToPlaylist(selectedSong, selectedPlaylist);
        JOptionPane.showMessageDialog(null, "Song added to playlist: " + selectedPlaylist);
    }
}

private void addSongToPlaylist(String song, String playlist) {
    // Implement the logic to associate a song with a playlist in the database.
    // You'll need to update your database schema and perform the necessary SQL operations.

    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO playlist_songs (playlist_name, song_title) VALUES (?, ?)")) {

        preparedStatement.setString(1, playlist);
        preparedStatement.setString(2, song);

        preparedStatement.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, "Failed to add the song to the playlist.");
    }
}
    
    private void handleShowPlaylists() {
    DefaultListModel<String> playlistListModel = new DefaultListModel<>();
    loadPlaylistsFromDatabase(playlistListModel);

    JList<String> playlistList = new JList<>(playlistListModel);
    playlistList.setBackground(Color.BLACK);
    playlistList.setForeground(Color.WHITE);
    JScrollPane scrollPane = new JScrollPane(playlistList);
    
    JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem openPlaylistItem = new JMenuItem("Open Playlist");
    
    openPlaylistItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedPlaylist = playlistList.getSelectedValue();
            if (selectedPlaylist != null) {
                openPlaylist(selectedPlaylist);
            }
        }
    });

    popupMenu.add(openPlaylistItem);

    playlistList.setComponentPopupMenu(popupMenu);
    
    JPanel playlistsPanel = new JPanel(new BorderLayout());
    playlistsPanel.add(scrollPane, BorderLayout.CENTER);

    JFrame playlistsFrame = new JFrame("Playlists");
    playlistsFrame.setSize(400, 300);
    playlistsFrame.setLocationRelativeTo(null);
    playlistsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    playlistsFrame.add(playlistsPanel);
    playlistsFrame.setVisible(true);
}
    

private void loadSongsFromPlaylist(DefaultListModel<String> playlistSongsModel, String playlistName) {
    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT s.title, s.artist, s.genre, s.album " +
                "FROM playlist_songs ps " +
                "JOIN songs s ON ps.song_title = s.title " +
                "WHERE ps.playlist_name = ?")) {

        preparedStatement.setString(1, playlistName);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String genre = resultSet.getString("genre");
                String album = resultSet.getString("album");

                String songInfo = title + " - " + artist + " (" + genre + ", " + album + ")";
                System.out.println("Debug: Adding song to playlist: " + songInfo);
                playlistSongsModel.addElement(songInfo);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private void openPlaylist(String playlistName) {

    DefaultListModel<String> playlistSongsModel = new DefaultListModel<>();
    loadSongsFromPlaylist(playlistSongsModel, playlistName);

    JList<String> playlistSongsList = new JList<>(playlistSongsModel);
    playlistSongsList.setBackground(Color.BLACK);
    playlistSongsList.setForeground(Color.WHITE);
    JScrollPane songsScrollPane = new JScrollPane(playlistSongsList);

    JFrame playlistSongsFrame = new JFrame("Playlist: " + playlistName);
    playlistSongsFrame.setSize(400, 300);
    playlistSongsFrame.setLocationRelativeTo(null);
    playlistSongsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    playlistSongsFrame.add(songsScrollPane);
    playlistSongsFrame.setVisible(true);
}

private void loadPlaylistsFromDatabase(DefaultListModel<String> playlistListModel) {
    // Implement the logic to fetch playlists from your "playlists" table in the database.
    // Adjust the SQL query based on your database structure.

    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM playlists")) {

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String playlistName = resultSet.getString("name");
                playlistListModel.addElement(playlistName);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    private void loadSongsFromDatabase(DefaultListModel<String> musicListModel) {
    try (Connection connection = DriverManager.getConnection(SONGS_JDBC_URL);
         PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM songs")) {

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String artist = resultSet.getString("artist");
                String genre = resultSet.getString("genre");
                String album = resultSet.getString("album");

                musicListModel.addElement(title + " - " + artist + " (" + genre + ", " + album + ")");
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
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
            showAddToPlaylistDialog();
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
