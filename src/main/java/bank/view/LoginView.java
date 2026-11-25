package bank.view;

import bank.BankException;
import bank.app.AppContext;
import bank.dao.DataAccessException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Boundary class for the login screen: shows fields and passes input to the LoginController. */
public class LoginView {
    @FXML private TextField customerIdField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void onLogin() {
        try {
            AppContext.get().getLoginController().login(customerIdField.getText(), passwordField.getText());
            Navigator.show("dashboard.fxml", "Banking System - My Accounts");
        } catch (BankException | DataAccessException e) {
            messageLabel.setText(e.getMessage());
            passwordField.clear();
        }
    }

    @FXML
    private void onRegister() {
        Navigator.show("register.fxml", "Banking System - Register");
    }
}
