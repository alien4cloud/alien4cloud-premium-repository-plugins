package alien4cloud.repository.git;

import static alien4cloud.repository.git.GitUtil.isGitURL;

import java.nio.file.Path;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.exception.InvalidResolverConfigurationException;
import alien4cloud.repository.model.ValidationResult;
import alien4cloud.repository.model.ValidationStatus;
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
        if (!isGitURL(configuration.getUrl())) {
            throw new InvalidResolverConfigurationException("URL is not a valid git " + configuration.getUrl());
        }
        this.configuration = configuration;
    }

    @Override
    public ValidationResult canHandleArtifact(String artifactReference, String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return gitArtifactResolver.canHandleArtifact(artifactReference, ResolverUtil.getMandatoryConfiguration(this).getUrl(), repositoryType,
                    ResolverUtil.getConfiguredCredentials(this, credentials));
        }
    }

    private ValidationResult validateArtifact(String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        if (StringUtils.isNotBlank(repositoryURL) && !repositoryURL.equals(ResolverUtil.getMandatoryConfiguration(this).getUrl())) {
            return new ValidationResult(ValidationStatus.INVALID_REPOSITORY_URL, "Artifact's repository's URL does not match configuration");
        } else {
            return gitArtifactResolver.validateArtifact(ResolverUtil.getMandatoryConfiguration(this).getUrl(), repositoryType, credentials);
        }
    }

    @Override
    public Path resolveArtifact(String artifactReference, String repositoryURL, String repositoryType, Map<String, Object> credentials) {
        if (!validateArtifact(repositoryURL, repositoryType, credentials).equals(ValidationResult.SUCCESS)) {
            return null;
        }
        return gitArtifactResolver.doResolveArtifact(artifactReference, ResolverUtil.getMandatoryConfiguration(this).getUrl(),
                ResolverUtil.getConfiguredCredentials(this, credentials));
    }
}
