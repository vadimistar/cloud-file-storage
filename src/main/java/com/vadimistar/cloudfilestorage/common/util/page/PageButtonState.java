package com.vadimistar.cloudfilestorage.common.util.page;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PageButtonState {
    ACTIVE("active"),
    INACTIVE(""),
    DISABLED("disabled");

    private final String styleClass;
}
