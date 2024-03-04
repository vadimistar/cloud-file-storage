package com.vadimistar.cloudfilestorage.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaginationItemState {
    ACTIVE("active"),
    INACTIVE(""),
    DISABLED("disabled");

    private final String styleClass;
}
