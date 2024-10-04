import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {
    //colour configurations
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer  musicPlayer;

    //allows us to use file explorer
    private JFileChooser jFileChooser;

    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI(){
        //calls JFrame constructor to config GUI with title header "Music Player"
        super("Music Player");

        //set width and height
        setSize(400, 600);

        //end process when app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //Launch app on center of screen
        setLocationRelativeTo(null);

        //prevent app from being resized
        setResizable(false);

        //set layout to null to allow control of (x, y) coordinates
        //and also width and height
        setLayout(null);

        //change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();

        //set default path for file explorer
        jFileChooser.setCurrentDirectory(new File("scr/assets"));

        //filter file chooser to only see .mp3 file
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();
    }

    private void addGuiComponents() {
        //add toolbar
        addToolbar();

        // load record image
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        // song title
        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        //song artist
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        //playback slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                //when user is holing the tick song should pause
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                //play when user drops tick
                JSlider source = (JSlider) e.getSource();

                //get value from where the user want play back
                int frame = source.getValue();

                //update the frame in the music player to this frame
                musicPlayer.setCurrentFrame(frame);

                //update the time in milli
                musicPlayer.setCurrentTimeMilli((int) (frame / (1.82 * musicPlayer.getCuttentSong().getFrameRatePerMillisecond())));

                //resume the song
                musicPlayer.playCurrentSong();

                //toggle pause and off button
                enablePauseButtonDisablePlayButton();

            }
        });
        add(playbackSlider);

        // playback buttons (previous, play, next)
        addPlaybackBtns();

    }



    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0,0,getWidth(), 20);

        //prevent toolbar from being moved
        toolBar.setFloatable(false);

        //add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // menu for the song
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        //add load song item to songMenu
        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //an integer is returned so we know what the user did
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectFile = jFileChooser.getSelectedFile();
            //This checks if the user clicked "open" button
            if(result == JFileChooser.APPROVE_OPTION && selectFile != null){
                    //create song obj
                    Song song = new Song(selectFile.getPath());

                    //load song
                    musicPlayer.loadSong(song);

                    // update song title and artist
                    updateSongTitleAndArtist(song);

                    //playback slider
                    updatePlaybackSlider(song);

                    //toggle on pause and play button
                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);

        //playlist menu
        JMenu playListMenu = new JMenu("Playlist");
        menuBar.add(playListMenu);

        //add items to playlist menu
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //load music player dialog
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playListMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                jFileChooser.setCurrentDirectory(new File("src/assets"));

                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    //stop music
                    musicPlayer.stopSong();

                    //load song
                    musicPlayer. loadPlaylist(selectedFile);

                }
            }
        });
        playListMenu.add(loadPlaylist);

        add(toolBar);
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 432, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        //previous button
        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //got to previous song
                musicPlayer.prevSong();
            }
        });
        playbackBtns.add(prevButton);

        //play button
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //toggle off play button and pause
                enablePauseButtonDisablePlayButton();

                //play or resume current song
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);

        //pause button
        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //toggle off pause button on play button
                enablePlayButtonDisablePauseButton();

                //pause the song
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        //next button
        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //go to next song
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    //this will update the slider form the music player class
    public void setPlaybackSliderValues(int frame){
        playbackSlider.setValue(frame);

    }

    public void updateSongTitleAndArtist(Song song){
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    public void updatePlaybackSlider(Song song){
        //update max count for slider
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        //create the song length label
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        //beginning 00:00
        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        //end will vary depending on song
        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    public void enablePauseButtonDisablePlayButton(){
        //retrieve reference to play button
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        //turn off play button
        playButton.setVisible(false);
        playButton.setVisible(false);

        //turn on pause button
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    public void enablePlayButtonDisablePauseButton(){
        //retrieve reference to play button
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        //turn on play button
        playButton.setVisible(true);
        playButton.setVisible(true);

        //turn off pause button
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath){
        try{
            //read image file from given path
            BufferedImage image = ImageIO.read(new File(imagePath));

            //returns image icon so that the component can render tha image
            return new ImageIcon(image);

        }catch(Exception e){
            e.printStackTrace();
        }
        //could not find resource
        return null;
    }
}
