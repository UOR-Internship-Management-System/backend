package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

public class ExportGenerationException extends RuntimeException {
    private final String code;
    public ExportGenerationException(String code, String message) { super(message); this.code = code; }
    public ExportGenerationException(String code, String message, Throwable cause) { super(message, cause); this.code = code; }
    public String code() { return code; }
}
