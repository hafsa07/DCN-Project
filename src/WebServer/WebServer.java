package WebServer;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class WebServer {
    public static void main (String[] args) throws Exception
    {

//        int port = Integer.parseInt(args[0]);

        Socket socket;
        ServerSocket listenSocket = new ServerSocket(1112);
        while (true) {
            socket = listenSocket.accept();
            new ServerClient(socket).run();
        }
    }






}
