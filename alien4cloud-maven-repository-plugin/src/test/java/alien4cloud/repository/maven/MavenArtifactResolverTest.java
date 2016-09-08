package alien4cloud.repository.maven;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.elasticsearch.common.collect.ImmutableMap;
import org.junit.BeforeClass;
import org.junit.Test;

import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
import alien4cloud.tosca.normative.NormativeCredentialConstant;

public class MavenArtifactResolverTest {

    private static MavenArtifactResolver mavenArtifactResolver = new MavenArtifactResolver();

    @BeforeClass
    public static void before() throws IOException {
        mavenArtifactResolver.setTempDir("target/test");
    }

    @Test
    public void testGetResolverType() throws IOException {
        assertEquals("maven", mavenArtifactResolver.getResolverType());
    }

    @Test
    public void canHandleArtifactWithSuccess() throws IOException {
        String artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:1.3.0-SM2@zip";
        String repositoryURL = "https://fastconnect.org/maven/content/repositories/opensource";
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
        artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:[1.3.0-SM1,)@zip";
        validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
        artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:[100.0.1,100.0.2]@zip";
        validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWhenAnArtifactURLAndEmptyRepositoryWithSuccess() throws IOException {
        String artifactReference = "log4j:log4j:1.2.16";
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, null, repositoryType, null);
        assertEquals(ValidationStatus.SUCCESS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongURLShouldFailed() throws IOException {
        String artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:1.3.0-SM2";
        String repositoryURL = "fastconnect";
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_URL, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactOnWrongTypeShouldFailed() throws IOException {
        String artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:1.3.0-SM2";
        String repositoryURL = "https://fastconnect.org/maven/content/repositories/opensource";
        String repositoryType = "git";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_REPOSITORY_TYPE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongCredentialsShouldFailed() throws IOException {
        String artifactReference = "alien4cloud:alien4cloud-cloudify3-provider:1.3.0-SM2";
        String repositoryURL = "https://fastconnect.org/maven/content/repositories/opensource";
        String repositoryType = "maven";
        Map<String, Object> credentials = ImmutableMap.<String, Object> builder().put(NormativeCredentialConstant.USER_KEY, "toto").build();
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials);
        assertEquals(ValidationStatus.INVALID_CREDENTIALS, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactReferenceShouldFailed() throws IOException {
        String repositoryURL = "https://fastconnect.org/maven/content/repositories/opensource";
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(null, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_ARTIFACT_REFERENCE, validationResult.getStatus());
        String artifactReference = "somethingShouldFail";
        validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_ARTIFACT_REFERENCE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactURLShouldFailed() throws IOException {
        String artifactReference = "abc:cde:1.1.1";
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(artifactReference, null, repositoryType, null);
        assertEquals(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE, validationResult.getStatus());
    }

    @Test
    public void canHandleArtifactWithWrongArtifactURLAndRepositoryURLShouldFailed() throws IOException {
        String repositoryType = "maven";
        ValidationResult validationResult = mavenArtifactResolver.canHandleArtifact(null, null, repositoryType, null);
        assertEquals(ValidationStatus.INVALID_ARTIFACT_REFERENCE, validationResult.getStatus());
    }
}
