import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static ObjectOutputStream objectOutputStream;
    static readMessageThread messageReader;

    public static void main(String[] args) {
        String login = args[0];
        Scanner scanner = new Scanner(System.in);
        try {
            Socket Client = new Socket("localhost", 10000);
            try {
                messageReader = new readMessageThread(Client);
                messageReader.start();

                while (true) {
                    String tt = scanner.nextLine();
                    Message msg = new Message(login, tt);
                    objectOutputStream = new ObjectOutputStream(Client.getOutputStream());
                    objectOutputStream.writeObject(msg);
                }
            } finally {
                objectOutputStream.close();
            }
        } catch (Exception e) {
            System.err.println(e);
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

class readMessageThread extends Thread {
    private ObjectInputStream objectInputStream;
    private Socket socket;

    readMessageThread(Socket sucker) throws IOException {
        this.socket = sucker;
    }

    @Override
    public void run() {
        try {
            while (true) {
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                Message newMessage = (Message) objectInputStream.readObject();
                if (newMessage.msg.equals("mamka")) break;
                System.out.println(newMessage.login + "--said--" + newMessage.msg);
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
