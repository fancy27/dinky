/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.dinky.data.constant.DirConstant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;

class URLUtilsTest {

    @TempDir
    Path tempDir;

    @Test
    void clearRsLocalCacheDeletesCachedFile() {
        String fullName = "urlutils-test/" + UUID.randomUUID() + "/app.jar";
        File cached = FileUtil.file(DirConstant.getTempRootDir(), "rs", fullName);
        FileUtil.writeUtf8String("stale-jar-content", cached);

        assertThat(cached).exists();
        URLUtils.clearRsLocalCache(fullName);
        assertThat(cached).doesNotExist();
    }

    @Test
    void clearRsLocalCacheIgnoresBlankName() {
        URLUtils.clearRsLocalCache(" ");
        URLUtils.clearRsLocalCache(null);
    }

    @Test
    void rewriteRsLocalFileReplacesSamePathContent() {
        File target = tempDir.resolve("app.bin").toFile();

        URLUtils.rewriteRsLocalFile(utf8Stream("old-content"), target);
        assertThat(FileUtil.readUtf8String(target)).isEqualTo("old-content");

        URLUtils.rewriteRsLocalFile(utf8Stream("new"), target);
        assertThat(FileUtil.readUtf8String(target)).isEqualTo("new");
    }

    @Test
    void rewriteRsLocalFileNewJarFileSeesUpdatedEntryWhileOldHandleOpen() throws IOException {
        File target = tempDir.resolve("app.jar").toFile();
        File v2Jar = tempDir.resolve("v2.jar").toFile();
        writeJar(target, "marker.txt", "v1");
        writeJar(v2Jar, "marker.txt", "v2");

        try (JarFile oldJar = new JarFile(target)) {
            assertThat(readJarEntry(oldJar, "marker.txt")).isEqualTo("v1");

            try (InputStream in = new FileInputStream(v2Jar)) {
                URLUtils.rewriteRsLocalFile(in, target);
            }

            try (JarFile newJar = new JarFile(target)) {
                assertThat(readJarEntry(newJar, "marker.txt")).isEqualTo("v2");
            }
        }
    }

    private static ByteArrayInputStream utf8Stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeJar(File file, String entryName, String content) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(file))) {
            jos.putNextEntry(new JarEntry(entryName));
            jos.write(content.getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
        }
    }

    private static String readJarEntry(JarFile jarFile, String entryName) throws IOException {
        try (InputStream in = jarFile.getInputStream(jarFile.getJarEntry(entryName))) {
            return IoUtil.readUtf8(in);
        }
    }
}
