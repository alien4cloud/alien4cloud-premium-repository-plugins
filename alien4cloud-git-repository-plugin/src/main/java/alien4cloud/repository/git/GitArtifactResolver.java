package alien4cloud.repository.git;

import static alien4cloud.repository.git.GitUtil.isGitURL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Maps;

import alien4cloud.component.repository.IArtifactResolver;
import alien4cloud.git.RepositoryManager;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitArtifactResolver implements IArtifactResolver {

    private Path tempDir;

    private Map<CachedGitLocation, Path> cache = Maps.newConcurrentMap();

    @Value("${directories.alien}/${directories.upload_temp}")
    public void setTempDir(String tempDir) throws IOException {
        this.tempDir = ResolverUtil.createPluginTemporaryDownloadDir(tempDir, "artifacts/git");
    }

    @Override
    public String getResolverType() {
        return "git";
    }

    @Override
    public ValidationResult canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        ValidationResult basicValidationResult = validateArtifact(repositoryURL, repositoryType, credentials);
        if (basicValidationResult.equals(ValidationResult.SUCCESS)) {
            // repository must be cloneable and file must exist
            try {
                Path artifactPath = cloneRepository(artifactReference, repositoryURL, credentials);
                if (!Files.exists(artifactPath)) {
                    return new ValidationResult(ValidationStatus.INVALID_ARTIFACT_REFERENCE,
                            "Artifact with reference " + artifactReference + " does not exist inside the repository " + repositoryURL);
                } else {
                    return ValidationResult.SUCCESS;
                }
            } catch (Exception e) {
                return new ValidationResult(ValidationStatus.ARTIFACT_NOT_RETRIEVABLE, "Artifact with reference " + artifactReference
                        + " cannot be retrieved from the repository " + repositoryURL + " because of error" + e.getMessage());
            }
        } else {
            return basicValidationResult;
        }
    }

    ValidationResult validateArtifact(String repositoryURL, String repositoryType, String credentials) {
        if (!ResolverUtil.isResolverTypeCompatible(this, repositoryType)) {
            // Type must be git
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_TYPE, "Repository is not of type " + getResolverType());
        }
        if (!ResolverUtil.isCredentialsBasicUserPassword(credentials)) {
            // Must have credentials as user:password
            return new ValidationResult(ValidationStatus.INVALID_CREDENTIALS, "Credentials must be in format [user:password]");
        }
        if (StringUtils.isBlank(repositoryURL) || !isGitURL(repositoryURL)) {
            // URL must be a git
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL,
                    "Repository's URL " + repositoryURL + " is not git compliant, must finish by .git");
        }
        return ValidationResult.SUCCESS;
    }

    private Path cloneRepository(String artifactReference, String repositoryURL, String credentials) throws IOException {
        String user = null;
        String password = null;
        if (StringUtils.isNotBlank(credentials)) {
            int indexOfSeparator = credentials.indexOf(':');
            user = credentials.substring(0, indexOfSeparator);
            password = credentials.substring(indexOfSeparator + 1, credentials.length());
        }
        if (artifactReference == null) {
            artifactReference = "";
        }
        int indexOfTwoPoints = artifactReference.lastIndexOf(':');
        String branch = null;
        String nestedPath;
        if (indexOfTwoPoints < 0) {
            nestedPath = artifactReference;
        } else {
            branch = artifactReference.substring(0, indexOfTwoPoints);
            nestedPath = artifactReference.substring(indexOfTwoPoints + 1, artifactReference.length());
        }
        if (StringUtils.isBlank(branch)) {
            branch = "master";
        }
        while (nestedPath.startsWith("/")) {
            // Trim '/' characters
            nestedPath = nestedPath.substring(1);
        }
        CachedGitLocation cachedGitLocation = new CachedGitLocation(repositoryURL, user, password, branch);
        Path repoPath = cache.get(cachedGitLocation);
        if (repoPath != null && !Files.exists(repoPath)) {
            log.info("Cached Git repository {} at path {} has been removed", cachedGitLocation, repoPath);
            cache.remove(cachedGitLocation);
            repoPath = null;
        }
        boolean newGit = repoPath == null;
        if (newGit) {
            repoPath = Files.createTempDirectory(tempDir, "");
            log.info("Clone new Git repository {} and put in cache for further use {}", cachedGitLocation, repoPath);
            cache.put(cachedGitLocation, repoPath);
        }
        Git git = RepositoryManager.cloneOrCheckout(repoPath, repositoryURL, user, password, branch, "");
        if (newGit) {
            RepositoryManager.pull(git, user, password);
        }
        return repoPath.resolve(nestedPath);
    }

    Path doResolveArtifact(String artifactReference, String repositoryURL, String credentials) {
        try {
            Path artifactPathInsideRepo = cloneRepository(artifactReference, repositoryURL, credentials);
            return ResolverUtil.copyArtifactToTempFile(artifactReference, artifactPathInsideRepo, tempDir);
        } catch (Exception e) {
            log.info("Could not resolve git artifact " + artifactReference + " at " + repositoryURL + " because of " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve git artifact " + artifactReference + " at " + repositoryURL, e);
            }
            return null;
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!validateArtifact(repositoryURL, repositoryType, credentials).equals(ValidationResult.SUCCESS)) {
            return null;
        }
        return doResolveArtifact(artifactReference, repositoryURL, credentials);
    }
}