package com.project.Search.DTO;

import lombok.Data;
import java.util.List;

@Data
public class SearchResultDTO {

  private List<SearchQueryDTO> content;
  private PaginationDTO paginationDTO;

}
