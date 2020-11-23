package model;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Email implements Serializable, Comparable<Email> {
    private int id;//controlli da fare su id per unicità
    private String sender;
    private ArrayList<String> recipient = new ArrayList<>();
    private String subject;
    private String text;
    private Date mailingDate = new Date();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public ArrayList<String> getRecipient() {
        return recipient;
    }

    public String getRecipientAsString() {
        String res = "";
        for (String item : recipient)
            res += item + ", ";
        return recipient.size() > 0 ?  res.substring(0, res.length()-2) : "";
    }

    public void setRecipient(ArrayList<String> recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getMailingDate() {
        return mailingDate;
    }

    public void setMailingDate(Date mailingDate) {
        this.mailingDate = mailingDate;
    }

    public Email(int id, String sender, ArrayList<String> recipient, String subject, String text, Date mailingDate) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.text = text;
        this.mailingDate = mailingDate;
    }

    @Override
    public int compareTo(Email o) {
        return o.mailingDate.compareTo(mailingDate);
    }
}
