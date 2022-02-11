package log_converter.dto.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Settings {

    @JsonProperty
    private App app;
}

