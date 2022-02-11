package log_converter.helpers;

import log_converter.dto.reader.TestReport;
import log_converter.dto.settings.Settings;
import log_converter.enums.Status;
import log_converter.exceptions.ReadFileException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogReader {

    private final Settings settings;

    public LogReader(Settings settings) {
        this.settings = settings;
    }

    public List<TestReport> readLogs() {
        List<TestReport> reports = new ArrayList<>();
        // Get files list
        List<File> files = getFiles(settings.getApp().getInput().getPath());
        if (files.size() > 0) {
            // read and serialize reports
            for (File file : files) {
                TestReport itemReport = readReport(file);
                if (itemReport != null) {
                    reports.add(itemReport);
                }
            }
        }
        return reports;
    }

    private List<File> getFiles(String path) {
        List<File> results = new ArrayList<>();
        File inputFolder = Paths.get(path).toFile();
        File[] allFiles = inputFolder.listFiles();
        for (File item : Arrays.asList(allFiles)) {
            if (item.isFile()) {
                results.add(item);
            } else if (item.isDirectory()) {
                for (File i : getFiles(item.getPath())) {
                    results.add(i);
                }
            }
        }
        return results;
    }

    private TestReport readReport(File file) {
        // Read file
        String reportString;
        try {
            reportString = readFile(file);
        } catch (ReadFileException re) {
            return null;
        }
        // create TestReport
        return parseReport(reportString, file);
    }

    private String readFile(File file) throws ReadFileException {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String tmpString;
            while ((tmpString = br.readLine()) != null) {
                stringBuilder.append(tmpString).append("\n");
            }
        } catch (IOException e) {
            throw new ReadFileException("Can not read test file: " + file.toString());
        }
        return stringBuilder.toString();
    }

    private TestReport parseReport(String report, File file) {
        String testName = getStringByRegex(report, "[.]*test\\=([\\w\\d]*)[.]*");
        testName = testName.contains("=") ? testName.split("=")[1] : testName;
        String elapsedString = getStringByRegex(report, "[.]*elapsed\\=([\\d]*) [.]*");
        Long elapsed = elapsedString.contains("=")
                ? Long.valueOf(elapsedString.split("=")[1].trim())
                : Long.valueOf(0);
        String testResult = getStringByRegex(report, "test result: ([\\w]+)");
        testResult = testResult.contains(":") ? testResult.split(":")[1].trim() : testResult;
        Long startTimeMilli = getEpoch(report);
        String logs = getLogs(report);
        String error = getError(report);
        return new TestReport(
                file.getName().replace(".log", ""),
                startTimeMilli,
                elapsed,
                testName,
                logs,
                error,
                !testResult.isEmpty() ? Status.valueOf(testResult) : Status.Error);
    }

    private String getStringByRegex(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private Long getEpoch(String report) {
        Long result = Long.valueOf(0);
        Matcher matcher = Pattern
                .compile("\\#([\\w]{3}) ([\\w]{3}) ([\\d]{1,2}) ([\\d]{1,2})\\:([\\d]{2})\\:([\\d]{2}) [\\w]{3} ([\\d]{4})")
                .matcher(report);
        if (matcher.find()) {
            String[] dateArr = matcher.group().replace("#", "").split(" ");
            String dateString = String.format("%s %s %s %s", dateArr[1], dateArr[2], dateArr[3], dateArr[5]);
            result = LocalDateTime.parse(dateString,
                            DateTimeFormatter.ofPattern("MMM dd HH:mm:ss yyyy", Locale.US))
                    .atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
        }
        return result;
    }

    private String getLogs(String report) {
        return report.split("----------log----------")[1].split("----------err----------")[0].trim();
    }

    private String getError(String report) {
        String error = "";
        String[] parts = report.split("----------err----------")[1].split("\n");
        for (String errString : parts) {
            if (errString.isEmpty()) break;
            error += errString;
        }
        return error;
    }
}
