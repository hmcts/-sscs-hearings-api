package uk.gov.hmcts.reform.sscs.exception;

public class CaseException extends Exception {
    private static final long serialVersionUID = -3977477574548786807L;

    public CaseException(String message) {
        super(message);
    }
}
