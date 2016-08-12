package alien4cloud.repository.git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.collect.Maps;

import alien4cloud.component.repository.IArtifactResolver;
import alien4cloud.git.RepositoryManager;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitArtifactResolver implements IArtifactResolver {

    private Path tempDir;

    private Map<CachedGitLocation, Path> cache = Maps.newConcurrentMap();

    @Value("${directories.alien}/${directories.upload_temp}")
    public void setTempDir(String tempDir) throws IOException {
        this.tempDir = ResolverUtil.createPluginTemporaryDownloadDir(tempDir, "artifacts");
    }

    @Override
    public String getResolverType() {
        return "git";
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!ResolverUtil.isResolverTypeCompatible(this, repositoryType)) {
            // Type must be git
            return false;
        }
        if (!ResolverUtil.isCredentialsBasicUserPassword(credentials)) {
            // Must have credentials as user:password
            return false;
        }
        return StringUtils.isNotBlank(repositoryURL) && isGitURL(repositoryURL);
    }

    private boolean isGitURL(String reference) {
        // Git offers a wide variety of protocol https://git-scm.com/book/en/v2/Git-on-the-Server-The-Protocols
        // It events works in local so better not check the scheme of reference
        return reference.endsWith(".git");
    }

    Path doResolveArtifact(String artifactReference, String repositoryURL, String credentials) {
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
        try {
            Path repoPath = cache.get(new CachedGitLocation(repositoryURL, user, password, branch));
            if (repoPath == null || !Files.exists(repoPath)) {
                repoPath = Files.createTempDirectory(tempDir, "");
                RepositoryManager.cloneOrCheckout(repoPath, repositoryURL, user, password, branch, "");
                cache.put(cachedGitLocation, repoPath);
            }
            Path artifactPathInsideRepo = repoPath.resolve(nestedPath);
            return ResolverUtil.copyArtifactToTempFile(artifactReference, artifactPathInsideRepo, tempDir);
        } catch (Exception e) {
            log.info("Unable to resolve git artifact [" + artifactReference + "] at repository [" + repositoryURL + "]", e);
            return null;
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        return doResolveArtifact(artifactReference, repositoryURL, credentials);
    }
}
