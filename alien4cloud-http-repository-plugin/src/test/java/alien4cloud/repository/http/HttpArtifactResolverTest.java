package alien4cloud.repository.http;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;

public class HttpArtifactResolverTest {

    private HttpArtifactResolver httpArtifactResolver = new HttpArtifactResolver();

    @Test
    public void testGetResolverType() throws IOException {
        assertEquals("http", httpArtifactResolver.getResolverType());
    }

    @Test
    public void canHandleArtifactWithSuccess() throws IOException {
        String artifactReference = "alien4cloud/alien4cloud-cloudify3-provider/1.3.0-SM2/alien4cloud-cloudify3-provider-1.3.0-SM2.zip";
        String repositoryURL = "https://fastconnect.org/maven/service/local/repositories/opensource/content";
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWhenAnArtifactURLAndEmptyRepositoryWithSuccess() throws IOException {
        String artifactReference = "https://fastconnect.org/maven/service/local/repositories/opensource/content/alien4cloud/alien4cloud-cloudify3-provider/1.3.0-SM2/alien4cloud-cloudify3-provider-1.3.0-SM2.zip";
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, null, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongURLShouldFailed() throws IOException {
        String artifactReference = "alien4cloud/alien4cloud-cloudify3-provider/1.3.0-SM2/alien4cloud-cloudify3-provider-1.3.0-SM2.zip";
        String repositoryURL = "fastconnect";
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_URL, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactOnWrongTypeShouldFailed() throws IOException {
        String artifactReference = "alien4cloud/alien4cloud-cloudify3-provider/1.3.0-SM2/alien4cloud-cloudify3-provider-1.3.0-SM2.zip";
        String repositoryURL = "https://fastconnect.org/maven/service/local/repositories/opensource/content";
        String repositoryType = "git";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_TYPE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongCredentialsShouldFailed() throws IOException {
        String artifactReference = "alien4cloud/alien4cloud-cloudify3-provider/1.3.0-SM2/alien4cloud-cloudify3-provider-1.3.0-SM2.zip";
        String repositoryURL = "https://fastconnect.org/maven/service/local/repositories/opensource/content";
        String repositoryType = "http";
        String credentials = "toto";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials);
        assertEquals(ValidationStatus.INVALID_CREDENTIALS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactReferenceShouldFailed() throws IOException {
        String repositoryURL = "https://fastconnect.org/maven/service/local/repositories/opensource/content";
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(null, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_ARTIFACT_REFERENCE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactURLShouldFailed() throws IOException {
        String artifactReference = "https://fastconnectshouldfailed.org";
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(artifactReference, null, repositoryType, null);
        assertEquals(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactURLAndRepositoryURLShouldFailed() throws IOException {
        String repositoryType = "http";
        ValidationResult validationResult = httpArtifactResolver.canHandleArtifact(null, null, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_ARTIFACT_REFERENCE, validationResult.getStatus());
    }

}


