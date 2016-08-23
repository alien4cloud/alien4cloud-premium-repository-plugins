package alien4cloud.repository.maven;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;

@Configuration
@ImportResource("classpath:alien-properties-config.xml")
public class PluginFactoryConfiguration {

    @Bean(name = "configurable-maven-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ConfigurableMavenArtifactResolver configurableHttpArtifactResolver() {
        return new ConfigurableMavenArtifactResolver();
    }

    @Bean(name = "configurable-maven-artifact-resolver-factory")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConfigurableMavenArtifactResolverFactory configurableMavenArtifactResolverFactory() {
        return new ConfigurableMavenArtifactResolverFactory();
    }

    @Bean(name = "maven-artifact-resolver")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MavenArtifactResolver mavenArtifactResolver() {
        return new MavenArtifactResolver();
    }
}
