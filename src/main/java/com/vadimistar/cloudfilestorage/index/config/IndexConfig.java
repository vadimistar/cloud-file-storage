package com.vadimistar.cloudfilestorage.index.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class IndexConfig {

    @Value("cloudfilestorage.index.page-size")
    private Integer pageSize;
}
