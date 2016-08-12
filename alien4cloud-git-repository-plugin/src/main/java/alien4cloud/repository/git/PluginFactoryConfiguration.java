package alien4cloud.repository.git;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@ImportResource("classpath:alien-properties-config.xml")
public class PluginFactoryConfiguration {

    @Bean(name = "configurable-git-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ConfigurableGitArtifactResolver configurableGitArtifactResolver() {
        return new ConfigurableGitArtifactResolver();
    }

    @Bean(name = "configurable-git-artifact-resolver-factory")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConfigurableGitArtifactResolverFactory configurableGitArtifactResolverFactory() {
        return new ConfigurableGitArtifactResolverFactory();
    }

    @Bean(name = "git-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public GitArtifactResolver gitArtifactResolver() {
        return new GitArtifactResolver();
    }
}
