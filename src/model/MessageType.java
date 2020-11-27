package model;

public enum  MessageType {
    send, //client sends an email
    delete_i, //client deletes an email from inbox or sent
    delete_s,
    sync,  //server sends sync msg for advise client of a new email
    login, //username check and login
    logout, //remove mailboxes from runtime memory of server
    fetch //mailboxes retrieving from thread Sync
}
