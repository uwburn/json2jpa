package it.mgt.util.json2jpa;

public class Json2JpaException extends RuntimeException {

    public Json2JpaException(String message) {
        super(message);
    }

    public Json2JpaException(Throwable cause) {
        super(cause);
    }

    public Json2JpaException(String message, Throwable cause) {
        super(message, cause);
    }
}
