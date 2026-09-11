package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex;

/** Internal generation failure. Compiler output and temporary paths must never cross the API boundary. */
public class LatexCompilationException extends RuntimeException {
    public LatexCompilationException(String message) {
        super(message);
    }

    public LatexCompilationException(String message, Throwable cause) {
        super(message, cause);
    }
}
