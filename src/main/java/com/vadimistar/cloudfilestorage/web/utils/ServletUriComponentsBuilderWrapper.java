package com.vadimistar.cloudfilestorage.web.utils;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@UtilityClass
public class ServletUriComponentsBuilderWrapper {

    public static ServletUriComponentsBuilder fromCurrentRequest() {
        return ServletUriComponentsBuilder.fromCurrentRequest();
    }
}
