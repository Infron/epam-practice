import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.Serializable;
import java.net.SocketAddress;
import java.rmi.ServerError;
import java.sql.ClientInfoStatus;
import java.util.ArrayList;
import java.util.LinkedList;

public class Server {
    private static Socket ClientSocket;

    public static LinkedList<userThread> userThreadList;

    public static void main(String[] args) throws IOException {
        System.out.println("Server starts!");
        ServerSocket Server = new ServerSocket(10000);
        userThreadList = new LinkedList<>();

        try {
            while (true) {
                ClientSocket = Server.accept();
                System.out.println("User connected" + ClientSocket);
                try {
                    userThreadList.add(new userThread((ClientSocket)));
                } catch (IOException e) {
                    ClientSocket.close();
                }
            }
        } finally {
            Server.close();
        }
    }
}

class Message implements Serializable {
    String login;
    String msg;

    Message(String log, String mes) {
        this.login = log;
        this.msg = mes;
    }
}

class userThread extends Thread{
    private static Socket ClientSocket;
    private static ObjectInputStream objectInputStream;
    private static ObjectOutputStream objectOutputStream;

    userThread(Socket socket) throws IOException {
        this.ClientSocket = socket;

        start();
    }

    public  void run(){
        try{
            while (true){
                objectInputStream = new ObjectInputStream(ClientSocket.getInputStream());
                objectOutputStream = new ObjectOutputStream(ClientSocket.getOutputStream());
                Message newMessage = (Message) objectInputStream.readObject();
                if (newMessage.msg.equals("mamka")) break;

                for (userThread vr: Server.userThreadList) {
                    vr.sendMessage(newMessage);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void sendMessage(Message msg) {
        try{
                objectOutputStream.writeObject(msg);
                objectOutputStream.flush();
        } catch (IOException e){
            System.err.println(e);
        }
    }

}