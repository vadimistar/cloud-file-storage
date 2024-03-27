package com.vadimistar.cloudfilestorage.search.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SearchConfig {

    @Value("${cloudfilestorage.search.page-size}")
    private Integer pageSize;
}
