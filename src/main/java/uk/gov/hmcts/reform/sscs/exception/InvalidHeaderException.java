package uk.gov.hmcts.reform.sscs.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidHeaderException extends Exception  {
    private static final long serialVersionUID = 7849412332464241373L;

    public InvalidHeaderException(Exception ex) {
        super(ex);
    }
}
