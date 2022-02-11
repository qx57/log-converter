package log_converter.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class App {

    @JsonProperty
    private Input input;

    @JsonProperty
    private Output output;
}
