package ch.jalu.configme.wiki;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

public class TitleConfig implements SettingsHolder {

    public static final Property<String> TITLE_TEXT =
        newProperty("title.text", "-Default-");

    public static final Property<Integer> TITLE_SIZE =
        newProperty("title.size", 10);

    private TitleConfig() {
        // prevent instantiation
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("title", "This section defines the title that will be displayed");
    }
}
