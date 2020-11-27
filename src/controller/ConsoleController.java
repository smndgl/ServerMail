package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import model.DataModel;

public class ConsoleController {
    @FXML
    private TextArea txtLog;

    private DataModel model;

    public void initModel(DataModel model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;

        model.loadAcccountList();

        txtLog.setOpacity(1);
        txtLog.setWrapText(true);
        txtLog.textProperty().bind(model.LogProperty());

        /* TODO
            #1 carico la lista degli utenti
            #2 carico dal model la lista delle email (inviate e ricevute)
            #3 creo la socket
            #4 event handler su chiusura server per chiudere le socket ed eventualmente salvare modifiche
            #5 thread socket per ogni client
         */

    }
}
