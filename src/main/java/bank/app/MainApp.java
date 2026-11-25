package bank.app;

import bank.view.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX entry point. Run with: mvn javafx:run */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AppContext.init("jdbc:h2:./data/bankdb");   // file database, created on first run
        Navigator.init(stage);
        Navigator.show("login.fxml", "Banking System - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
