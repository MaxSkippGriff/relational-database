package SQL;

/**
 * Class GlobalErrorHandler
 * used to catch parse errors
 * Inspired by
 * https://www.baeldung.com/java-global-exception-handler
 * and
 * https://airbrake.io/blog/java-exception-handling/
 * invocationtargetexception#:~:text=lang.-,reflect.,
 * in%20the%20Java%20Exception%20Hierarchy.
 */

@SuppressWarnings("serial")

public class GlobalErrorHandler extends Exception {

    String error;


    // constructor
    public GlobalErrorHandler(String error)
    {
        this.error = error;
    }


    // return message 
    public String ToString()
    {
        return "[ERROR]: " + error;
    }
}
