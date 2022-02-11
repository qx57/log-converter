package log_converter.helpers;

import io.qameta.allure.internal.shadowed.jackson.databind.ObjectMapper;
import io.qameta.allure.internal.shadowed.jackson.databind.ObjectWriter;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestResult;
import log_converter.dto.reader.TestReport;
import log_converter.dto.settings.Settings;
import log_converter.exceptions.WriteException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class LogWriter {

    private final Settings settings;

    private List<TestResult> allureResults = new ArrayList<>();

    public LogWriter(Settings settings) {
        this.settings = settings;
    }

    public LogWriter convert(List<TestReport> reports) {
        allureResults = new ArrayList<>();
        for (TestReport report : reports) {
            TestResult itemAllureReport = new TestResult();
            itemAllureReport.setUuid(UUID.randomUUID().toString());
            itemAllureReport.setHistoryId(UUID.randomUUID().toString().replace("-", ""));
            itemAllureReport.setName(report.getName());
            itemAllureReport.setStatus(
                    report.getStatus().toString().equals("Error")
                            ? Status.BROKEN
                            : Status.fromValue(report.getStatus().toString().toLowerCase(Locale.ROOT))
            );
            StatusDetails allureDetails = new StatusDetails();
            allureDetails.setMessage(report.getLog());
            allureDetails.setTrace(report.getError());
            itemAllureReport.setStatusDetails(allureDetails);
            itemAllureReport.setStart(report.getStartTime());
            itemAllureReport.setStop(report.getStartTime() + report.getElapsed());
            allureResults.add(itemAllureReport);
        }
        return this;
    }

    public void write() {
        checkOutputDir();
        for (TestResult allureResult : allureResults) {
            try {
                writeToFile(allureResult);
            } catch (WriteException e) {
                System.out.println(String.format("Allure report for test '%s' non written",
                        allureResult.getName()));
            }
        }
    }

    private void checkOutputDir() {
        File outputDir = Paths.get(settings.getApp().getOutput().getPath()).toFile();
        if (!outputDir.exists()) {
            new File(settings.getApp().getOutput().getPath()).mkdirs();
        }
    }

    private void writeToFile(TestResult allureReport) throws WriteException {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            String json = objectWriter.writeValueAsString(allureReport);
            File allureReportFile = new File(String.format("%s/%s-result.json",
                    settings.getApp().getOutput().getPath(),
                    allureReport.getUuid()));
            FileOutputStream fileOutputStream = new FileOutputStream(allureReportFile);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(json);
            bufferedWriter.close();
        } catch (IOException e) {
            throw new WriteException("Can not write allure report file");
        }
    }
}
