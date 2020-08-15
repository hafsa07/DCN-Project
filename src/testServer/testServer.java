package testServer;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class testServer {
    public static void main(String[] args) throws Exception {
        ServerSocket listen = new ServerSocket(12345);
        Socket sock = listen.accept();
        DataOutputStream outputStream = new DataOutputStream(sock.getOutputStream());
        while (true) {
            sleep(500);
            System.out.println("Sending");
            outputStream.writeUTF("Hello\n");
        }
    }
}
