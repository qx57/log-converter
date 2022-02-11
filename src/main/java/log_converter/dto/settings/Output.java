package log_converter.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Output {

    @JsonProperty
    private String path;
}
