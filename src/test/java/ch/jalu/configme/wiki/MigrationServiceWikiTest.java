package ch.jalu.configme.wiki;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.resource.PropertyReader;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class MigrationServiceWikiTest {

    public static final Property<Boolean> LOG_ERRORS = newProperty("system.logErrorOccurrence", true);

    public static final Property<Boolean> USE_LIGHT_MODE = newProperty("appearance.light", true);

    public static final Property<Double> BRIGHTNESS_GRADIENT = newProperty("appearance.brightness", 1.0);

    private static class MyMigrationService extends PlainMigrationService {

        @Override
        protected boolean performMigrations(PropertyReader reader, ConfigurationData configurationData) {
            Property<Boolean> oldErrorLogProperty = newProperty("system.logErrorOccurance", true);
            return moveProperty(oldErrorLogProperty, LOG_ERRORS, reader, configurationData);
        }
    }

    private static class AppMigrationService extends PlainMigrationService {

        @Override
        protected boolean performMigrations(PropertyReader reader,
                                            ConfigurationData configurationData) {
            if (!reader.contains(BRIGHTNESS_GRADIENT.getPath())) {
                Boolean oldUseLightModeValue = reader.getBoolean("appearance.light");
                if (oldUseLightModeValue != null) {
                    double newGradientValue = oldUseLightModeValue ? 1.0 : 0.0;
                    configurationData.setValue(BRIGHTNESS_GRADIENT, newGradientValue);
                }
                return MIGRATION_REQUIRED;
            }
            return NO_MIGRATION_NEEDED;
        }
    }
}
