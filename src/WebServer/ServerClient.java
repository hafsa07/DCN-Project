package WebServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerClient extends Thread {
    Socket socket;
    DataInputStream inputStream;
    DataOutputStream outputStream;

    public ServerClient(Socket socket) throws Exception {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public ArrayList<String> readFile(String fileName) {
        ArrayList<String> ans = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) ans.add(line);
            reader.close();
        } catch (FileNotFoundException e) {
            ans.add("ERROR 404: File Not Found");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ans;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String file = inputStream.readUTF();
                ArrayList<String> read = readFile(file);
                for (String a: read) {
                    outputStream.writeUTF("\n"+a);
                }

            } catch (EOFException e) {
                break;
            }
            catch (Exception e) {
                break;
            }
        }
    }
}
