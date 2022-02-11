package log_converter;

import log_converter.dto.reader.TestReport;
import log_converter.dto.settings.Settings;
import log_converter.helpers.LogReader;
import log_converter.helpers.LogWriter;
import log_converter.helpers.SettingsReader;

import java.util.List;

public class LogReporter {

    public static void main(String... args) {
        // Read the settings
        Settings settings = new SettingsReader().getSettings();
        // Read logs
        List<TestReport> reports = new LogReader(settings).readLogs();
        // Write allure reports
        new LogWriter(settings).convert(reports).write();
    }
}
