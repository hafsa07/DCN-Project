package DNS;

import javax.xml.crypto.Data;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class LocalDNS implements DNSInterface {
    private ArrayList<String[]> cache;
    private DatagramSocket socket;
    final String ROOTDNS = "localhost";
    final int ROOTPORT = 1123;

    public static void main(String[] args) {
        new LocalDNS().mainLoop();
    }

    public void mainLoop() {
        // Local DNS will run on port 1122
        try {
            socket = new DatagramSocket(1122);
            // Init Our Cache
            cache = new ArrayList<>();
            System.out.println("LOCAL DNS: Started");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Main Loop Listens for a query, resolves it and sends it back
        while (true) {
            try {
                String[] que = getQuery();
                String query = que[0];
                System.out.println("LOCAL DNS: Query: " + que[0]);
                boolean hit = false;
                for (String s[]: cache) {
                    if (s[0].equals(que[0])) {
                        que[0] = s[1];
                        System.out.println("LOCAL DNS: Cache Hit");
                        hit = true;
                        break;
                    }
                }
                if (!hit) {
                    String[] ans = query(que[0]);
                    que[0] = ans[0] + ":" + ans[1];
                    if (ans[0].equals("-")) {
                        System.out.println("Query did not resolve");
                    }
                    else {
                        String[] entry = {query, que[0]};
                        cache.add(entry);
                    }
                }
                System.out.println("Query Answer: " + que[0]);
                sendQuery(que);

            } catch (Exception e) {
                e.printStackTrace();
            }
            ;
        }
    }
    @Override
    public String[] query(String addr) throws Exception {
        // First of all make a query to the root dns
        String rootAns[] = rootQuery(addr);
        System.out.println("Root DNS Answered: " + rootAns[0] + ":" + rootAns[1]);
        //After receving the answer making the final query to the .com server
        if (rootAns[0].equals("-")) {
            String toR[] = {"-","-"};
            System.out.println("Root DNS Returened -");
            return toR;
        }
        return comQuery(addr,rootAns);
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

    private String[] rootQuery(String query) throws Exception {
        byte[] buff = query.getBytes(StandardCharsets.US_ASCII);
        DatagramPacket outPacket = new DatagramPacket(buff,buff.length);
        outPacket.setAddress(InetAddress.getLocalHost());
        outPacket.setPort(ROOTPORT);
        socket.send(outPacket);
        buff = new byte[1000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        socket.receive(inPacket);
        String ans = new String(buff,0,inPacket.getLength(),StandardCharsets.US_ASCII);
        return ans.split(":");
    }

    private String[] comQuery(String query, String[] addr) throws Exception {
        // First of all create packet to send to com dns
        byte[] buff = query.getBytes(StandardCharsets.US_ASCII);
        DatagramPacket outPacket = new DatagramPacket(buff,buff.length);
        outPacket.setPort(Integer.parseInt(addr[1]));
        outPacket.setAddress(InetAddress.getLocalHost());
        socket.send(outPacket);
        // This will result in comDNS sending us the final resolved query
        buff = new byte[1000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        socket.receive(inPacket);
        String ans = new String(buff,0,inPacket.getLength(),StandardCharsets.US_ASCII);
        return ans.split(":");
    }


}
