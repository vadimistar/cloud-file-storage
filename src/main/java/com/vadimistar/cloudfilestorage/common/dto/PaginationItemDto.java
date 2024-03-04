package com.vadimistar.cloudfilestorage.common.dto;

import com.vadimistar.cloudfilestorage.common.util.PaginationItemState;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaginationItemDto {
    private String href;
    private String text;
    private PaginationItemState state;
}
