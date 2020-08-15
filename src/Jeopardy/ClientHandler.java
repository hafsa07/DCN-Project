package Jeopardy;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientHandler extends Thread {
    private Socket socket;
    private Semaphore sem,ansSem;
    private JLabel label;
    public DataInputStream inputStream;
    public DataOutputStream outputStream;
    private int score,option;
    private String name;

    private final int QUESTION = 0, STATUS = 1,CONTROL = 2;
    private String data;
    private GameMaster gameMaster;



    public ClientHandler(Socket socket, JLabel label,Semaphore ansSem,GameMaster gameMaster) {
        this.gameMaster = gameMaster;
            this.ansSem = ansSem;
        this.socket = socket;
        this.label = label;

        sem = new Semaphore(0);
        score = 0;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            name = inputStream.readUTF();

        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLabel();


    }
    @Override
    public void run() {
        try {
            while (true) {
                sem.acquire();
                switch(option) {
                    case QUESTION:
                        doQuestion();
                        break;
                    case STATUS:
                        doStatus();
                        break;
                    case CONTROL:
                        break;
                    default:
                        System.out.println("Error");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateLabel() {
        label.setText("Name: "+name+" Score: "+score);
    }
    public void doQuestion() throws Exception{
        outputStream.writeUTF("QUE|"+data);
        String response = inputStream.readUTF();
        if (response.contains("C")) {
            ansSem.acquire();
            if (!gameMaster.answered) {
            	gameMaster.answerRecieved();
            	score++;
            	setStatus("Correct Answer");
            } else {
            	gameMaster.replies++;
            	gameMaster.questionSem.release();
            	setStatus("Question Already Answered");
            }
        } else {
            setStatus("Incorrect Answer");
            gameMaster.replies++;
            gameMaster.questionSem.release();
        }
        updateLabel();
    }

    public void doStatus() throws Exception{
        outputStream.writeUTF("STA|"+data+"|"+score);
        updateLabel();
    }
    public void setStatus(String data) {
        option = STATUS;
        this.data = data;
        sem.release();
    }
    public void setQuestion(String data) {
        option = QUESTION;
        this.data = data;
        sem.release();
    }
}
