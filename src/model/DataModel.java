package model;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class DataModel {
    private final StringProperty logProperty = new SimpleStringProperty("");
    private int row_line = 0;

    public StringProperty LogProperty() {
        return logProperty;
    }

    public void consoleLog(String logLine) {
        if(logProperty.get() == "")
            this.logProperty.set("#"+this.row_line+" "+logLine);
        else
            this.logProperty.set(logProperty.get()+"\n#"+this.row_line+" "+logLine);

        row_line++;
    }
    //<editor-fold desc="Mailbox stuff">
    public ObservableMap<String, Mailbox> mailboxes = FXCollections.emptyObservableMap();

    public ObservableMap<String, Mailbox> mailboxesProperty() {
        return mailboxes;
    }

    public void setMailboxValue(String username, Mailbox mb){
        mailboxes.replace(username, mb);
    }

    public ArrayList<Email> getMailboxValue(String username, String filter) {
        if(filter.equals("INBOX"))
            return  mailboxesProperty().get(username).Inbox();
        else if(filter.equals("SENT"))
            return  mailboxesProperty().get(username).Sent();
        else
            consoleLog("?? HOW DO YOU GET IN THERE ?? (DataModel.java line: 51)");
        return null; //hope won't pass here
    }

    /*
     * I/O mailbox
     */
    public Mailbox decodeMailbox(String username) {
        return new Mailbox(
                decodeSingleMailbox(username, "sent"),
                decodeSingleMailbox(username, "inbox")
        );
    }

    public ArrayList<Email> decodeSingleMailbox(String username, String mailbox) {
        ArrayList<Email> emails = new ArrayList<>();
        File f = new File(System.getProperty("user.dir")+
                "/src/storage/"+ username + // account name
                "_" +mailbox +                // choosen mailbox
                ".json");
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            Type type = new TypeToken<List<Email>>(){}.getType();
            emails = new Gson().fromJson(br, type);
        }
        catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        Collections.sort(emails);

        return emails;
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
            HashMap<String, Mailbox> map = new HashMap<>();
            for(String item : accountList) {
                map.putIfAbsent(item, new Mailbox());
            }
            mailboxes = FXCollections.observableMap(map);
        }
        catch (IOException e) {
            consoleLog("Error while loading account list: "+ e.getMessage());
            System.exit(0);
        }
    }

    public Boolean usernameExists(String username) {
        return accountList.contains(username);
    }

    // </editor-fold>
}
