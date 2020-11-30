package task;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.ListChangeListener;
import model.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ClientHandlerThread implements Runnable {
    private final Socket clientSocket;
    private final DataModel model;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;
    private String USERNAME;
    private ArrayList<Email> sent;
    private Boolean interruption = false;

    private String HEADER;

    public ClientHandlerThread(Socket clientSocket, DataModel model) {
        this.clientSocket = clientSocket;
        this.model = model;
        this.HEADER = "";
        this.sent = new ArrayList<>();
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
    /*
     * listener on inbox property for removing | adding an email
     */
    ListChangeListener<Email> emailListChangeListener = new ListChangeListener<Email>() {
        @Override
        public void onChanged(Change<? extends Email> c) {
            //avvio scrittura su file
            System.out.println("WHAT A FUCK IS GOINF ON HERE !!");
            new Thread(new InputOutputOperation(USERNAME, "inbox", model.getMailbox(USERNAME), true)).start();
            while(c.next()) {
                try {
                    if(c.wasAdded()) {
                        model.consoleLog(HEADER + ": EMAIL RECEIVED");
                        objectOut.writeObject(new Message<Email>(MessageType.sync, c.getAddedSubList().get(0)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void run() {
        model.consoleLog(" started connection: "+clientSocket);
        try {
            objectIn = new ObjectInputStream(clientSocket.getInputStream());
            objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            while(isConnected() && !interruption) {
                Object o = objectIn.readObject();
                if(o instanceof Message) {
                    switch(((Message)o).getType()) {
                        case send -> {
                            try {
                                Message<Email> message = (Message) o;
                                Email email = (Email)message.getContent();
                                String res = "";
                                for(String recipient : email.getRecipient()) {
                                    if(!model.usernameExists(recipient)) {
                                        res += "unknown address: "+ recipient+" ";
                                    }
                                }

                                if(res.equals("")) {
                                    this.sent.add(email); //added email to sentbox
                                    new Thread(new InputOutputOperation(USERNAME, "sent", sent, true)).start(); //append to file

                                    for (String recipient : email.getRecipient()) {
                                        if (model.isAuthenticated(recipient)) //foreach recipient add to propertylist
                                            model.addNewEmail(recipient, email);
                                        else
                                            new Thread(new InputOutputOperation(recipient, "inbox", email, false)).start();
                                    }
                                    model.consoleLog(HEADER + ": EMAIL SENT");
                                    objectOut.writeObject(new Message<String>(MessageType.send, "Email sent"));
                                }
                                else { //mandare indietro unknown address
                                    objectOut.writeObject(new Message<String>(MessageType.send, "Error:"+ res));
                                }
                            }
                            catch (ClassCastException e) {
                                System.err.println(HEADER +" throw error: "+e.getMessage());
                            }
                        }
                        case fetch -> {
                            try {
                                Message<String> message = (Message) o;
                                model.consoleLog(HEADER+": FETCH REQUEST");
                                if(message.getContent().equals("INBOX")) {
                                    objectOut.writeObject(
                                            new Message<String>(MessageType.fetch,
                                                    new Gson().toJson(model.getMailbox(USERNAME))
                                            )
                                    );
                                }
                                else if(message.getContent().equals("SENT")) {
                                    String sent_json = model.decodeSent(USERNAME);
                                    this.sent = new Gson().fromJson(sent_json, new TypeToken<Collection<Email>>(){}.getType());
                                    objectOut.writeObject(new Message<String>(MessageType.fetch, sent_json));
                                }
                                // mandato json con inbox/sent allo specifico USERNAME
                            }
                            catch(ClassCastException e) {
                                e.printStackTrace();
                            }
                        }
                        case delete_i -> {
                            Message<Email> message = (Message) o;
                            model.removeEmail(USERNAME, message.getContent()); //change event wont trigger
                            model.consoleLog(HEADER+": DELETE FROM INBOX");
                        }
                        case delete_s -> {
                            Message<Email> message = (Message) o;
                            Email tbd = message.getContent();

                            for(int i = 0; i < sent.size(); i++)
                                if(sent.get(i).getId() == tbd.getId())
                                    sent.remove(i);

                            new Thread(new InputOutputOperation(USERNAME, "sent", sent, true)).start(); //append to file
                            model.consoleLog(HEADER+": DELETE FROM SENT");
                        }
                        case login, reconnect -> {
                            Message<String> message = (Message) o;
                            this.USERNAME = message.getContent();
                            this.HEADER = "ClientHandler["+USERNAME+"]";
                            Thread.currentThread().setName("ClientHandler["+USERNAME+"]");
                            if(message.getType() == MessageType.login) {
                                Boolean res = model.usernameExists(USERNAME);
                                objectOut.writeObject(new Message<>(MessageType.login, res));
                                if (res) {
                                    model.consoleLog(HEADER + ": LOGIN");

                                    model.initializeMailbox(USERNAME, model.decodeInbox(USERNAME));
                                    // listener for observable list inbox
                                    model.mailboxes.get(USERNAME).addListener(emailListChangeListener);
                                    model.consoleLog(HEADER + ": MAILBOXES LOADED");
                                } else {
                                    model.consoleLog("TRY AGAIN DUDE, I BELIEVE IN YOU <3");
                                }
                            }
                            else {
                                model.consoleLog(HEADER + "RECONNECTION REQUEST");
                                model.initializeMailbox(USERNAME, model.decodeInbox(USERNAME));
                                model.mailboxes.get(USERNAME).addListener(emailListChangeListener);
                                model.consoleLog(HEADER + ": MAILBOXES LOADED");
                            }
                        }
                        case logout -> {
                            Message<String> message = (Message) o;
                            this.USERNAME = message.getContent();
                            model.consoleLog(HEADER+": LOGOUT");

                            model.mailboxes.get(USERNAME).removeListener(emailListChangeListener);
                            model.detachMailbox(USERNAME); //il client si è sloggato, setto a null la sua mailbox
                            this.interruption = true;
                        }
                    }
                }
            }
        }
        catch (IOException | ClassNotFoundException e) { //quando chiudo client erro null a cazzo
            if(e instanceof EOFException) {
                model.detachMailbox(USERNAME);
            }
            else {
                model.consoleLog(HEADER + " Error:" + e.getMessage());
                System.err.println(HEADER + "Error: " + e.getMessage());
            }
        }
        catch (NullPointerException e) {
            if(!isConnected())
                this.close();
        }

        this.close();
    }
}
