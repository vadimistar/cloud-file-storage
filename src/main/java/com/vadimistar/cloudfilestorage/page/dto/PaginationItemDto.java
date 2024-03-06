package com.vadimistar.cloudfilestorage.page.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationItemDto {
    private String href;
    private String text;
    private PaginationItemState state;
}
