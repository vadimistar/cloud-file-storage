package com.vadimistar.cloudfilestorage.common.utils;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@UtilityClass
public class StreamUtils {

    public static <T> Stream<T> asStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), IS_STREAM_PARALLEL);
    }

    private static final boolean IS_STREAM_PARALLEL = false;
}
