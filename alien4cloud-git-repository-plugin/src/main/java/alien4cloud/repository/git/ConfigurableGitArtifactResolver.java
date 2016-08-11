package alien4cloud.repository.git;

import java.io.InputStream;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.repository.exception.ResolverNotConfiguredException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurableGitArtifactResolver implements IConfigurableArtifactResolver<GitArtifactResolverConfiguration> {

    @Resource
    private GitArtifactResolver gitArtifactResolver;

    private GitArtifactResolverConfiguration configuration;

    private GitArtifactResolverConfiguration getMandatoryConfiguration() {
        if (configuration == null) {
            throw new ResolverNotConfiguredException("Git resolver is not configured, please call setConfiguration first");
        }
        return configuration;
    }

    @Override
    public void setConfiguration(GitArtifactResolverConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getResolverType() {
        return gitArtifactResolver.getResolverType();
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return false;
        }
        String configuredURL = getMandatoryConfiguration().getUrl();
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(configuredURL)) {
            // The artifact's repository url is configured and it's not equals to the configured URL of the resolver
            return false;
        }
        // By default if the user used short notation artifact_name: https://abcd.com/my_script.sh check if the reference contains repository's url
        return artifactReference.startsWith(configuredURL) && artifactReference.endsWith(".git");
    }

    @Override
    public InputStream resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        GitArtifactResolverConfiguration preConfiguration = getMandatoryConfiguration();
        if (StringUtils.isBlank(credentials) && preConfiguration.getUser() != null) {
            credentials = preConfiguration.getUser() + ":" + preConfiguration.getPassword();
        }
        return gitArtifactResolver.resolveArtifact(artifactReference, repositoryURL, repositoryType, credentials);
    }
}
