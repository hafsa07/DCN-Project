package Client;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientDisplay extends Thread {
    private JTextArea textArea;
    public  Socket socket;
    public DataInputStream inStream;
    boolean running;
    private Semaphore sem;

    public ClientDisplay(JTextArea t, Semaphore sem) {
        textArea = t;
        running = true;
        this.sem = sem;
    }

    @Override
    public void run(){
        try {
            sem.acquire();
            while (running) {
                try {
                        textArea.append(inStream.readUTF());

                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket (Socket s) {
        socket = s;
        try {
            inStream = new DataInputStream(s.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        running = false;
        socket = null;
    }
}