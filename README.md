# CSE202 Banking System (JavaFX + H2 + JDBC)

Layers (the GUI never talks to the model or database directly):

```
FXML screens  ->  View classes (boundary)  ->  Controllers  ->  Core model (Bank, Account...)
                                                    \-> DAOs (JDBC) -> H2 database
```

## Project layout

```
pom.xml
.devcontainer/devcontainer.json        Java 21 + Maven + a desktop for JavaFX in Codespaces
src/main/java/bank/                    Core model: Account, Savings/Investment/Cheque, Customer, Bank, Transaction ...
src/main/java/bank/dao/                Database, CustomerDAO, AccountDAO, TransactionDAO
src/main/java/bank/controller/         LoginController, CustomerController, AccountController, Session
src/main/java/bank/view/               LoginView, RegisterView, DashboardView, OpenAccountView, Navigator (boundary classes)
src/main/java/bank/app/                MainApp (entry point), AppContext (wiring), SampleData (10 customers)
src/main/resources/bank/view/          login.fxml, register.fxml, dashboard.fxml, open-account.fxml, styles.css
src/test/java/bank/                    BankIntegrationTest
```

## Run

```bash
mvn test            # integration tests (controller -> model -> DAO -> database)
mvn javafx:run      # start the GUI
```

First run creates `./data/bankdb` and inserts 10 sample customers.
Log in with `C001` ... `C010`, password `pass1234`.

### Seeing the GUI in GitHub Codespaces

1. Commit `.devcontainer/devcontainer.json`, then Command Palette -> **Codespaces: Rebuild Container**.
2. Run `mvn javafx:run` in the terminal.
3. Open the **Ports** tab, open port **6080** in the browser, password `vscode`. The window appears there.

You can also clone the repo on your own computer (Java 17+ and Maven) and run the same commands.
