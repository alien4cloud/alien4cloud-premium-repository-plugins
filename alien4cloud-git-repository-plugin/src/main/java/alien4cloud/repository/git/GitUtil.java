package alien4cloud.repository.git;

public class GitUtil {

    public static boolean isGitURL(String reference) {
        // Git offers a wide variety of protocol https://git-scm.com/book/en/v2/Git-on-the-Server-The-Protocols
        // It events works in local so better not check the scheme of reference
        return reference.endsWith(".git");
    }
}
