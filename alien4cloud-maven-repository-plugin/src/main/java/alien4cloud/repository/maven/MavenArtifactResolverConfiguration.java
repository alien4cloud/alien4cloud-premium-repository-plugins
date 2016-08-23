package alien4cloud.repository.maven;

import alien4cloud.repository.configuration.SimpleConfiguration;
import alien4cloud.ui.form.annotation.FormProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@FormProperties({ "url", "user", "password" })
public class MavenArtifactResolverConfiguration extends SimpleConfiguration {
}
