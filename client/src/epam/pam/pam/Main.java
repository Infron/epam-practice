package epam.pam.pam;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.pam.pam.Common.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;


public class Main {

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

        System.out.println("Enter your name.");
        String login = scanner.nextLine();
        System.out.println("Hi, " + login + "! Enter your message.");





        try {

            Socket ClientSocket = new Socket("localhost", 10001);

            outputmsg = ClientSocket.getOutputStream();

            try {
                messageReader = new readMessageThread(ClientSocket);

                while (true) {
                    String text = scanner.nextLine();
                    Message msg = new Message();
                    msg.login = login;
                    msg.text = text;
                    mapper.writeValue(outputmsg, msg);
                    outputmsg.flush();
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

                newMessage = Main.mapper.readValue(inputmsg, Message.class);

                System.out.println(newMessage.login + ":  " + newMessage.text);

                if (newMessage.login.equals("mamka")) {
                    return;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
