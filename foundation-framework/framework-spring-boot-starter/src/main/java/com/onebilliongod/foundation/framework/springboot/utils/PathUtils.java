package com.onebilliongod.foundation.framework.springboot.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public final class PathUtils {
    public static boolean ensureDirectory(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path pathNew = Files.createDirectories(path);
            return Files.isWritable(pathNew);
        } catch (IOException e) {
            log.error("file createDirectories find error:" + e.getMessage());
            return false;
        }
    }
}
