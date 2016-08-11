package alien4cloud.repository.git;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IArtifactResolver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitArtifactResolver implements IArtifactResolver {

    @Override
    public String getResolverType() {
        return "http";
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (StringUtils.isNotBlank(repositoryType) && !repositoryType.equals(getResolverType())) {
            return false;
        }
        // Must have credentials as user:password
        return credentials.indexOf(':') > 0 && isGitURL(repositoryURL);
    }

    private boolean isGitURL(String reference) {
        return reference.startsWith("http://") || reference.startsWith("https://");
    }

    @Override
    public InputStream resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        int indexOfSeparator = credentials.indexOf(':');
        String getURL = null;
        try {
            if (isGitURL(artifactReference)) {
                getURL = artifactReference;
            } else {
                getURL = repositoryURL + "/" + URLEncoder.encode(artifactReference, "UTF-8");
            }
            // TODO
            // RepositoryManager.cloneOrCheckout(getURL, );

        } catch (IOException e) {
            log.warn("Error downloading artifact at " + getURL, e);
            return null;
        }
        return null;
    }
}
