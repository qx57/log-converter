package log_converter.dto.reader;

import log_converter.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestReport {

    private Long startTime;
    private Long elapsed;
    private String name;
    private String log;
    private String error;
    private Status status;
}
