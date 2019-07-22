package epam.pam.pam;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import epam.pam.pam.Common.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;


public class Client {

    static ObjectMapper mapper = new ObjectMapper();
    private static OutputStream outputmsg;
    private static readMessageThread messageReader;

    public static void main(String[] args) throws IOException {


        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));


        mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

        Scanner scanner = new Scanner(System.in);
        String login;

        try {

            Socket ClientSocket = new Socket("localhost", 10001);

            outputmsg = ClientSocket.getOutputStream();
            try {
                System.out.println("Enter your name: ");

                while (true) {
                    String newLog = scanner.nextLine();
                    mapper.writeValue(outputmsg, newLog);
                    outputmsg.flush();

                    InputStream inputmsg = ClientSocket.getInputStream();
                    if (mapper.readValue(inputmsg, String.class).equals("no")) {
                        System.out.println("Choose another name");
                    } else {
                        login = newLog;
                        System.out.println("Welcome, " + login);
                        break;
                    }
                }

                messageReader = new readMessageThread(ClientSocket);

                while (true) {
                    String text = scanner.nextLine();
                    Message msg = new Message();
                    msg.login = login;
                    msg.text = text;
                    try {
                        mapper.writeValue(outputmsg, msg);
                        outputmsg.flush();
                    } catch (SocketException e) {
                        break;
                    }
                }
            } finally {
                outputmsg.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class readMessageThread extends Thread {
    private Socket socket;

    readMessageThread(Socket sucker) throws IOException {
        this.socket = sucker;
        start();
    }

    @Override
    public void run() {
        try {
            InputStream inputmsg = socket.getInputStream();
            while (true) {
                Message newMessage;
                try {
                    newMessage = Client.mapper.readValue(inputmsg, Message.class);
                    System.out.println(newMessage.login + ":  " + newMessage.text);
                } catch (MismatchedInputException e) {
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
