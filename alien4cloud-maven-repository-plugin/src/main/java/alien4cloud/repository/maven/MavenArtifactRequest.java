package alien4cloud.repository.maven;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MavenArtifactRequest {

    private String groupId;

    private String artifactId;

    private String packaging;

    private String version;

    private String classifier;
}
