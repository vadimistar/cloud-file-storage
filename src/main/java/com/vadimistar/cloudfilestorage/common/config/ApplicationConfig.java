package com.vadimistar.cloudfilestorage.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ApplicationConfig {

    @Value("${cloudfilestorage.index.page-size}")
    private int indexPageSize;

    @Value("${cloudfilestorage.search.page-size}")
    private int searchPageSize;
}
