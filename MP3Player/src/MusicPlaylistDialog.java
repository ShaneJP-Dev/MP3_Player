import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MusicPlaylistDialog extends JDialog {
    private MusicPlayerGUI musicPlayerGUI;

    //stores all paths to be written to a txt file (when loading a playlist)
    private ArrayList<String> songPaths;

    public MusicPlaylistDialog(MusicPlayerGUI musicPlayerGUI){
        this.musicPlayerGUI = musicPlayerGUI;
        songPaths = new ArrayList<>();

        //config dialog
        setTitle("Create Playlist");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(MusicPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true);
        setLocationRelativeTo(musicPlayerGUI);

        addDialogComponents();
    }

    private void addDialogComponents(){
        // container holds each song path
        JPanel songContainer = new JPanel();
        songContainer.setLayout(new BoxLayout(songContainer, BoxLayout.Y_AXIS));
        songContainer.setBounds((int)(getWidth() * 0.025), 10, (int)(getWidth() * 0.90), (int)(getHeight() * 0.75));
        add(songContainer);

        //add song button
        JButton addSongButton = new JButton("Add");
        addSongButton.setBounds(60, (int)(getHeight() * 0.80), 100, 25);
        addSongButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addSongButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //open file explorer
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter((new FileNameExtensionFilter("MP3", "mp3")));
                jFileChooser.setCurrentDirectory(new File("src/assets"));
                int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

                File selectedFile = jFileChooser.getSelectedFile();
                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null){
                    JLabel filePathLabel = new JLabel(selectedFile.getPath());
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    //add to the list
                    songPaths.add(filePathLabel.getText());

                    //ad a container
                    songContainer.add(filePathLabel);

                    //refreshes dialog showing newly added JLabel
                    songContainer.revalidate();
                }
            }
        });
        add(addSongButton);

        //save playlist button
        JButton savePlaylistButton = new JButton("Save");
        savePlaylistButton.setBounds(215, (int)(getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFileChooser jFileChooser = new JFileChooser();
                    jFileChooser.setCurrentDirectory(new File("src/assets"));
                    int result = jFileChooser.showOpenDialog(MusicPlaylistDialog.this);

                    if(result == JFileChooser.APPROVE_OPTION){
                        //using getSelectedFile() to get reference to the file we about to save
                        File selectedFile = jFileChooser.getSelectedFile();

                        // convert to .txt file if not done already.
                        if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")){
                            selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                        }

                        //create file at designated directory
                        selectedFile.createNewFile();

                        //write song to new file
                        FileWriter fileWriter = new FileWriter(selectedFile);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        //iterate through the songs and write each string to the file
                        //each song will be written in their own row

                        for(String songPath : songPaths){
                            bufferedWriter.write(songPath + "\n");
                        }
                        bufferedWriter.close();

                        //display success dialog
                        JOptionPane.showMessageDialog(MusicPlaylistDialog.this, "Successfully Created Playlist!");

                        //clos dialog
                        MusicPlaylistDialog.this.dispose();
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        });
        add(savePlaylistButton);

    }
}
