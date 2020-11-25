import controller.ConsoleController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataModel;
import task.ClientHandlerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main extends Application {
    private final static int PORT = 8189;
    private boolean INTERRUPTION = false;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader consoleLoader = new FXMLLoader(getClass().getResource("view/console.fxml"));
        Parent root = consoleLoader.load();
        ConsoleController console = consoleLoader.getController();

        DataModel model = new DataModel();
        console.initModel(model);

        primaryStage.setTitle("ServerMail");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        ServerSocket serverSocket = new ServerSocket(PORT);
        model.consoleLog("Server started!!");

        primaryStage.setOnCloseRequest((windowEvent) -> {
            model.consoleLog("Connection closed");
            INTERRUPTION = true;
            try {
                serverSocket.close();
            }
            catch (IOException e) {
                model.consoleLog("Exception on close: "+ e.getMessage());
                System.exit(0);
            }
        });

        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        /*
         * loading
         */

        new Thread(new Runnable() {
            @Override
            public void run() {
               while (!INTERRUPTION) {
                   try {
                       Socket clientSocket = null;
                       clientSocket = serverSocket.accept();
                       poolExecutor.execute(new ClientHandlerThread(clientSocket, model));
                   }
                   catch(IOException e) {
                       model.consoleLog("serverSocket.accept() failed: "+ e.getMessage());
                       System.exit(0);
                   }
               }
            }
        }).start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
