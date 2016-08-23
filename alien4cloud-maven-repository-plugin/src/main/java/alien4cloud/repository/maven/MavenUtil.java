package alien4cloud.repository.maven;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MavenUtil {

    public static boolean isValidMavenRepository(String repositoryURI) {
        try {
            // A maven repository can be on the file system
            // In this case the remote repository will be in the form of file://path/to/maven/repo
            // It must be an absolute uri
            return new URI(repositoryURI).isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static RepositorySystem newRepositorySystem() {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, WagonTransporterFactory.class);
        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                log.error(
                        "Could not create maven repository system due to service implementation creation failure " + impl.getName() + " for " + type.getName(),
                        exception);
            }
        });
        return locator.getService(RepositorySystem.class);
    }

    public static RepositorySystemSession newSession(Path localRepositoryPath, RepositorySystem system) {
        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository(localRepositoryPath.toString());
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

        session.setTransferListener(new AbstractTransferListener() {
            @Override
            public void transferSucceeded(TransferEvent event) {
                log.info("Transfer succeeded {}", event);
            }

            @Override
            public void transferFailed(TransferEvent event) {
                log.warn("Transfer failed", event.getException());
            }

            @Override
            public void transferCorrupted(TransferEvent event) {
                log.warn("Transfer corrupted", event.getException());
            }
        });
        session.setRepositoryListener(new AbstractRepositoryListener() {
        });
        return session;
    }

    private static String convertToAetherCompatibleFormat(String groupId, String artifactId, String classifier, String packaging, String version) {
        return groupId + ":" + artifactId + (StringUtils.isNotBlank(packaging) ? ":" + packaging : "")
                + (StringUtils.isNotBlank(classifier) ? ":" + classifier : "") + ":" + version;
    }

    public static Artifact resolveMavenArtifact(RepositorySystem system, RepositorySystemSession session, String url, String credentials,
            MavenArtifactRequest request) throws ArtifactResolutionException, VersionRangeResolutionException {
        RemoteRepository.Builder remoteRepositoryBuilder;
        if (StringUtils.isNotBlank(url)) {
            remoteRepositoryBuilder = new RemoteRepository.Builder(null, "default", url);
        } else {
            remoteRepositoryBuilder = new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/");
        }
        if (StringUtils.isNotBlank(credentials)) {
            int indexOfSeparator = credentials.indexOf(':');
            AuthenticationBuilder authenticationBuilder = new AuthenticationBuilder();
            String user = credentials.substring(0, indexOfSeparator);
            String password = credentials.substring(indexOfSeparator + 1, credentials.length());
            authenticationBuilder.addUsername(user);
            authenticationBuilder.addPassword(password);
            remoteRepositoryBuilder.setAuthentication(authenticationBuilder.build());
        }
        RemoteRepository remoteRepository = remoteRepositoryBuilder.build();
        Artifact artifact = new DefaultArtifact(convertToAetherCompatibleFormat(request.getGroupId(), request.getArtifactId(), request.getClassifier(),
                request.getPackaging(), request.getVersion()));
        if (artifact.getBaseVersion().startsWith("[") || artifact.getBaseVersion().startsWith("(")) {
            VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, Collections.singletonList(remoteRepository), null);
            VersionRangeResult versionResult = system.resolveVersionRange(session, versionRangeRequest);
            if (versionResult.getHighestVersion() == null) {
                throw new VersionRangeResolutionException(versionResult, "No version found for range " + artifact.getBaseVersion());
            }
            String highestVersion = versionResult.getHighestVersion().toString();
            artifact = new DefaultArtifact(convertToAetherCompatibleFormat(request.getGroupId(), request.getArtifactId(), request.getClassifier(),
                    request.getPackaging(), highestVersion));
        }
        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(Collections.singletonList(remoteRepository));
        ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
        return artifactResult.getArtifact();
    }
}