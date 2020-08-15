package DNS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class RootDNS implements DNSInterface {
    private DatagramSocket socket;
    private ArrayList<String[]> answers;

    public RootDNS() throws Exception {
        socket = new DatagramSocket(1123);
        answers = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader("root.txt"));
        String temp;
        while ((temp = reader.readLine()) != null) {
            answers.add(temp.split("/"));
        }
        System.out.println("ROOT DNS Read Entries: ");
        for (String s[]: answers)
            System.out.println(s[0]+"\t"+s[1]+"\t"+s[2]);
    }

    public static void main(String[] args) throws Exception{
        new RootDNS().mainLoop();
    }

    public void mainLoop() {
        while (true) {
            try {
                String query[] = getQuery();
                System.out.println("ROOT DNS: Received Query: " + query[0]);
                String addr = query[0];
                String[] ans = query(addr);
                query[0] = ans[1] + ":" + ans[2];
                System.out.println("ROOT DNS: Reply: " + query[0]);
                sendQuery(query);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String[] query(String addr) throws Exception {
        for (String ans[] : answers) {
            if (ans[0].contains(addr)) return ans;
        }
        String[] toR = {"","-","-"};
        return toR;
    }
    private String[] getQuery() throws Exception {
        byte[] buff = new byte[3000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        socket.receive(inPacket);
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
        socket.send(outPacket);
    }
}
