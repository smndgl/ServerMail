package model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DataModel {
    private final StringProperty logProperty = new SimpleStringProperty("");
    private int row_line = 0;

    public StringProperty LogProperty() {
        return logProperty;
    }

    public synchronized void consoleLog(String logLine) {
        if(logProperty.get().equals(""))
            this.logProperty.set("#"+this.row_line+" "+logLine);
        else
            this.logProperty.set(logProperty.get()+"\n#"+this.row_line+" "+logLine);

        row_line++;
    }

    //<editor-fold desc="Mailbox stuff">
    public HashMap<String, ObservableList<Email>> mailboxes = new HashMap<>();

    public ArrayList<Email> getMailbox(String username) {
        return new ArrayList<>(mailboxes.get(username));
    }

    public synchronized void initializeMailbox(String username, ArrayList<Email> inbox) {
        mailboxes.replace(username, FXCollections.observableArrayList(inbox));
    }

    public synchronized void detachMailbox(String username) {
        mailboxes.replace(username, null);
    }

    public synchronized void addNewEmail(String username, Email email) {
        int new_id = mailboxes.get(username).size() > 0 ? mailboxes.get(username).get(mailboxes.get(username).size()-1).getId() : 1;
        email.setId(new_id);
        mailboxes.get(username).add(email);
    }

    public synchronized void removeEmail(String username, Email email) {
        for(int i = 0; i < mailboxes.get(username).size(); i++)
            if(mailboxes.get(username).get(i).getId() == email.getId())
                mailboxes.get(username).remove(i);
    }

    /*
     * I/O mailbox
     */
    public ArrayList<Email> decodeInbox(String username) {
        ArrayList<Email> emails = new ArrayList<>();
        File f = new File(System.getProperty("user.dir")+
                "/src/storage/"+ username + // account name
                "_inbox.json");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            Type type = new TypeToken<List<Email>>(){}.getType();
            emails = new Gson().fromJson(br, type);
            if(emails == null)
                emails = new ArrayList<>();
        }
        catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        Collections.sort(emails);

        return emails;
    }

    public String decodeSent(String username) {
        String res = "";
        File f = new File(System.getProperty("user.dir")+
                "/src/storage/"+ username + // account name
                "_sent.json");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            while((line = br.readLine()) != null)
                res += line;
        }
        catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }

        return res;
    }

    //</editor-fold>

    // <editor-fold desc="AccountList">
    private ArrayList<String> accountList;

    public void loadAcccountList() {
        accountList = new ArrayList<>();
        System.out.println(System.getProperty("user.dir")+"/src/storage/account_list.txt");
        File f = new File(System.getProperty("user.dir")+"/src/storage/account_list.txt");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";
            while((line = br.readLine()) != null) {
                accountList.add(line);// fill map with <username, new Mailbox()>
            }
            consoleLog("Account list succesfully loaded!");

            for(String item : accountList) {
                mailboxes.putIfAbsent(item, null);
            }
        }
        catch (IOException e) {
            consoleLog("Error while loading account list: "+ e.getMessage());
            System.exit(0);
        }
    }

    public Boolean usernameExists(String username) {
        return accountList.contains(username);
    }

    public Boolean isAuthenticated(String username) {
        return mailboxes.get(username) != null;
    }

    // </editor-fold>
}
