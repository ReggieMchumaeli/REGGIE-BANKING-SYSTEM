package bank.dao;

/** Wraps SQL errors so the rest of the application does not depend on java.sql. */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
