package model;

import java.util.ArrayList;

public class Mailbox {
    private ArrayList<Email> sent;
    private ArrayList<Email> inbox;

    public Mailbox() {
        this.sent = new ArrayList<>();
        this.inbox = new ArrayList<>();
    }

    public Mailbox(ArrayList<Email> sent, ArrayList<Email> inbox) {
        this.sent = sent;
        this.inbox = inbox;
    }

    public ArrayList<Email> Sent() {
        return sent;
    }

    public void Sent(ArrayList<Email> sent) {
        this.sent = sent;
    }

    public ArrayList<Email> Inbox() {
        return inbox;
    }

    public void Inbox(ArrayList<Email> inbox) {
        this.inbox = inbox;
    }

    public void add(String mb, Email e) {
        if(mb.equals("inbox"))
            this.inbox.add(e);
        else if(mb.equals("sent"))
            this.sent.add(e);
        else
            System.out.println("HOW DO YOU GET IN THERE ?!");
    }
}
