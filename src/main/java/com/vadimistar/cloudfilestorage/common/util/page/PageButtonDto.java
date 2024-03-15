package com.vadimistar.cloudfilestorage.common.util.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PageButtonDto {
    @Builder.Default
    private String uri = "";
    private String text;
    private PageButtonState state;
}
