import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadPoolExecutor;

public class MusicPlayer extends PlaybackListener {
    //used to update the isPaused
    private static final Object playSignal = new Object();
    //need reference to update gui
    private MusicPlayerGUI musicPlayerGUI;
    //string song details
    private Song currentSong;
    public Song getCuttentSong(){
        return currentSong;
    }

    private ArrayList<Song> playlist;

    //need to keep track of the index of the playlist we in
    private int currentPlaylistIndex;

    //use JLayer to create AdvancePlayer obj which will handle playing music
    private AdvancedPlayer advancedPlayer;

    //pause boolean flag to show if player has been paused
    private boolean isPaused;

    //boolean flag to tell when the song is finished
    private boolean songFinished;

    private boolean pressedNext, pressedPrev;

    //stores the last frame of the playback(used for pausing and resuming)
    private int currentFrame;
    public void setCurrentFrame(int frame){
        currentFrame = frame;
    }

    // track how many milliseconds have passed since playing the song (used to update slider)
    private int currentTimeInMilliseconds;
    public void setCurrentTimeMilli(int timeMilli){
        currentTimeInMilliseconds = timeMilli;
    }

    //constructor
    public MusicPlayer(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;

    }

    public void loadSong(Song song){
        currentSong = song;
        playlist = null;

        //play current song if not null
        if(currentSong != null){
            //rest frame
            currentFrame = 0;

            //reset current time in milli
            currentTimeInMilliseconds = 0;

            //update gui
            musicPlayerGUI.setPlaybackSliderValues(0);

            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        //stores paths from text file to the playlist array list
        try{
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //read each line form the file and store it in a songPath variable
            String songPath;
            while((songPath = bufferedReader.readLine()) != null){
                //create song object based on song path
                Song song = new Song(songPath);

                //add to playlist arraylist
                playlist.add(song);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(playlist.size() > 0){
            //reset playback slider
            musicPlayerGUI.setPlaybackSliderValues(0);
            currentTimeInMilliseconds = 0;

            //update current song to first song in the playlist
            currentSong = playlist.get(0);

            //start from the beginning frame
            currentFrame = 0;

            //update gui
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            //start song
            playCurrentSong();
        }
    }

    public void pauseSong(){
        if(advancedPlayer != null){
            //update isPaused flag
            isPaused = true;
            //then player stop
            stopSong();
        }
    }

    public void stopSong(){
        if(advancedPlayer != null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void nextSong(){
        //no need to go to next song if there is no playlist
        if(playlist == null) return;

        //check to see if we reached the end of the playlist, if so do nothing
        if(currentPlaylistIndex + 1 > playlist.size() - 1) return;

        pressedNext = true;

        //stop the song if possible
        if(!songFinished){
            stopSong();
        }

        //increase current playlist index
        currentPlaylistIndex ++;

        //update current song
        currentSong = playlist.get(currentPlaylistIndex);

        //update current song
        currentFrame = 0;

        //reset current time in milli
        currentTimeInMilliseconds = 0;

        //update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        //play the song
        playCurrentSong();
    }

    public void prevSong(){
        //no need to go to next song if there is no playlist
        if(playlist == null) return;

        //check if we can go to previous song
        if(currentPlaylistIndex - 1 < 0) return;

        pressedPrev = true;

        //stop the song if possible
        if(!songFinished){
            stopSong();
        }

        //increase current playlist index
        currentPlaylistIndex --;

        //update current song
        currentSong = playlist.get(currentPlaylistIndex);

        //update current song
        currentFrame = 0;

        //reset current time in milli
        currentTimeInMilliseconds = 0;

        //update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        //play the song
        playCurrentSong();
    }

    public void playCurrentSong() {
        if(currentSong == null) return;
        try{
            //read mp3 audio file
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            //create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);
            
            //start music
            startMusicThread();

            //start playback slider
            startPlaybackSliderThread();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(isPaused){
                        synchronized (playSignal){
                            //update flag
                            isPaused = false;

                            //notify the other thread to continue
                            playSignal.notify();
                        }

                        //resume music
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    }else {
                        //play music
                        advancedPlayer.play();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //update slider
    private void startPlaybackSliderThread(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                if(isPaused){
                    try {
                        //wait till get notified then continue
                        //makes sure isFalse flag updates to false before continuing
                        synchronized (playSignal){
                            playSignal.wait();
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                while (!isPaused && !songFinished && !pressedNext && !pressedPrev){
                    try{
                        //increment time in milliseconds
                        currentTimeInMilliseconds++;

                        //calculate into frame seconds
                        int calculatedFrame = (int) ((double) currentTimeInMilliseconds * 1.82 * currentSong.getFrameRatePerMillisecond());

                        //update gui
                        musicPlayerGUI.setPlaybackSliderValues(calculatedFrame);

                        //mimic 1 millisecond using thread.sleep
                        Thread.sleep(1);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        //this method is called at the beginning of a song
        System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        //this method is called whn the song is stopped or app is closed
        System.out.println("Playback Finished");
        if(isPaused){
            currentFrame = (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMillisecond());
        }else{
            //if user clicks next or previous rest of code does not need to run
            if(pressedNext || pressedPrev) return;
            //when song ends
            songFinished = true;

            if(playlist == null){
                //update gui
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            }else{
                //last song in playlist
                if(currentPlaylistIndex == playlist.size() - 1){
                    //update gui
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                }else{
                    //go to next song in the playlist
                    nextSong();
                }
            }
        }

    }
}
