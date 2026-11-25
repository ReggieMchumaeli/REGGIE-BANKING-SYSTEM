package bank.view;

import bank.BankException;
import bank.app.AppContext;
import bank.dao.DataAccessException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Boundary class for customer registration. */
public class RegisterView {
    @FXML private TextField customerIdField;
    @FXML private TextField firstNameField;
    @FXML private TextField surnameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private CheckBox employedCheck;
    @FXML private Label messageLabel;

    @FXML
    private void onRegister() {
        try {
            AppContext.get().getCustomerController().register(
                    customerIdField.getText(), firstNameField.getText(), surnameField.getText(),
                    addressField.getText(), phoneField.getText(),
                    passwordField.getText(), confirmField.getText(), employedCheck.isSelected());
            Navigator.showInfo("Registration successful. You can now log in.");
            Navigator.show("login.fxml", "Banking System - Login");
        } catch (BankException | DataAccessException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onBack() {
        Navigator.show("login.fxml", "Banking System - Login");
    }
}
