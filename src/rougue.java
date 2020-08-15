import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;

public class rougue {
    static DatagramSocket socket;
    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket(9999);
        String[] abc = {"yahoo.com","localhost","1122"};
        sendQuery(abc);
        abc = getQuery();
        System.out.println(abc[0]);
        abc[0] = "yahooabs.com";
        sendQuery(abc);
        abc = getQuery();
        System.out.println(abc[0]);
        abc[0] = "yahoo.com";
        sendQuery(abc);
        abc = getQuery();
        System.out.println(abc[0]);
        abc[0] = "yaho123231";
        sendQuery(abc);
        abc = getQuery();
        System.out.println(abc[0]);
    }
    private static String[] getQuery() throws Exception {
        byte[] buff = new byte[3000];
        DatagramPacket inPacket = new DatagramPacket(buff,buff.length);
        socket.receive(inPacket);
        String[] que = new String[3];
        que[0] = new String(buff,0,inPacket.getLength(), StandardCharsets.US_ASCII);
        que[1] = String.valueOf(inPacket.getAddress());
        que[2] = String.valueOf(inPacket.getPort());
        return que;

    }
    private static void sendQuery(String[] que) throws Exception {
        byte[] buff = que[0].getBytes("US-ASCII");
        DatagramPacket outPacket = new DatagramPacket(buff,buff.length);
        outPacket.setAddress(InetAddress.getLocalHost());
        outPacket.setPort(Integer.parseInt(que[2]));
        socket.send(outPacket);
    }
}
