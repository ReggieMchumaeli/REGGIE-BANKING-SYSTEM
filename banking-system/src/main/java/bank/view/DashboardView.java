package bank.view;

import bank.Account;
import bank.BankException;
import bank.Transaction;
import bank.app.AppContext;
import bank.controller.AccountController;
import bank.dao.DataAccessException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

/** Boundary class for the main screen: accounts, deposits, withdrawals and history. No business rules here. */
public class DashboardView {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private Label welcomeLabel;
    @FXML private Label messageLabel;
    @FXML private TextField amountField;

    @FXML private TableView<Account> accountsTable;
    @FXML private TableColumn<Account, String> typeColumn;
    @FXML private TableColumn<Account, String> numberColumn;
    @FXML private TableColumn<Account, String> branchColumn;
    @FXML private TableColumn<Account, String> balanceColumn;

    @FXML private TableView<Transaction> historyTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> txTypeColumn;
    @FXML private TableColumn<Transaction, String> txAmountColumn;
    @FXML private TableColumn<Transaction, String> balanceAfterColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    private AccountController accounts() {
        return AppContext.get().getAccountController();
    }

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + AppContext.get().getLoginController().getCurrentCustomerName());

        typeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccountType()));
        numberColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAccountNumber()));
        branchColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBranch()));
        balanceColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("BWP %,.2f", c.getValue().getBalance())));

        dateColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDate().format(DATE_FORMAT)));
        txTypeColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType().name()));
        txAmountColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%,.2f", c.getValue().getAmount())));
        balanceAfterColumn.setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%,.2f", c.getValue().getBalanceAfter())));
        descriptionColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription()));

        accountsTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldAccount, selected) -> showHistory(selected));

        refresh(null);
    }

    @FXML
    private void onDeposit() {
        runTransaction(true);
    }

    @FXML
    private void onWithdraw() {
        runTransaction(false);
    }

    @FXML
    private void onOpenAccount() {
        Navigator.showDialog("open-account.fxml", "Open a new account");
        refresh(null);
    }

    @FXML
    private void onPayInterest() {
        try {
            accounts().payMonthlyInterest();
            Account selected = accountsTable.getSelectionModel().getSelectedItem();
            refresh(selected == null ? null : selected.getAccountNumber());
            showMessage("Monthly interest has been paid to all interest-bearing accounts.", false);
        } catch (BankException | DataAccessException e) {
            showMessage(e.getMessage(), true);
        }
    }

    @FXML
    private void onLogout() {
        AppContext.get().getLoginController().logout();
        Navigator.show("login.fxml", "Banking System - Login");
    }

    private void runTransaction(boolean isDeposit) {
        Account selected = accountsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showMessage("Select an account first.", true);
            return;
        }
        String number = selected.getAccountNumber();
        try {
            if (isDeposit) {
                accounts().deposit(number, amountField.getText());
            } else {
                accounts().withdraw(number, amountField.getText());
            }
            amountField.clear();
            refresh(number);
            showMessage(isDeposit ? "Deposit successful." : "Withdrawal successful.", false);
        } catch (BankException | DataAccessException e) {
            showMessage(e.getMessage(), true);
        }
    }

    /** Reload the accounts table and, if possible, keep the same account selected. */
    private void refresh(String accountNumberToSelect) {
        accountsTable.setItems(FXCollections.observableArrayList(accounts().getAccounts()));
        if (accountNumberToSelect != null) {
            for (Account a : accountsTable.getItems()) {
                if (a.getAccountNumber().equals(accountNumberToSelect)) {
                    accountsTable.getSelectionModel().select(a);
                    break;
                }
            }
        }
    }

    private void showHistory(Account selected) {
        if (selected == null) {
            historyTable.setItems(FXCollections.<Transaction>observableArrayList());
        } else {
            historyTable.setItems(FXCollections.observableArrayList(
                    accounts().getTransactions(selected.getAccountNumber())));
        }
    }

    private void showMessage(String text, boolean isError) {
        messageLabel.getStyleClass().setAll("label", isError ? "error-label" : "ok-label");
        messageLabel.setText(text);
    }
}
