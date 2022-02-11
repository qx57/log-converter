package log_converter.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import log_converter.dto.settings.Settings;
import log_converter.exceptions.SettingsException;

import java.io.File;
import java.io.IOException;

public class SettingsReader {

    private Settings settings = null;

    public Settings getSettings() {
        if (settings == null) {
            try {
                settings = readSettings();
            } catch (SettingsException e) {
                settings = null;
            }
        }
        return settings;
    }

    public Settings readSettings() throws SettingsException {
        File file = new File(getClass().getClassLoader().getResource("settings.yaml").getFile());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(file, Settings.class);
        } catch (IOException e) {
            throw new SettingsException("Can't read settings file");
        }
    }
}
