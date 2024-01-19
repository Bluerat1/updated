import javazoom.jl.player.Player;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javazoom.jl.decoder.JavaLayerException;

public class MusicPlayer implements ActionListener {
    private JLabel songName;
    private JButton select, play, pause, resume, stop;
    private JPanel playerPanel, controlPanel;
    private ImageIcon iconPlay, iconPause, iconResume, iconStop;
    private JFrame frame;
    private JFileChooser fileChooser;
    private File myFile;
    private String filename, filePath;
    private Player player;
    private FileInputStream fileInputStream;
    private BufferedInputStream bufferedInputStream;
    private int totalLength, pauseLength;
    
     private long pausePosition;

    public static void main(String[] args) {
        MusicPlayer musicPlayer = new MusicPlayer();
        musicPlayer.initUI();
        musicPlayer.addActionEvents();
    }
    private boolean isPaused;
    private boolean isAudioPlaying;



public void initUI() {
        //Setting songName Label to center
        songName = new JLabel("", SwingConstants.CENTER);
        //Creating button for selecting a song
        select = new JButton("Select Mp3");
        //Creating Panels
        playerPanel = new JPanel(); //Music Selection Panel
        controlPanel = new JPanel(); //Control Selection Panel
        //Creating icons for buttons
        iconPlay = new ImageIcon("C:\\Users\\Rakk\\Desktop\\play-circle (1).png");
        iconPause = new ImageIcon("C:\\Users\\Rakk\\Desktop\\pause-circle (2).png");
        iconResume = new ImageIcon("C:\\Users\\Rakk\\Desktop\\step-forward.png");
        iconStop = new ImageIcon("C:\\Users\\Rakk\\Desktop\\stop-circle.png");
        
        //Creating image buttons
        play = new JButton(iconPlay);
        pause = new JButton(iconPause);
        resume = new JButton(iconResume);
        stop = new JButton(iconStop);
        //Setting Layout of PlayerPanel
        playerPanel.setLayout(new GridLayout(2, 1));
        //Addings components in PlayerPanel
        playerPanel.add(select);
        playerPanel.add(songName);
        //Setting Layout of ControlPanel
        controlPanel.setLayout(new GridLayout(1, 4));
        //Addings components in ControlPanel
        controlPanel.add(play);
        controlPanel.add(pause);
        controlPanel.add(resume);
        controlPanel.add(stop);
        //Setting buttons background color
        play.setBackground(Color.WHITE);
        pause.setBackground(Color.WHITE);
        resume.setBackground(Color.WHITE);
        stop.setBackground(Color.WHITE);
        
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("C:\\Users"));
        fileChooser.setDialogTitle("Select Mp3");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Mp3 files", "mp3"));

        
        //Initialing the frame
        frame = new JFrame();
        //Setting Frame's Title
        frame.setTitle("DataFlair's Music Player");
        //Adding panels in Frame
        frame.add(playerPanel, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.SOUTH);
        //Setting Frame background color
        frame.setBackground(Color.white);
        frame.setSize(400, 200);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
}

public void addActionEvents() {
    //registering action listener to buttons
    select.addActionListener(this);
    play.addActionListener(this);
    pause.addActionListener(this);
    resume.addActionListener(this);
    stop.addActionListener(this);
}
@Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(select)) {
            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("C:\\Users\\Rakk\\Downloads\\Music"));
            fileChooser.setDialogTitle("Select Mp3");
fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
fileChooser.setFileFilter(new FileNameExtensionFilter("Mp3 files", "mp3"));
            if (fileChooser.showOpenDialog(select) == JFileChooser.APPROVE_OPTION) {
                myFile = fileChooser.getSelectedFile();
                filename = fileChooser.getSelectedFile().getName();
                filePath = fileChooser.getSelectedFile().getPath();
                songName.setText("File Selected : " + filename);
            }
        }
        
         if (e.getSource().equals(play)) {
             
            
             
         if (e.getSource().equals(play)) {
            if (!isAudioPlaying) {
                // Play the audio if it's not currently playing
                playAudio();
            }
            // Set the state to indicate that the audio is playing
            isAudioPlaying = true;
        }  else {
            songName.setText("No File was selected!");
        }
    }

    if (e.getSource().equals(resume)) {
            if (filename != null && isPaused) {
                // Resume from the stored pause position
                resumeAudio();
            } else {
                songName.setText("No File was selected or Audio is not paused!");
            }
        }
   if (e.getSource().equals(pause)) {
            // Pause the audio and store the current position
            pauseAudio();
        }

        if (e.getSource().equals(stop)) {
            // Stop the audio
            stopAudio();
            songName.setText("");
        }
    }

private void playAudio() {
    if (filename != null) {
        Thread playThread = new Thread(runnablePlay);
        playThread.start();
        songName.setText("Now playing: " + filename);
    } else {
        songName.setText("No File was selected!");
    } 
    
}

private void resumeAudio() {
        if (filename != null) {
            Thread resumeThread = new Thread(runnableResume);
            resumeThread.start();
            songName.setText("Resuming: " + filename);
        } else {
            songName.setText("No File was selected!");
        }
    }

private void pauseAudio() {
        if (filename != null && player != null) {
            // Store the current position before pausing
            pausePosition = player.getPosition();
            player.close();
            isPaused = true;
            // Release resources
            releaseResources();
            songName.setText("Paused: " + filename);
        } else {
            songName.setText("No File was selected!");
        }
    }

private void stopAudio() {
    if (player != null) {
        player.close();
        isPaused = false; // reset pause state
        // Release resources
        releaseResources();
    }isAudioPlaying = false;
}

private void releaseResources() {
        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/***
private void releaseResources() {
    try {
        if (fileInputStream != null) {
            fileInputStream.close();
        }
        if (bufferedInputStream != null) {
            bufferedInputStream.close();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
}
***/




Runnable runnablePlay = new Runnable() {
    @Override
    public void run() {
        try {
            fileInputStream = new FileInputStream(myFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            player = new Player(bufferedInputStream);
            totalLength = fileInputStream.available();
            player.play(); // starting music
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JavaLayerException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
};

Runnable runnableResume = new Runnable() {
    @Override
    public void run() {
        try {
            fileInputStream = new FileInputStream(myFile);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            player = new Player(bufferedInputStream);

            // Skip the bytes that have already been played
            if (pauseLength > 0) {
                long skipped = 0;
                while (skipped < pauseLength) {
                    skipped += fileInputStream.skip(pauseLength - skipped);
                }
            }
            player.setPosition(pausePosition);

            // Start playing from the paused position
            player.play();

        } catch (FileNotFoundException e) {
            // Handle file not found exception
            JOptionPane.showMessageDialog(frame, "File not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JavaLayerException | IOException e) {
            // Handle other exceptions
            JOptionPane.showMessageDialog(frame, "Error playing audio: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
};

    
}