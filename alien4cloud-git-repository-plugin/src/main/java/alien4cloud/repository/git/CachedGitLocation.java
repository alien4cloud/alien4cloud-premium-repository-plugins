package alien4cloud.repository.git;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CachedGitLocation {
    String repositoryURL;
    String user;
    String password;
    String branch;
}
