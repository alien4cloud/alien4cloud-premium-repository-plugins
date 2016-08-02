package alien4cloud.repository.exception;

import alien4cloud.exception.TechnicalException;

public class ArtifactNotFoundException extends TechnicalException {

    public ArtifactNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactNotFoundException(String message) {
        super(message);
    }
}
