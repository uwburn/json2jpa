package it.mgt.util.json2jpa;

public class JpaDeserializerException extends RuntimeException {

    public JpaDeserializerException(String message) {
        super(message);
    }

    public JpaDeserializerException(Throwable cause) {
        super(cause);
    }

    public JpaDeserializerException(String message, Throwable cause) {
        super(message, cause);
    }
}
