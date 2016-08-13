package alien4cloud.repository.exception;

import alien4cloud.exception.TechnicalException;

public class ResolverNotConfiguredException extends TechnicalException {

    public ResolverNotConfiguredException(String message) {
        super(message);
    }
}
