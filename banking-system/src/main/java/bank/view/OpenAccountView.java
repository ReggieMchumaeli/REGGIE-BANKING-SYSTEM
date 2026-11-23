package bank.view;

import bank.BankException;
import bank.app.AppContext;
import bank.dao.DataAccessException;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Boundary class for the "open a new account" dialog. */
public class OpenAccountView {
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField depositField;
    @FXML private TextField companyNameField;
    @FXML private TextField companyAddressField;
    @FXML private Label hintLabel;
    @FXML private Label messageLabel;

    @FXML
    private void initialize() {
        typeBox.getItems().addAll("Savings", "Investment", "Cheque");
        typeBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, type) -> updateFields(type));
        typeBox.getSelectionModel().select("Savings");
    }

    /** Only changes what is shown; the rules themselves are enforced by the model. */
    private void updateFields(String type) {
        boolean cheque = "Cheque".equals(type);
        companyNameField.setDisable(!cheque);
        companyAddressField.setDisable(!cheque);
        if ("Investment".equals(type)) {
            hintLabel.setText("Investment accounts need an opening deposit of at least BWP 500.00.");
        } else if (cheque) {
            hintLabel.setText("Cheque accounts are for working customers. Enter your employer's details.");
        } else {
            hintLabel.setText("Savings accounts pay 0.05% monthly interest. No withdrawals are allowed.");
        }
    }

    @FXML
    private void onOpen() {
        try {
            AppContext.get().getAccountController().openAccount(
                    typeBox.getValue(), depositField.getText(),
                    companyNameField.getText(), companyAddressField.getText());
            close();
        } catch (BankException | DataAccessException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) messageLabel.getScene().getWindow()).close();
    }
}
