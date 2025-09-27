/*
 * This file is part of faker - https://github.com/o1seth/faker
 * Copyright (C) 2024 o1seth
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.faker.util;

import net.java.faker.AppInfo;
import net.java.faker.ui.Window;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

public class Util {

    public static ImageIcon getResourceImageIcon(String path) {
        URL url = locateResource(path);
        if (url == null) {
            return null;
        }
        return new ImageIcon(url);
    }

    public static Image getResourceImage(String res) {
        URL url = locateResource(res);
        if (url == null) {
            return null;
        }
        return Toolkit.getDefaultToolkit().getImage(url);
    }

    public static byte[] getResourceBytes(String res) {
        for (String candidate : candidatePaths(res)) {
            try (InputStream is = Util.class.getResourceAsStream(candidate)) {
                if (is == null) {
                    continue;
                }
                byte[] arrBuffer = new byte[16384];
                ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
                int read;
                while ((read = is.read(arrBuffer)) != -1) {
                    baos.write(arrBuffer, 0, read);
                }
                return baos.toByteArray();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static URL locateResource(String path) {
        for (String candidate : candidatePaths(path)) {
            URL url = Window.class.getResource(candidate);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    private static String[] candidatePaths(String path) {
        if (path == null || path.isEmpty()) {
            return new String[0];
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalized);
        if (normalized.startsWith(AppInfo.LEGACY_RESOURCE_ROOT)) {
            candidates.add(normalized.replace(AppInfo.LEGACY_RESOURCE_ROOT, AppInfo.RESOURCE_ROOT));
        } else if (normalized.startsWith(AppInfo.RESOURCE_ROOT)) {
            candidates.add(normalized.replace(AppInfo.RESOURCE_ROOT, AppInfo.LEGACY_RESOURCE_ROOT));
        } else {
            candidates.add(AppInfo.RESOURCE_ROOT + normalized);
            candidates.add(AppInfo.LEGACY_RESOURCE_ROOT + normalized);
        }
        return candidates.toArray(new String[0]);
    }
}
