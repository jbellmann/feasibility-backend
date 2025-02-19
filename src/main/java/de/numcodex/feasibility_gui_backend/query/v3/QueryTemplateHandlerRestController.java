package de.numcodex.feasibility_gui_backend.query.v3;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.numcodex.feasibility_gui_backend.common.api.TermCode;
import de.numcodex.feasibility_gui_backend.query.QueryHandlerService;
import de.numcodex.feasibility_gui_backend.query.api.QueryTemplate;
import de.numcodex.feasibility_gui_backend.query.templates.QueryTemplateException;
import de.numcodex.feasibility_gui_backend.terminology.validation.TermCodeValidation;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/*
Rest Interface for the UI to send and receive query templates from the backend.
*/
@RequestMapping("api/v3/query/template")
@RestController("QueryTemplateHandlerRestController-v3")
@Slf4j
@CrossOrigin(origins = "${cors.allowedOrigins}", exposedHeaders = "Location")
public class QueryTemplateHandlerRestController {

  private final QueryHandlerService queryHandlerService;
  private final TermCodeValidation termCodeValidation;
  private final String apiBaseUrl;

  public QueryTemplateHandlerRestController(QueryHandlerService queryHandlerService,
      TermCodeValidation termCodeValidation, @Value("${app.apiBaseUrl}") String apiBaseUrl) {
    this.queryHandlerService = queryHandlerService;
    this.termCodeValidation = termCodeValidation;
    this.apiBaseUrl = apiBaseUrl;
  }

  @PostMapping(path = "")
  public ResponseEntity<Object> storeQueryTemplate(@Valid @RequestBody QueryTemplate queryTemplate,
      @Context HttpServletRequest httpServletRequest, Principal principal) {

    Long queryId;
    try {
      queryId = queryHandlerService.storeQueryTemplate(queryTemplate, principal.getName());
    } catch (QueryTemplateException e) {
      log.error("Error while storing queryTemplate", e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (DataIntegrityViolationException e) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    UriComponentsBuilder uriBuilder = (apiBaseUrl != null && !apiBaseUrl.isEmpty())
        ? ServletUriComponentsBuilder.fromUriString(apiBaseUrl)
        : ServletUriComponentsBuilder.fromRequestUri(httpServletRequest);

    var uriString = uriBuilder.replacePath("")
        .pathSegment("api", "v3", "query", "template", String.valueOf(queryId))
        .build()
        .toUriString();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(HttpHeaders.LOCATION, uriString);
    return new ResponseEntity<>(httpHeaders, HttpStatus.CREATED);
  }

  @GetMapping(path = "/{queryId}")
  public ResponseEntity<Object> getQueryTemplate(@PathVariable(value = "queryId") Long queryId,
      Principal principal) {

    try {
      var query = queryHandlerService.getQueryTemplate(queryId, principal.getName());
      var queryTemplate = queryHandlerService.convertTemplatePersistenceToApi(query);
      List<TermCode> invalidTermCodes = termCodeValidation.getInvalidTermCodes(
          queryTemplate.content());
      var queryTemplateWithInvalidTermCodes = QueryTemplate.builder()
              .id(queryTemplate.id())
              .content(queryTemplate.content())
              .label(queryTemplate.label())
              .comment(queryTemplate.comment())
              .lastModified(queryTemplate.lastModified())
              .createdBy(queryTemplate.createdBy())
              .invalidTerms(invalidTermCodes)
              .isValid(queryTemplate.isValid())
              .build();
      return new ResponseEntity<>(queryTemplateWithInvalidTermCodes, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (QueryTemplateException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(path = "")
  public ResponseEntity<Object> getQueryTemplates(Principal principal) {

    var queries = queryHandlerService.getQueryTemplatesForAuthor(principal.getName());
    var ret = new ArrayList<QueryTemplate>();
    queries.forEach(q -> {
      try {
        QueryTemplate convertedQuery = queryHandlerService.convertTemplatePersistenceToApi(q);
        var convertedQueryWithoutContent = QueryTemplate.builder()
                .id(convertedQuery.id())
                .label(convertedQuery.label())
                .comment(convertedQuery.comment())
                .lastModified(convertedQuery.lastModified())
                .createdBy(convertedQuery.createdBy())
                .invalidTerms(convertedQuery.invalidTerms())
                .isValid(convertedQuery.isValid())
                .build();
        ret.add(convertedQueryWithoutContent);
      } catch (JsonProcessingException e) {
        log.error("Error converting query");
      }
    });
    return new ResponseEntity<>(ret, HttpStatus.OK);
  }

  @PutMapping(path = "/{queryTemplateId}")
  public ResponseEntity<Object> updateQueryTemplate(@PathVariable(value = "queryTemplateId") Long queryTemplateId,
                                                    @Valid @RequestBody QueryTemplate queryTemplate,
                                                    Principal principal) {
    try {
      queryHandlerService.updateQueryTemplate(queryTemplateId, queryTemplate, principal.getName());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (QueryTemplateException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping(path = "/{queryTemplateId}")
  public ResponseEntity<Object> deleteQueryTemplate(@PathVariable(value = "queryTemplateId") Long queryTemplateId,
                                                    Principal principal) {
    try {
      queryHandlerService.deleteQueryTemplate(queryTemplateId, principal.getName());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (QueryTemplateException e) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(path = "/validate")
  public ResponseEntity<Object> validateTemplates(Principal principal) {

    var queries = queryHandlerService.getQueryTemplatesForAuthor(principal.getName());
    var ret = new ArrayList<QueryTemplate>();
    queries.forEach(q -> {
      try {
        QueryTemplate convertedQuery = queryHandlerService.convertTemplatePersistenceToApi(q);
        List<TermCode> invalidTermCodes = termCodeValidation.getInvalidTermCodes(
            convertedQuery.content());
        var convertedQueryWithoutContent = QueryTemplate.builder()
                .id(convertedQuery.id())
                .label(convertedQuery.label())
                .comment(convertedQuery.comment())
                .lastModified(convertedQuery.lastModified())
                .createdBy(convertedQuery.createdBy())
                .invalidTerms(invalidTermCodes)
                .isValid(convertedQuery.isValid())
                .build();
        ret.add(convertedQueryWithoutContent);
      } catch (JsonProcessingException e) {
        log.error("Error converting query");
      }
    });
    return new ResponseEntity<>(ret, HttpStatus.OK);
  }

}
