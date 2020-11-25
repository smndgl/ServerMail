package model;

import java.io.Serializable;

public class Message<T> implements Serializable {
    private MessageType type;
    private T content;


    public Message(MessageType type, T content) {
        this.type = type;
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message received: ["+type+"]";
    }
}
