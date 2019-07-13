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

public class Server {
    private static Socket ClientSocket;

    public static ArrayList<userThread> userThreadList;
    private static ObjectInputStream objectInputStream;
    public static void main(String[] args) throws IOException {
        System.out.println("Server starts!");
        ServerSocket Server = new ServerSocket(10000);
        userThreadList = new ArrayList<>();

        try {
            try {
                while (true) {
                    ClientSocket = Server.accept();
                    objectInputStream = new ObjectInputStream(ClientSocket.getInputStream());
                    System.out.println("User connected");
                    userThreadList.add(new userThread(ClientSocket, objectInputStream));
                }
            } catch (IOException e){
                ClientSocket.close();
            }
        } finally {
            Server.close();
        }
    }
}
/*
class Message implements Serializable {
    String login;
    String msg;

    Message(String log, String mes) {
        this.login = log;
        this.msg = mes;
    }
}*/

class userThread extends Thread{
    private Socket ClientSocket;

    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    userThread(Socket socket, ObjectInputStream in) throws IOException {
        this.ClientSocket = socket;
        this.objectInputStream = in;
        start();
    }

    public void run(){
        try{
            while (true){

                objectOutputStream = new ObjectOutputStream(ClientSocket.getOutputStream());
                Message newMessage = (Message) objectInputStream.readObject();
                if (newMessage.msg.equals("mamka")) break;

                for (userThread i : Server.userThreadList) {
                    i.sendMessage(newMessage);
                }
                //objectOutputStream.close();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void sendMessage(Message msg) {
        try{
            objectOutputStream.writeObject(msg);
            //objectOutputStream.flush();
        } catch (IOException e){
            System.err.println(e);
        }
    }

}
