package com.vadimistar.cloudfilestorage.common.util.path;

import com.vadimistar.cloudfilestorage.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class PathDepthComparator implements Comparator<String> {

    @Override
    public int compare(String s1, String s2) {
        long depth1 = getDepth(s1);
        long depth2 = getDepth(s2);
        return Long.compare(depth1, depth2);
    }

    private static long getDepth(String path) {
        return StringUtils.count(path, '/');
    }
}
