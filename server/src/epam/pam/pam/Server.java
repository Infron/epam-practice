package epam.pam.pam;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import epam.pam.pam.Common.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;

public class Server {

    public static void main(String[] args) {

        userThread.mapper.setVisibilityChecker(userThread.mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        userThread.mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        userThread.mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);


        try (ServerSocket server = new ServerSocket(10001)) {

            System.out.println("Server starts!");

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("User connected" + clientSocket);
                try {
                    userThread.userThreadList.add(new userThread(clientSocket));
                    userThread.userThreadList.getLast().start();
                } catch (IOException e) {
                    e.printStackTrace();
                    clientSocket.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class userThread extends Thread {
    static LinkedList<userThread> userThreadList = new LinkedList<>();
    static ObjectMapper mapper = new ObjectMapper();
    private static LinkedList<String> userLoginsList = new LinkedList<>();
    private Socket ClientSocket;
    private OutputStream outputmsg;

    userThread(Socket socket) throws IOException {
        ClientSocket = socket;
        outputmsg = ClientSocket.getOutputStream();
    }


    public void run() {
        try {
            InputStream inputmsg = ClientSocket.getInputStream();
            while (true) {
                String newLogin = mapper.readValue(inputmsg, String.class);
                if (userLoginsList.contains(newLogin)) {
                    mapper.writeValue(outputmsg, "no");
                    outputmsg.flush();
                } else {
                    userLoginsList.add(newLogin);
                    mapper.writeValue(outputmsg, "yes");
                    outputmsg.flush();
                    break;
                }
            }


            while (true) {
                outputmsg = ClientSocket.getOutputStream();
                Message newMessage;
                try {
                    newMessage = mapper.readValue(inputmsg, Message.class);
                } catch (Exception e) {
                    break;
                }
                System.out.println(newMessage.login + ":  " + newMessage.text);

                for (var userThread : userThreadList) {
                    userThread.sendMessage(newMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg) {
        try {
            mapper.writeValue(outputmsg, msg);
            outputmsg.flush();
        } catch (SocketException e) {
            int i = userThreadList.indexOf(this);
            userThreadList.remove(i);
            userLoginsList.remove(i);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}