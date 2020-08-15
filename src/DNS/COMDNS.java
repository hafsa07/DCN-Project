package DNS;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class COMDNS implements DNSInterface {

    private ArrayList<String[]> answers;
    private DatagramSocket socket;
    private int port;


    public COMDNS( int port) throws Exception {
        String fileName = "com.txt";
        socket = new DatagramSocket(port);
        answers = new ArrayList<>();
        this.port = port;
        // Read File
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String temp;
        while ((temp = reader.readLine()) != null) {
            answers.add(temp.split("/"));
        }
        for (String s[] : answers) {
            for (String a: s)
                System.out.print("\t" + a);
            System.out.println();
        }

    }

    public static void main(String[] args) throws Exception {
        //int port = Integer.parseInt(args[0]);
        int port  = 1124;
    	new COMDNS(port).mainLoop();

    }
    public void mainLoop() {
        while (true) {
            try {
                String[] que = getQuery();
                System.out.println("COMDNS(" +port+"): Received Query: " + que[0]);
                String addr = que[0];
                String ans[] = query(addr);
                que[0] = ans[1] + ":" + ans[2];
                System.out.println("COMDNS(" +port+"): Send Query: " + que[0]);
                sendQuery(que);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public String[] query(String addr) throws Exception {
        for (String s[] : answers) {
            if (addr.equals(s[0])) {
                return s;
            }
        }
        String noAns[] = {"-1","-",""};
        return noAns;
    }
    private String[] getQuery() throws Exception {
        byte[] buff = new byte[3000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        socket.receive(inPacket);
        String[] que = new String[3];
        que[0] = new String(buff,0,inPacket.getLength(), "US-ASCII");
        que[1] = String.valueOf(inPacket.getAddress());
        que[2] = String.valueOf(inPacket.getPort());
        return que;

    }
    private void sendQuery(String[] que) throws Exception {
        byte[] buff = que[0].getBytes(StandardCharsets.US_ASCII);
//        System.out.println(que[0] + "\t" + que[1]);
        DatagramPacket outPacket = new DatagramPacket(buff,buff.length);
        outPacket.setAddress(InetAddress.getLocalHost());
        outPacket.setPort(Integer.parseInt(que[2]));
        System.out.println("COMDNS: Sending: " + new String(buff,0,buff.length,StandardCharsets.US_ASCII));
        socket.send(outPacket);
    }


}
