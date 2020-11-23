package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class DataModel {
    private final StringProperty logProperty = new SimpleStringProperty();
    private int row_line = 0;

    public StringProperty getLogProperty() {
        return logProperty;
    }

    public void setLogProperty(String logLine) {
        this.logProperty.set(logProperty.get()+"\n#"+this.row_line+" "+logLine);
        row_line++;
    }
}
