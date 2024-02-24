package com.vadimistar.cloudfilestorage.utils;

import com.vadimistar.cloudfilestorage.dto.FolderDto;
import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class NavigationFoldersParser {

    public static List<FolderDto> parseNavigationFolders(String path) {
        List<FolderDto> result = new ArrayList<>();

        FolderDto homeFolder = new FolderDto("/", "/");
        result.add(homeFolder);

        String[] pathParts = path.split("/");

        StringBuilder pathBuilder = new StringBuilder("/?path=");
        for (int i = 0; i < pathParts.length - 1; i ++) {
            pathBuilder.append(URLUtils.encode(pathParts[i]));
            result.add(new FolderDto(pathParts[i], pathBuilder.toString()));
            pathBuilder.append(URLUtils.encode("/"));
        }

        result.add(new FolderDto(pathParts[pathParts.length - 1], path));

        return result;
    }
}
