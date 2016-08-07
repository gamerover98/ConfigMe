package com.github.authme.configme.resource;

import com.github.authme.configme.SettingsManager;
import com.github.authme.configme.TestUtils;
import com.github.authme.configme.exception.ConfigMeException;
import com.github.authme.configme.properties.Property;
import com.github.authme.configme.propertymap.PropertyEntry;
import com.github.authme.configme.propertymap.PropertyMap;
import com.github.authme.configme.samples.TestConfiguration;
import com.github.authme.configme.samples.TestEnum;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.authme.configme.properties.PropertyInitializer.newProperty;
import static com.github.authme.configme.samples.TestSettingsMigrationServices.checkAllPropertiesPresent;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link YamlFileResource} and {@link YamlFileReader}.
 */
public class YamlFileResourceTest {

    private static final String COMPLETE_FILE = "/config-sample.yml";
    private static final String INCOMPLETE_FILE = "/config-incomplete-sample.yml";
    private static final String DIFFICULT_FILE = "/config-difficult-values.yml";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void shouldThrowForAbsentYamlMap() throws IOException {
        // given
        File file = temporaryFolder.newFile();
        Files.write(file.toPath(), "123".getBytes());

        // when / then
        try {
            new YamlFileResource(file);
            fail("Expected exception to be thrown");
        } catch (ConfigMeException e) {
            assertThat(e.getMessage(), containsString("Top-level is not a map"));
        }
    }

    @Test
    public void shouldWrapIOException() throws IOException {
        // given
        File folder = temporaryFolder.newFolder();
        File file = new File(folder, "test");

        // when / then
        try {
            new YamlFileResource(file);
            fail("Expected exception to be thrown");
        } catch (ConfigMeException e) {
            assertThat(e.getMessage(), containsString("Could not read file"));
        }
    }

    @Test
    public void shouldReadAllProperties() {
        // given
        File config = copyFileFromResources(COMPLETE_FILE);

        // when
        PropertyResource resource = new YamlFileResource(config);

        // then
        Map<Property<?>, Object> expected = new HashMap<>();
        expected.put(TestConfiguration.DURATION_IN_SECONDS, 22);
        expected.put(TestConfiguration.SYSTEM_NAME, "Custom sys name");
        expected.put(TestConfiguration.RATIO_ORDER, TestEnum.FIRST);
        expected.put(TestConfiguration.RATIO_FIELDS, Arrays.asList("Australia", "Burundi", "Colombia"));
        expected.put(TestConfiguration.VERSION_NUMBER, 2492);
        expected.put(TestConfiguration.SKIP_BORING_FEATURES, false);
        expected.put(TestConfiguration.BORING_COLORS, Arrays.asList("beige", "gray"));
        expected.put(TestConfiguration.DUST_LEVEL, 2);
        expected.put(TestConfiguration.USE_COOL_FEATURES, true);
        expected.put(TestConfiguration.COOL_OPTIONS, Arrays.asList("Dinosaurs", "Explosions", "Big trucks"));

        for (Map.Entry<Property<?>, Object> entry : expected.entrySet()) {
            assertThat("Property '" + entry.getKey().getPath() + "' has expected value",
                entry.getKey().getValue(resource), equalTo(entry.getValue()));
        }
    }

    @Test
    public void shouldWriteMissingProperties() {
        // given
        File file = copyFileFromResources(INCOMPLETE_FILE);
        YamlFileResource resource = new YamlFileResource(file);
        PropertyMap propertyMap = TestConfiguration.generatePropertyMap();

        // when
        resource.exportProperties(propertyMap);

        // then
        // Load file again to make sure what we wrote can be read again
        resource = new YamlFileResource(file);
        Map<Property<?>, Object> expected = new HashMap<>();
        expected.put(TestConfiguration.DURATION_IN_SECONDS, 22);
        expected.put(TestConfiguration.SYSTEM_NAME, "[TestDefaultValue]");
        expected.put(TestConfiguration.RATIO_ORDER, "SECOND");
        expected.put(TestConfiguration.RATIO_FIELDS, Arrays.asList("Australia", "Burundi", "Colombia"));
        expected.put(TestConfiguration.VERSION_NUMBER, 32046);
        expected.put(TestConfiguration.SKIP_BORING_FEATURES, false);
        expected.put(TestConfiguration.BORING_COLORS, Collections.EMPTY_LIST);
        expected.put(TestConfiguration.DUST_LEVEL, -1);
        expected.put(TestConfiguration.USE_COOL_FEATURES, false);
        expected.put(TestConfiguration.COOL_OPTIONS, Arrays.asList("Dinosaurs", "Explosions", "Big trucks"));
        for (Map.Entry<Property<?>, Object> entry : expected.entrySet()) {
            // Check with resource#getObject to make sure the values were persisted to the file
            // If we go through Property objects they may fall back to their default values
            String propertyPath = entry.getKey().getPath();
            assertThat("Property '" + propertyPath + "' has expected value",
                resource.getObject(propertyPath), equalTo(entry.getValue()));
        }
    }

    /** Verifies that "difficult cases" such as apostrophes in strings etc. are handled properly. */
    @Test
    public void shouldProperlyExportAnyValues() {
        // given
        File file = copyFileFromResources(DIFFICULT_FILE);
        YamlFileResource resource = new YamlFileResource(file);

        // Additional string properties
        List<Property<String>> additionalProperties = Arrays.asList(
            newProperty("more.string1", "it's a text with some \\'apostrophes'"),
            newProperty("more.string2", "\tthis one\nhas some\nnew '' lines-test"));
        List<PropertyEntry> entries = TestConfiguration.generatePropertyMap().getEntries();
        for (Property<?> property : additionalProperties) {
            entries.add(new PropertyEntry(property));
        }
        PropertyMap propertyMap = mock(PropertyMap.class);
        given(propertyMap.getEntries()).willReturn(entries);

        // when
        new SettingsManager(propertyMap, resource, checkAllPropertiesPresent());
        // Save and load again
        resource.exportProperties(propertyMap);
        resource.reload();

        // then
        assertThat(resource.getObject(TestConfiguration.DUST_LEVEL.getPath()), not(nullValue()));

        Map<Property<?>, Object> expected = new HashMap<>();
        expected.put(TestConfiguration.DURATION_IN_SECONDS, 20);
        expected.put(TestConfiguration.SYSTEM_NAME, "A 'test' name");
        expected.put(TestConfiguration.RATIO_ORDER, "FOURTH");
        expected.put(TestConfiguration.RATIO_FIELDS, Arrays.asList("Australia\\", "\tBurundi'", "Colombia?\n''"));
        expected.put(TestConfiguration.VERSION_NUMBER, -1337);
        expected.put(TestConfiguration.SKIP_BORING_FEATURES, false);
        expected.put(TestConfiguration.BORING_COLORS, Arrays.asList("it's a difficult string!", "gray\nwith new lines\n"));
        expected.put(TestConfiguration.DUST_LEVEL, -1);
        expected.put(TestConfiguration.USE_COOL_FEATURES, true);
        expected.put(TestConfiguration.COOL_OPTIONS, Collections.EMPTY_LIST);
        expected.put(additionalProperties.get(0), additionalProperties.get(0).getDefaultValue());
        expected.put(additionalProperties.get(1), additionalProperties.get(1).getDefaultValue());

        for (Map.Entry<Property<?>, Object> entry : expected.entrySet()) {
            assertThat("Property '" + entry.getKey().getPath() + "' has expected value",
                resource.getObject(entry.getKey().getPath()), equalTo(entry.getValue()));
        }
    }

    @Test
    public void shouldReloadValues() throws IOException {
        // given
        File file = temporaryFolder.newFile();
        YamlFileResource resource = new YamlFileResource(file);
        Files.copy(TestUtils.getJarPath(COMPLETE_FILE), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // when
        assertThat(TestConfiguration.RATIO_ORDER.getValue(resource), equalTo(TestEnum.SECOND)); // default value
        resource.reload();

        // then
        assertThat(TestConfiguration.RATIO_ORDER.getValue(resource), equalTo(TestEnum.FIRST));
    }

    @Test
    public void shouldRetrieveTypedValues() {
        // given
        File file = copyFileFromResources(COMPLETE_FILE);
        YamlFileResource resource = new YamlFileResource(file);

        // when / then
        assertThat(resource.getBoolean(TestConfiguration.DURATION_IN_SECONDS.getPath()), nullValue());
        assertThat(resource.getString(TestConfiguration.DURATION_IN_SECONDS.getPath()), nullValue());
        assertThat(resource.getDouble(TestConfiguration.DURATION_IN_SECONDS.getPath()), equalTo(22.0));
    }

    @Test
    public void shouldSetValuesButNotPersist() {
        // given
        File file = copyFileFromResources(INCOMPLETE_FILE);
        YamlFileResource resource = new YamlFileResource(file);

        // when
        assertThat(TestConfiguration.RATIO_ORDER.getValue(resource), equalTo(TestEnum.SECOND)); // default value
        resource.setValue(TestConfiguration.RATIO_ORDER.getPath(), TestEnum.THIRD);
        resource.setValue(TestConfiguration.SKIP_BORING_FEATURES.getPath(), true);

        // then
        assertThat(TestConfiguration.RATIO_ORDER.getValue(resource), equalTo(TestEnum.THIRD));
        assertThat(TestConfiguration.SKIP_BORING_FEATURES.getValue(resource), equalTo(true));

        // when (2) - reload without saving, so will fallback to default again
        resource.reload();

        // then
        assertThat(TestConfiguration.RATIO_ORDER.getValue(resource), equalTo(TestEnum.SECOND));
        assertThat(TestConfiguration.SKIP_BORING_FEATURES.getValue(resource), equalTo(false));
    }

    private File copyFileFromResources(String path) {
        try {
            Path source = TestUtils.getJarPath(path);
            File destination = temporaryFolder.newFile();
            Files.copy(source, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destination;
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy test file", e);
        }
    }
}
