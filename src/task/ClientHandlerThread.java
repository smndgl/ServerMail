package task;

import com.google.gson.Gson;
import controller.ConsoleController;
import model.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandlerThread implements Runnable {
    private final Socket clientSocket;
    private String username;
    private final DataModel model;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private String USERNAME;

    private final String HEADER;

    public ClientHandlerThread(Socket clientSocket, DataModel model) {
        this.clientSocket = clientSocket;
        this.model = model;
        this.HEADER = "ClientHandlerThread #"+ Thread.currentThread().getName();
    }

    private Boolean isConnected(){
        return clientSocket != null && clientSocket.isConnected();
    }

    private void close() {
        try {
            if (objectOut != null) objectOut.close();
            if (objectIn != null) objectIn.close();
            if (clientSocket != null) clientSocket.close();
            objectOut = null;
            objectIn = null;
        } catch (IOException e) {
            System.err.println("Disconnection exception: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        model.consoleLog(HEADER+" started: "+clientSocket);
        try {
            objectIn = new ObjectInputStream(clientSocket.getInputStream());
            objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            while(isConnected()) {
                Object o = objectIn.readObject();
                if(o instanceof Message) {
                    switch(((Message)o).getType()) {
                        case send -> {
                            Message<Email> m = (Message) o;
                            model.consoleLog(HEADER+":"+USERNAME+" has sent an email "+m.toString());
                            /*TODO
                             *  DESTINATARI --> controllo che l'instanza mail box dei destinatari non sia null:
                             * - not_null: aggiungo le mail alla model.mailboxes(MapProperty) e al file
                             * - null: aggiungo le mail (inbox) SOLO al file
                             *  (map.property devo capire l'evento che si triggera aggiungendo un valore penso valuechange o qualcosa di simile)
                             *  MITTENTE --> aggiungo al file (sent) la mail e alla model.mailboxes(MapProperty)
                             */
                        }
                        case fetch -> {
                            try {
                                Message<String> m = (Message) o;
                                model.consoleLog(HEADER+": fetch request from "+ USERNAME +" "+m.toString());
                                ArrayList<Email> selected = model.getMailboxValue(USERNAME, m.getContent());
                                objectOut.writeObject(
                                        new Message<String>(MessageType.fetch,
                                                new Gson().toJson(selected)
                                        )
                                );
                                // mandato json con inbox/sent allo specifico USERNAMEm
                            }
                            catch(ClassCastException e) {
                                e.printStackTrace();
                            }
                        }
                        case delete -> {

                        }
                        case login -> {
                            /*
                             *
                             */
                            Message<String> m = (Message) o;
                            this.USERNAME = m.getContent();
                            model.consoleLog(HEADER+": "+m.toString());
                            objectOut.writeObject(new Message<>(MessageType.login, model.usernameExists(USERNAME)));
                            Mailbox mb = model.decodeMailbox(USERNAME);
                            model.mailboxesProperty().replace(USERNAME, mb); //TODO ? semaforo --> SI CAZZO prima mandano?
                            model.consoleLog(HEADER+": loaded mailbox for "+ USERNAME);
                        }
                    }
                }
            }
            close();
        }
        catch (IOException e) {
            model.consoleLog(HEADER+" Error:"+ e.getMessage());
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            model.consoleLog(HEADER+" Error:"+ e.getMessage());
            e.printStackTrace();
        }
    }
}
