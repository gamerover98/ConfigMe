package ch.jalu.configme.wiki;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

public class CreatingSettingsManagerWikiTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    void s() {
        File configFile = TestUtils.copyFileFromResources("/empty_file.yml", temporaryFolder);

        SettingsManager settingsManager = SettingsManagerBuilder.withYamlFile(configFile)
            .configurationData(TitleConfig.class)
            .useDefaultMigrationService()
            .create();

        System.out.println("Title: " + settingsManager.getProperty(TitleConfig.TITLE_TEXT));

        settingsManager.setProperty(TitleConfig.TITLE_TEXT, "New title");
        System.out.println("Title: " + settingsManager.getProperty(TitleConfig.TITLE_TEXT));
    }
}
