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
import java.util.LinkedList;

public class Server {

    static LinkedList<userThread> userThreadList = new LinkedList<>();
    static LinkedList<String> userLoginsList = new LinkedList<>();
    static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        mapper.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);


        try (ServerSocket server = new ServerSocket(10001)) {

            System.out.println("Server starts!");

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("User connected" + clientSocket);
                try {
                    userThreadList.add(new userThread(clientSocket));
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
    private Socket ClientSocket;
    private OutputStream outputmsg;

    userThread(Socket socket) throws IOException {
        ClientSocket = socket;
        outputmsg = ClientSocket.getOutputStream();
        start();
    }

    public void run() {
        try {
            boolean flag = true;
            boolean flaggg = true;
            String str;

            while (flag) {
                InputStream inputmsg = ClientSocket.getInputStream();
                String newLogin = Server.mapper.readValue(inputmsg, String.class);
                if (!Server.userLoginsList.isEmpty())
                {
                    for (String ss : Server.userLoginsList)
                        if (ss.equals(newLogin)) {
                            flaggg = false;
                            break;
                        }

                    if (!flaggg) {
                        Server.mapper.writeValue(outputmsg, str = "no");
                        outputmsg.flush();
                    } else {
                        Server.userLoginsList.add(newLogin);
                        Server.mapper.writeValue(outputmsg, str = "yes");
                        outputmsg.flush();
                        flag = false;
                    }
                } else {
                    Server.userLoginsList.add(newLogin);
                    str = "yes";
                    Server.mapper.writeValue(outputmsg, str);
                    outputmsg.flush();
                    flag = false;
                }
                flaggg = true;
            }

            while (true) {
                InputStream inputmsg = ClientSocket.getInputStream();
                outputmsg = ClientSocket.getOutputStream();
                Message newMessage = Server.mapper.readValue(inputmsg, Message.class);
                System.out.println(newMessage.login + ":  " + newMessage.text);

                for (userThread userThread : Server.userThreadList) {
                    userThread.sendMessage(newMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Message msg) {
        try {
            Server.mapper.writeValue(outputmsg, msg);
            outputmsg.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}