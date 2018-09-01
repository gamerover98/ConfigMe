package ch.jalu.configme;

import ch.jalu.configme.configurationdata.ConfigurationData;
import ch.jalu.configme.configurationdata.ConfigurationDataBuilder;
import ch.jalu.configme.migration.MigrationService;
import ch.jalu.configme.migration.PlainMigrationService;
import ch.jalu.configme.resource.PropertyResource;
import ch.jalu.configme.resource.YamlFileResource;
import ch.jalu.configme.utils.Utils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

/**
 * Creates {@link SettingsManager} instances.
 */
public final class SettingsManagerBuilder {

    private final PropertyResource resource;
    private ConfigurationData configurationData;
    private MigrationService migrationService = new PlainMigrationService();

    private SettingsManagerBuilder(PropertyResource resource) {
        this.resource = resource;
    }

    /**
     * Creates a builder, using the given YAML file to use as property resource.
     *
     * @param file the yaml file to use
     * @return settings manager builder
     */
    public static SettingsManagerBuilder withYamlFile(File file) {
        Utils.createFileIfNotExists(file);
        return new SettingsManagerBuilder(new YamlFileResource(file));
    }

    /**
     * Creates a new builder with the given property resource.
     *
     * @param resource the resource to use
     * @return settings manager builder
     */
    public static SettingsManagerBuilder withResource(PropertyResource resource) {
        return new SettingsManagerBuilder(resource);
    }

    /**
     * Sets up configuration data with the input of the given settings holder classes.
     *
     * @param classes the settings holder classes
     * @return this builder
     */
    @SafeVarargs
    public final SettingsManagerBuilder configurationData(Class<? extends SettingsHolder>... classes) {
        this.configurationData = ConfigurationDataBuilder.createConfiguration(classes);
        return this;
    }

    /**
     * Sets the provided configuration data to the builder.
     *
     * @param configurationData the configuration data
     * @return this builder
     */
    public SettingsManagerBuilder configurationData(ConfigurationData configurationData) {
        this.configurationData = configurationData;
        return this;
    }

    /**
     * Sets the given migration service to the builder.
     *
     * @param migrationService the migration service to use (or null)
     * @return this builder
     */
    public SettingsManagerBuilder migrationService(@Nullable MigrationService migrationService) {
        this.migrationService = migrationService;
        return this;
    }

    /**
     * Creates a settings manager instance. It is mandatory that resource and configuration data have been
     * configured beforehand.
     *
     * @return the settings manager
     */
    public SettingsManager create() {
        Objects.requireNonNull(configurationData, "configurationData");
        Objects.requireNonNull(resource, "resource");
        return new SettingsManagerImpl(resource, configurationData, migrationService);
    }
}