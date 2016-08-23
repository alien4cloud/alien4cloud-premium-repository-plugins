package alien4cloud.repository.git;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import alien4cloud.repository.git.GitArtifactResolver;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;

public class GitArtifactResolverTest {

    private GitArtifactResolver gitArtifactResolver = new GitArtifactResolver();

    @Before
    public void init() {
        try {
            this.gitArtifactResolver.setTempDir("/tmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetResolverType() throws IOException {
        assertEquals("git", gitArtifactResolver.getResolverType());
    }

    @Test
    public void canHandleArtifactWithSuccess() throws IOException {
        String artifactReference = "demo-repository/artifacts/settings.properties";
        String repositoryURL = "https://github.com/alien4cloud/samples.git";
        String repositoryType = "git";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactForAllRepoWithSuccess() throws IOException {
        String repositoryURL = "https://github.com/alien4cloud/samples.git";
        String repositoryType = "git";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(null, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongURLShouldFailed() throws IOException {
        String artifactReference = "demo-repository/artifacts/settings.properties";
        String repositoryURL = "fastconnect";
        String repositoryType = "git";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_URL, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactOnWrongTypeShouldFailed() throws IOException {
        String artifactReference = "demo-repository/artifacts/settings.properties";
        String repositoryURL = "https://github.com/alien4cloud/samples.git";
        String repositoryType = "http";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_TYPE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongCredentialsShouldFailed() throws IOException {
        String artifactReference = "demo-repository/artifacts/settings.properties";
        String repositoryURL = "https://github.com/alien4cloud/samples.git";
        String repositoryType = "git";
        String credentials = "toto";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials);
        assertEquals(ValidationStatus.INVALID_CREDENTIALS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactURLAndRepositoryURLShouldFailed() throws IOException {
        String repositoryType = "git";
        ValidationResult validationResult = gitArtifactResolver.canHandleArtifact(null, null, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_URL, validationResult.getStatus());
    }

}

