package challenge.dto.inner;

import lombok.Data;

@Data
public class AggregatedAvailableCam {
    private Integer id;
    private String urlType;
    private String videoUrl;
    private String value;
    private Integer ttl;
}
