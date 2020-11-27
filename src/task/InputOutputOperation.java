package task;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import model.DataModel;
import model.Email;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class InputOutputOperation implements Runnable {
    private String username;
    private String filter;
    private ArrayList<Email> list = null;
    private Email email = null;
    private Boolean authenticated = false;
    // case A and B
    public InputOutputOperation(String username, String filter, ArrayList<Email> list, Boolean authenticated) {
        this.username = username;
        this.filter = filter;
        this.list = new ArrayList<>(list);
        this.authenticated = authenticated;
    }

    public InputOutputOperation(String username, String filter, Email email, Boolean authenticated) {
        this.username = username;
        this.filter = filter;
        this.email = email;
        this.authenticated = authenticated;
    }


        @Override
    public void run() {
            /*
             *  autenticato && filtro new_inbow && email == null
             *        A  -> scrittura su file di model.get(username)
             *  non autenticato && fitro new_inbox && email != null
             *        C  -> leggo da file, sovrascrivo e riscrivo (costruttore con email)
             *  autenticato && filtro new_sent &&
             *   B scrivere su sent --> SEMPRE sovrascrittura con clienthandlerthread.sent
             *
             *  ELIMINAZIONE
             *   filtro delete - autenticato always true
             *      A filtro delete_inbox
             *          --> sovrascrivo con model
             *      filtro delete_sent
             *        B  --> sovrascrivo con sent
             *
             */
        File f = new File(System.getProperty("user.dir")+
                "/src/storage/"+ username + // account name
                "_"+filter+".json");
        if(authenticated && list != null) { // A e B
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(f));
                bw.write(new Gson().toJson(list, new TypeToken<Collection<Email>>(){}.getType()));
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(!authenticated && email != null){// caso C inserisco nell'utente non autenticato la nuova mail, non devo avvisare
            ArrayList<Email> emails = new ArrayList<>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                Type type = new TypeToken<List<Email>>(){}.getType();
                Gson gson = new Gson();
                emails = gson.fromJson(br, type);

                Collections.sort(emails);
                emails.add(email);

                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(gson.toJson(emails, new TypeToken<Collection<Email>>(){}.getType()));
                bw.flush();
                bw.close();
            }
            catch (IOException | JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
