package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField; 

class UI extends JFrame {
    
    private static final long serialVersionUID = 1L;

    private JButton loginButton;
    private JLabel statusLabel;
    
    public UI() {
        super("Ask.fm backup (r1)");
        this.setSize(450, 120);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel panel = new JPanel();    
        this.add(panel);
        
        placeComponents(panel);
    }
    
    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("@username");
        userLabel.setBounds(10,20,80,25);
        panel.add(userLabel);
        
        statusLabel = new JLabel(">");
        statusLabel.setBounds(10,60, 220,25);
        panel.add(statusLabel);
        
        JTextField userText = new JTextField(20);
        userText.setBounds(100,20, 330,25);
        panel.add(userText);

        loginButton = new JButton("Download");
        loginButton.setBounds(330, 60, 100, 25);
        panel.add(loginButton);
        
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String username = userText.getText();
                
                if(username != null && !username.isEmpty()){
                    statusLabel.setText("Please, wait...");
                    loginButton.setEnabled(false);
                    download(username);
                }
                else{
                    statusLabel.setText("Enter @username");
                    loginButton.setEnabled(true);
                }
            }
        });
    }
    
    private void download(String username){
        ProfileParser parser = new ProfileParser();
        
        if(parser.checkUser(username)){
            statusLabel.setText("Download in progress, wait.");
            
            long startTime = System.currentTimeMillis();
            ArrayList<Answer> answers = parser.parse(username);
            long timeSpent = System.currentTimeMillis() - startTime;
        
            writeResult(username, answers);
        
            System.out.println("total: " + answers.size());
            System.out.println("time: " + (timeSpent/1000));
            
            statusLabel.setText("Done. Total " + answers.size() + " answers in " + (timeSpent/1000) + " sec.");
            loginButton.setEnabled(true);
        }
        else{
            statusLabel.setText("Connection error");
            loginButton.setEnabled(true);
        }
    }
    
    private void writeResult(String username, ArrayList<Answer> answers) {
        File file = new File(username + ".txt");
        if(file.exists()){
            file.delete();
        }
        
        try {
            file.createNewFile();
            PrintWriter out = new PrintWriter(file);
            
            for(int i = 0; i < answers.size(); ++i){
                Answer answer = answers.get(i);
                out.println("----------------------------");
                out.println("Q: " + answer.textQuestion);
                out.println("A: " + answer.textAnswer);
                out.println("https://ask.fm" + answer.textHref);
            }
            
            out.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}