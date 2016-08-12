package alien4cloud.repository.http;

import java.nio.file.Path;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.repository.util.ResolverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigurableHttpArtifactResolver implements IConfigurableArtifactResolver<HttpArtifactResolverConfiguration> {

    @Resource
    private HttpArtifactResolver httpArtifactResolver;

    private HttpArtifactResolverConfiguration configuration;

    public HttpArtifactResolverConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(HttpArtifactResolverConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        // The artifact's repository url is configured and it must be equals to the configured URL of the resolver
        return (StringUtils.isBlank(repositoryURL) || repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl()))
                && httpArtifactResolver.canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials);
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, String credentials) {
        if (!canHandleArtifact(artifactReference, repositoryURL, repositoryType, credentials)) {
            return null;
        }
        HttpArtifactResolverConfiguration preConfiguration = ResolverUtil.getMandatoryConfiguration(this);
        if (StringUtils.isBlank(credentials) && preConfiguration.getUser() != null) {
            credentials = preConfiguration.getUser() + ":" + preConfiguration.getPassword();
        }
        return httpArtifactResolver.doResolveArtifact(artifactReference, repositoryURL, credentials);
    }
}
