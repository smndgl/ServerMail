package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Account {
    final private StringProperty accountName = new SimpleStringProperty();

    public StringProperty accountNameProperty() {
        return accountName;
    }

    public final String getAccountName() {
        return accountNameProperty().get();
    }

    public final void setAccountName(String accountName) {
        accountNameProperty().set(accountName);
    }

    public Account(String accountName) {
        setAccountName(accountName);
    }
}
