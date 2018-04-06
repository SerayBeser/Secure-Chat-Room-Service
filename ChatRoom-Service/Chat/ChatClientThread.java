package Chat;
/*
 * Created 2/16/2003 by Ting Zhang
 * Part of implementation of the ChatClient to receive
 * all the messages posted to the chat room.
 */


import java.net.*;
import java.io.*;
import javax.swing.JTextArea;


public class ChatClientThread extends Thread {

    private ChatClient chatClient;
    private JTextArea _outputArea;
    private Socket _socket;

    ChatClientThread(ChatClient client) {

        super( "ChatClientThread" );
        chatClient = client;
        _socket = client.getSocket();
        _outputArea = client.getOutputArea();
    }

    public void run() {
        String msg;
        Message message;

        while (chatClient.exists) try {

            message = (Message) chatClient.in.readObject();
            if ((msg = message.verifyAndGetMessage( chatClient.roomKey )) != null) {
                consumeMessage( msg + " \n" );
            }
        } catch (IOException | ClassNotFoundException e) {

            System.out.println( "[INFO]: Client left room." );
            //e.printStackTrace();
        }
        try {

            _socket.close();

        } catch (IOException e) {

            System.out.println( "[ERROR]: ChatClientThread error: " + e.getMessage() );
            e.printStackTrace();
        }
    }

    private void consumeMessage(String msg) {

        if (msg != null) {
            _outputArea.append( msg );
        }

    }
}
