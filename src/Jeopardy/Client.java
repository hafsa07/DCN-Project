package Jeopardy;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Client extends JFrame implements ActionListener {
    private Socket socket;
    private JButton btnA,btnB,btnC,btnD;
    private String data;
    private JLabel nameLabel, statusLbl, queLbl,scoreLbl;
    private JPanel centerPanel,BtnPanel,topPanel;
    private String name, score;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String ans;
    private Semaphore sem;
    private String[] que;

    public static void main(String[] args) {
        new Client();
    }

    public Client() {
        name = "HAFSA";
        try {
            socket = new Socket("localhost", 9988);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(name);
            sem = new Semaphore(1);
            btnA = new JButton("Option A");
            btnB = new JButton("Option B");
            btnC = new JButton("Option C");
            btnD = new JButton("Option D");
            btnA.addActionListener(this);
            btnB.addActionListener(this);
            btnC.addActionListener(this);
            btnD.addActionListener(this);
            btnA.setBackground(Color.BLACK);
            btnA.setForeground(Color.WHITE);
            btnB.setBackground(Color.BLACK);
            btnB.setForeground(Color.WHITE);
            btnC.setBackground(Color.BLACK);
            btnC.setForeground(Color.WHITE);
            btnD.setBackground(Color.BLACK);
            btnD.setForeground(Color.WHITE);

            BtnPanel = new JPanel(new FlowLayout());
            BtnPanel.add(btnA);
            BtnPanel.add(btnB);
            BtnPanel.add(btnC);
            BtnPanel.add(btnD);
            setButtonState(false);


            centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.setBackground(Color.BLACK);
            queLbl = new JLabel("GOOD LUCK . YOUR QUESTIONS ARE SHOWN HERE");
            queLbl.setFont(queLbl.getFont().deriveFont(Font.BOLD,23));
            queLbl.setForeground(Color.WHITE);
            queLbl.setBackground(Color.BLACK);
            centerPanel.add(queLbl);

            topPanel= new JPanel(new FlowLayout(FlowLayout.CENTER));
            topPanel.setSize(650,40);
            topPanel.setBackground(Color.black);
            topPanel.setForeground(Color.white);
            nameLabel = new JLabel("NAME: "+name);
            nameLabel.setForeground(Color.white);
            scoreLbl = new JLabel("SCORE: "+score);
            scoreLbl.setForeground(Color.white);
            statusLbl = new JLabel("STATUS");
            statusLbl.setForeground(Color.white);
            topPanel.add(nameLabel);
            System.out.println("\n");
            topPanel.add(scoreLbl);
            topPanel.add(statusLbl);



            setLayout(new BorderLayout());
            setSize(650,400);
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            add(BtnPanel,BorderLayout.SOUTH);
            add(centerPanel,BorderLayout.CENTER);
            add(topPanel, BorderLayout.NORTH);
            setVisible(true);
            while (true) {
                //Wait For Update from master
            	sem.acquire();
                System.out.println("Waiting For Server");
                String rcv = inputStream.readUTF();
                System.out.println("Recieved: " + rcv);
                que = rcv.split("[|]"); //{"Que","question","1","2","3","4",ans}
                System.out.println(que[0]);
                if (que[0].contains("QUE")) {
                    System.out.println("Displaying Question....");
                    ans = que[Integer.parseInt(que[6]) + 2];
                    clearColor();
                    displayQuestion(que);
                    setButtonState(true);

                } else if (que[0].contains("STA")) {
                    statusLbl.setText(que[1]);
                    scoreLbl.setText("Score: " + que[2]);
                    sem.release();
                } else if (que[0].contains("CAT")) {
                	displayQuestion(que);
                	setButtonState(true);
                	sem.release();
                	
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayQuestion(String[] que) {
        queLbl.setText(que[1]);
        btnA.setText(que[2]);
        btnB.setText(que[3]);
        btnC.setText(que[4]);
        btnD.setText(que[5]);
    }
    private void setButtonState(boolean state) {
        btnA.setEnabled(state);
        btnB.setEnabled(state);
        btnC.setEnabled(state);
        btnD.setEnabled(state);
    }

    private void clearColor() {
        btnA.setBackground(Color.BLACK);
        btnA.setForeground(Color.WHITE);
        btnB.setBackground(Color.BLACK);
        btnB.setForeground(Color.WHITE);
        btnC.setBackground(Color.BLACK);
        btnC.setForeground(Color.WHITE);
        btnD.setBackground(Color.BLACK);
        btnD.setForeground(Color.WHITE);

    }
    private void answer(String response,JButton btn) throws Exception {
        if (response.equals(ans)) {
            outputStream.writeUTF("Correct Answer");
            setButtonState(false);
            btn.setBackground(Color.GREEN);
        } else {
            statusLbl.setText("Wrong Answer");
            outputStream.writeUTF("Wrong");
            setButtonState(false);
            btn.setBackground(Color.RED);
        }
        sem.release();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (que[0].equals("CAT"))
        	setButtonState(false);
    	try {
            if (e.getSource() == btnA) {
            	if (que[0].equals("CAT")) {
            		outputStream.writeUTF(btnA.getText());
            	
            	}
                answer(btnA.getText(),btnA);

            } else if (e.getSource() == btnB) {
            	if (que[0].equals("CAT")) {
            		outputStream.writeUTF(btnB.getText());
            	}
                answer(btnB.getText(),btnB);

            }
            else if (e.getSource() == btnC) {
            	if (que[0].equals("CAT")) {
            		outputStream.writeUTF(btnC.getText());
            	}
                answer(btnC.getText(),btnC);

            }
            else if (e.getSource() == btnD) {
            	if (que[0].equals("CAT")) {
            		outputStream.writeUTF(btnD.getText());
            	}
                answer(btnD.getText(),btnD);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
