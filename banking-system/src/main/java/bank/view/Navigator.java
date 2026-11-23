package bank.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/** Loads FXML screens into the main window or into a pop-up dialog. Pure UI plumbing. */
public final class Navigator {
    private static Stage stage;

    private Navigator() { }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    /** Replace the content of the main window with another screen. */
    public static void show(String fxmlFile, String title) {
        Parent root = load(fxmlFile);
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 960, 640);
            scene.getStylesheets().add(stylesheet());
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        stage.setTitle(title);
    }

    /** Open a modal pop-up and wait until it is closed. */
    public static void showDialog(String fxmlFile, String title) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        Scene scene = new Scene(load(fxmlFile));
        scene.getStylesheets().add(stylesheet());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static Parent load(String fxmlFile) {
        try {
            return FXMLLoader.load(Navigator.class.getResource("/bank/view/" + fxmlFile));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load screen " + fxmlFile, e);
        }
    }

    private static String stylesheet() {
        return Navigator.class.getResource("/bank/view/styles.css").toExternalForm();
    }
}
