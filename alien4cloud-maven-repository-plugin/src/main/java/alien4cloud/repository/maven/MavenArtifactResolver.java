package alien4cloud.repository.maven;

import static alien4cloud.repository.maven.MavenUtil.isValidMavenRepository;
import static alien4cloud.repository.maven.MavenUtil.newRepositorySystem;
import static alien4cloud.repository.maven.MavenUtil.newSession;
import static alien4cloud.repository.maven.MavenUtil.resolveMavenArtifact;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.springframework.beans.factory.annotation.Value;

import alien4cloud.component.repository.IArtifactResolver;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MavenArtifactResolver implements IArtifactResolver {

    private static final Pattern MAVEN_ARTIFACT_REGEXP = Pattern.compile("([^:@]+):([^:@]+):([^:@]+)(?::([^:@]+))?(?:@([^:@]+))?");

    private RepositorySystem system;

    private RepositorySystemSession session;

    @Value("${directories.alien}/${directories.upload_temp}")
    public void setTempDir(String alienUploadTempDir) throws IOException {
        Path tempDir = ResolverUtil.createPluginTemporaryDownloadDir(alienUploadTempDir, "artifacts/maven");
        this.system = newRepositorySystem();
        this.session = newSession(tempDir, this.system);
    }

    @Override
    public String getResolverType() {
        return "maven";
    }

    @Override
    public ValidationResult canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        ValidationResult basicValidationResult = validateArtifact(artifactReference, repositoryURL, repositoryType, credentials);
        if (basicValidationResult.equals(ValidationResult.SUCCESS)) {
            try {
                Artifact artifact = resolveMavenArtifact(system, session, repositoryURL, credentials, convertToArtifactRequest(artifactReference));
                if (artifact.getFile().exists()) {
                    return ValidationResult.SUCCESS;
                } else {
                    return new ValidationResult(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE,
                            "Artifact cannot be retrieved as it does not exist at " + artifact.getFile());
                }
            } catch (ArtifactResolutionException | VersionRangeResolutionException e) {
                return new ValidationResult(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE, "Artifact cannot be retrieved " + e.getMessage());
            }
        }
        return basicValidationResult;
    }

    private MavenArtifactRequest convertToArtifactRequest(String alienArtifactFormat) throws ArtifactResolutionException {
        Matcher matcher = MAVEN_ARTIFACT_REGEXP.matcher(alienArtifactFormat);
        if (matcher.matches()) {
            String groupId = matcher.group(1);
            String artifactId = matcher.group(2);
            String version = matcher.group(3);
            String classifier = matcher.group(4);
            String packaging = matcher.group(5);
            return new MavenArtifactRequest(groupId, artifactId, packaging, version, classifier);
        } else {
            // Normally this would never happens as it was validated before
            throw new ArtifactResolutionException(Collections.emptyList(), "Artifact do not follow format [" + MAVEN_ARTIFACT_REGEXP.pattern() + "]");
        }
    }

    String doResolveArtifact(String artifactReference, String repositoryURL, Map<String, Object> credentials) {
        try {

            Artifact artifact = resolveMavenArtifact(system, session, repositoryURL, credentials, convertToArtifactRequest(artifactReference));
            if (artifact.getFile().exists()) {
                return artifact.getFile().toPath().toString();
            } else {
                return null;
            }
        } catch (ArtifactResolutionException | VersionRangeResolutionException e) {
            log.info("Error downloading artifact" + artifactReference + " at " + repositoryURL + " because of exception " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Error downloading artifact" + artifactReference + " at " + repositoryURL, e);
            }
            return null;
        }
    }

    ValidationResult validateArtifact(String artifactReference, String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        if (!ResolverUtil.isResolverTypeCompatible(this, repositoryType)) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_TYPE, "Repository is not of type " + getResolverType());
        }
        if (!ResolverUtil.isCredentialsBasicUserPassword(credentials)) {
            // Must have credentials as user:password
            return new ValidationResult(ValidationStatus.INVALID_CREDENTIALS, "Credentials must be in format [user:password]");
        }
        // If repository is defined then it must follow URI syntax
        if (StringUtils.isNotBlank(repositoryURL) && !isValidMavenRepository(repositoryURL)) {
            // If reference is a http url then it must have base path the repository's url
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Repository's url is not a correct URI");
        }
        if (StringUtils.isBlank(artifactReference)) {
            return new ValidationResult(ValidationStatus.INVALID_ARTIFACT_REFERENCE, "Artifact's reference is mandatory for a maven artifact");
        } else if (!MAVEN_ARTIFACT_REGEXP.matcher(artifactReference).matches()) {
            return new ValidationResult(ValidationStatus.INVALID_ARTIFACT_REFERENCE,
                    "Artifact's reference must respect the following pattern [" + MAVEN_ARTIFACT_REGEXP.pattern() + "]");
        }
        return ValidationResult.SUCCESS;
    }

    @Override
    public String resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        if (!validateArtifact(artifactReference, repositoryURL, repositoryType, credentials).equals(ValidationResult.SUCCESS)) {
            return null;
        } else {
            return doResolveArtifact(artifactReference, repositoryURL, credentials);
        }
    }
}
