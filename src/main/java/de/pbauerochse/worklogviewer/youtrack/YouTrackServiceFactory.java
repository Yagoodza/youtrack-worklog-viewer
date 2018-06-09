package de.pbauerochse.worklogviewer.youtrack;

import com.google.common.collect.ImmutableSet;
import de.pbauerochse.worklogviewer.settings.Settings;
import de.pbauerochse.worklogviewer.settings.SettingsUtil;
import de.pbauerochse.worklogviewer.youtrack.v20174.UrlBuilder;
import de.pbauerochse.worklogviewer.youtrack.v20174.YouTrackServiceV20174;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static de.pbauerochse.worklogviewer.util.ExceptionUtil.getIllegalArgumentException;

/**
 * Factory to get the YouTrackService
 * configured in the settings properties
 */
public class YouTrackServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(YouTrackServiceFactory.class);

    private static final Set<YouTrackService> AVAILABLE_SERVICE_IMPLEMENTATIONS = ImmutableSet.of(
            new YouTrackServiceV20174(new UrlBuilder(
                    SettingsUtil.getSettings().getYouTrackConnectionSettings()::getUrl,
                    SettingsUtil.getSettings().getYouTrackConnectionSettings()::getVersion
            ))
    );

    private static YouTrackService cachedInstance = null;

    public static YouTrackService getInstance() {

        Settings settings = SettingsUtil.getSettings();

        if (authenticationMethodChanged(settings)) {
            cachedInstance = getYouTrackService(settings.getYouTrackConnectionSettings().getVersion());

            LOGGER.info("Created new YouTrackService instance of type {}", cachedInstance.getClass().getSimpleName());
        }

        return cachedInstance;
    }

    private static YouTrackService getYouTrackService(YouTrackVersion version) {
        return AVAILABLE_SERVICE_IMPLEMENTATIONS.stream()
                .filter(service -> service.getSupportedVersions().contains(version))
                .findFirst()
                .orElseThrow(() -> getIllegalArgumentException("exceptions.settings.version.invalid", version.name()));
    }

    private static boolean authenticationMethodChanged(Settings settings) {
        return cachedInstance == null || !cachedInstance.getSupportedVersions().contains(settings.getYouTrackConnectionSettings().getVersion());
    }

}
