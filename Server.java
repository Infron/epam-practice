import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static Socket ClientSocket;
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;

    public static void main(String[] args) throws IOException {
        System.out.println("Server starts!");
        ServerSocket Server = new ServerSocket(10000);
        try {
            while (true) {
                ClientSocket = Server.accept();
                try {
                    objectInputStream = new ObjectInputStream(ClientSocket.getInputStream());
                    Message newMessage = (Message) objectInputStream.readObject();
                    if (newMessage.msg.equals("mamka")) break;

                    objectOutputStream = new ObjectOutputStream(ClientSocket.getOutputStream());
                    objectOutputStream.writeObject(newMessage);
                    objectOutputStream.flush();
                    System.out.println(newMessage.login + " --said-- " + newMessage.msg);
                } catch (Exception e) {
                    System.err.println(e);
                }
            }
        } finally {
            ClientSocket.close();
            objectInputStream.close();
            objectOutputStream.close();
        }
    }
}
