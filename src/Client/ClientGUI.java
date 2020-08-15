package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

public class ClientGUI extends JFrame implements ActionListener, KeyListener {
    private JTextField addressBar;
    private String currentServer;
    private JTextArea textArea;
    private JLabel adrLabel;
    private JButton goBtn;
    private JPanel topPanel, centerPanel;
    private JScrollPane textPane;
    private ClientDisplay display;
    private Socket socket;
    private DataOutputStream outStream;
    private Semaphore sem;
    private DatagramSocket UDPSocket;

    public ClientGUI() throws Exception{
        UDPSocket = new DatagramSocket(2211);
        sem = new Semaphore(0);
        setLayout(new BorderLayout());
        topPanel = new JPanel(new FlowLayout());
        centerPanel = new JPanel(new BorderLayout());
        addressBar = new JTextField(30);
        textArea = new JTextArea();
        adrLabel = new JLabel("Address");
        goBtn = new JButton("Go");
        currentServer = "";
        goBtn.addActionListener(this);
        topPanel.add(adrLabel);
        topPanel.add(addressBar);
        topPanel.add(goBtn);
        display = new ClientDisplay(textArea,sem);
        display.start();
        addressBar.addKeyListener(this);
        centerPanel.add(textArea, BorderLayout.CENTER);
        textPane = new JScrollPane(centerPanel);
        textPane.setSize(300,300);
        add(topPanel,BorderLayout.NORTH);
        add(textPane,BorderLayout.CENTER);
        setSize(500,500);
        setTitle("Browser");
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private String[] breakAddress(String addr) throws Exception {
        if (addr.contains("/")) {
            String[] ans = addr.split("/");
            if (ans.length != 2) throw new Exception();

            if (ans[1].length() == 0 || ans[0].length() == 0) throw new Exception();
            else return ans;
        } else throw new Exception();
    }




    public static void main(String[] args) throws Exception {
        new ClientGUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == goBtn) {
           submit();
        }
    }

    public void submit() {
        try {
            if (addressBar.getText().length() != 0) {
                String ans[] = breakAddress(addressBar.getText());
                String addr = ans[0];
                String file = ans[1];
                System.out.println(addr + "\t" + file);

                if (addr.equals(currentServer)) {
                    //outStream.writeUTF(file);
                    textArea.setText("");
                    outStream.writeUTF(file);
                } else {
                    currentServer = addr;
                    if (socket != null) socket.close();
                    // TODO : GET IP AND PORT FROM DNS
                    // FOR TESTING PURPOSES IP IS "LOCALHOST" PORT IS 1234
                    textArea.setText("");
                    String answer = makeDNSQuery(addr);
                    if (answer != null) {
                        String[] addrPort = answer.split(":"); // {"localhost",1111}
                        socket = new Socket("localhost", Integer.parseInt(addrPort[1]));
                        outStream = new DataOutputStream(socket.getOutputStream());
                        outStream.writeUTF(file);
                        display.socket = socket;
                        display.inStream = new DataInputStream(socket.getInputStream());
                        sem.release();
                    } else {
                        textArea.setText("DNS Error \nCannot Resolve Address");
                    }
                }

            } else {
                JOptionPane.showMessageDialog(null,"Enter Address");
            }

        }
        catch (ConnectException ex) {
            textArea.setText(ex.getMessage());

        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,"Enter Correct Address");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
    private String[] getQuery() throws Exception {
        byte[] buff = new byte[3000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        UDPSocket.receive(inPacket);
        String[] que = new String[3];
        que[0] = new String(buff,0,inPacket.getLength(), StandardCharsets.US_ASCII);
        que[1] = String.valueOf(inPacket.getAddress());
        que[2] = String.valueOf(inPacket.getPort());
        return que;

    }
    private void sendQuery(String[] que) throws Exception {
        byte[] buff = que[0].getBytes(StandardCharsets.US_ASCII);
        DatagramPacket outPacket = new DatagramPacket(buff,buff.length);
        outPacket.setAddress(InetAddress.getLocalHost());
        outPacket.setPort(Integer.parseInt(que[2]));
        UDPSocket.send(outPacket);
    }
    private String makeDNSQuery(String addr) throws Exception{
        String[] que = {addr,"localhost","1122"};
        sendQuery(que);
        //Local DNS Will send back with answer
        String[] ans = getQuery();
        if (ans[0].contains("-")) return null;
        else return ans[0];
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            submit();
        }
    }
}
