package alien4cloud.repository.git;

import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurableGitArtifactResolver implements IConfigurableArtifactResolver<GitArtifactResolverConfiguration> {

    @Resource
    private GitArtifactResolver gitArtifactResolver;

    private GitArtifactResolverConfiguration configuration;

    public GitArtifactResolverConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(GitArtifactResolverConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        return (StringUtils.isBlank(repositoryURL) || repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl()))
                && gitArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials);
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        GitArtifactResolverConfiguration preConfiguration = ResolverUtil.getMandatoryConfiguration(this);
        if (StringUtils.isBlank(credentials) && preConfiguration.getUser() != null) {
            credentials = preConfiguration.getUser() + ":" + preConfiguration.getPassword();
        }
        return gitArtifactResolver.doResolveArtifact(artifactReference, repositoryURL, credentials);
    }
}
