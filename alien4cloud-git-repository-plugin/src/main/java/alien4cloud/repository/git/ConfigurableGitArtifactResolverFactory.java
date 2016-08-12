package alien4cloud.repository.git;

import javax.annotation.Resource;

import org.springframework.context.ApplicationContext;

import alien4cloud.component.repository.IConfigurableArtifactResolver;
import alien4cloud.component.repository.IConfigurableArtifactResolverFactory;

public class ConfigurableGitArtifactResolverFactory implements IConfigurableArtifactResolverFactory<GitArtifactResolverConfiguration> {

    @Resource
    private ApplicationContext factoryContext;

    @Resource
    private GitArtifactResolver gitArtifactResolver;

    @Override
    public IConfigurableArtifactResolver<GitArtifactResolverConfiguration> newInstance() {
        return factoryContext.getBean(ConfigurableGitArtifactResolver.class);
    }

    @Override
    public Class<GitArtifactResolverConfiguration> getResolverConfigurationType() {
        return GitArtifactResolverConfiguration.class;
    }

    @Override
    public String getResolverType() {
        return gitArtifactResolver.getResolverType();
    }
}
