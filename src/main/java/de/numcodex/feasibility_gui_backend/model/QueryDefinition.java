package de.numcodex.feasibility_gui_backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueryDefinition {

  @JsonProperty
  private URI version;
  @JsonProperty("inclusionCriteria")
  private List<List<Criterion>> inclusionCriteria;
  @JsonProperty("exclusionCriteria")
  private List<List<Criterion>> exclusionCriteria;
  @JsonProperty("display")
  private String display;
}
