package br.com.anteros.restdoc.maven.plugin.doclet;

public class DocletAbortException extends RuntimeException {
    public DocletAbortException() {
    }

    public DocletAbortException(String message) {
        super(message);
    }

    public DocletAbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocletAbortException(Throwable cause) {
        super(cause);
    }

    public DocletAbortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
