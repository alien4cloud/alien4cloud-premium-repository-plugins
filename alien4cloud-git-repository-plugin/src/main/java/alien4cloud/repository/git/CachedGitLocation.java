package alien4cloud.repository.git;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public class CachedGitLocation {
    String repositoryURL;
    String user;
    String password;
    String branch;
}
