package cx.fbn.nevernote.config;

/**
 * Thrown for startup config errors
 *
 * @author Nick Clarke
 */
public class InitializationException extends Exception {
    private static final long serialVersionUID = 0L;
    
    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
