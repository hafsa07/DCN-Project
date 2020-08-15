package Jeopardy;

//import com.sun.org.apache.xml.internal.security.utils.JDKXPathAPI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.DefaultMenuLayout;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;

public class GameMaster extends JFrame implements ActionListener {
    boolean gameStarted;
    public Semaphore ansSem,questionSem;
    public int replies;
    public boolean answered;
    private boolean ask;
    ArrayList<ClientHandler> clients;
    ArrayList<String> questions;
    public static void main(String[] args) {
        try {
            new GameMaster();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public GameMaster() throws Exception {
        answered = false;
        questionSem = new Semaphore(1);
        questions = readQuestions();
        clients = new ArrayList<>();
        this.ansSem = new Semaphore(0);
        setLayout(new BorderLayout());
        gameStarted = false;
        //Create Side Panel
        JPanel userPanel = new JPanel(new GridLayout(5,2));
        userPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        JLabel[] labels = new JLabel[10];
        JPanel[] labelPanel = new JPanel[10];
        for (int i = 0 ; i < 10; i++) {
            labels[i] = new JLabel();
            labels[i].setText("PLAYER NOT FOUND\t" + " | " + "\t SCORE: 0");
            labelPanel[i]  = new JPanel(new FlowLayout());
            labelPanel[i].setBackground(Color.BLACK);
            labels[i].setForeground(Color.WHITE);
            labelPanel[i].add(labels[i]);
            userPanel.add(labelPanel[i]);
        }
        add(userPanel,BorderLayout.CENTER);


        JPanel controlPanel = new JPanel(new GridLayout(1,3));
        JButton startBtn = new JButton("Start");

        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(Color.black);
        startBtn.setOpaque(false);
        startBtn.addActionListener(this);
       // JLabel statusFixl =new JLabel("status");
        //statusFixl.setHorizontalAlignment(SwingConstants.LEFT);
        JLabel statuslabel = new JLabel("waiting");
        statuslabel.setForeground(Color.white);

        controlPanel.setBackground(Color.black);
        controlPanel.add(startBtn);
        //controlPanel.add(statusFixl);
        controlPanel.add(statuslabel);
        controlPanel.add(startBtn);


        add(controlPanel,BorderLayout.SOUTH);
        this.setSize(700,200);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        ServerSocket listenSocket = new ServerSocket(9988);
        Socket tempSock;
        for (int i = 0; i < 3; i++) {
            Socket socket = listenSocket.accept();
            tempSock = socket;
            if (!gameStarted) {
                //TODO: Make Instance of Client Class And Start It;
                ClientHandler handle = new ClientHandler(socket,labels[i],ansSem,this);
                handle.start();
                clients.add(handle);
                System.out.println("Client Started");

            } else {
                System.out.println("GAME MASTER: Rejected Client Because Game Already Started");
            }
        }
       


    }
    public void doQuestion(String data) {
        answered = false;
        ansSem.drainPermits();
        ansSem.release();
        for (ClientHandler c: clients) {
            c.setQuestion(data);
        }
    }

    public void answerRecieved() {
        answered = true;
        replies++;
        ansSem.release(10);
        questionSem.release();
    }
    
    public void startGame() throws Exception {
    	if (clients.size() == 0) {
    		JOptionPane.showMessageDialog(null, "There must be clients");
    		return;
    	}
    	gameStarted = true;
    	/* Select Category */ /* TODO */
    	String category = "Category?|General|Science|Pakistan|History|";
    	clients.get(0).outputStream.writeUTF("CAT|"+category);
    	String res = clients.get(0).inputStream.readUTF();
    	System.out.println("Category Chosen: " + res);
    	for (String qs: questions) {
            replies = 0;
            System.out.println("Asking: " + qs);
            doQuestion(qs);
            while (replies != clients.size()) {
                System.out.println(replies);
                questionSem.acquire();
                System.out.println(replies);
            }
        }
    }

    public static ArrayList<String> readQuestions() {
        ArrayList<String> toR= new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("ques.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                toR.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return toR;
    }
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		try {
			startGame();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
