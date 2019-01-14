/**
 * Copyright (C) 2018 Ryszard Wiśniewski <brut.alll@gmail.com>
 * Copyright (C) 2018 Connor Tumbleson <connor.tumbleson@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brut.util;

import org.apache.commons.io.IOUtils;

import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.common.BrutException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AaptManager {

    public static File getAppt2() throws BrutException {
        return getAppt(2);
    }

    public static File getAppt1() throws BrutException {
        return getAppt(1);
    }

    private static File getAppt(Integer version) throws BrutException {
        File aaptBinary = null;
        String aaptVersion = getAaptBinaryName(version);
        try {
            if (OSDetection.isArm()) {
                aaptBinary = Jar.getResourceAsFile("/prebuilt/" + aaptVersion + "/armeabi-v7a/" + aaptVersion, AaptManager.class);
            }/* else if (OSDetection.isArm64()) {
                aaptBinary = Jar.getResourceAsFile("/prebuilt/" + aaptVersion + "/arm64-v8a/" + aaptVersion, Class.forName("com.ppma.apktool.MainActivity"));
            }*/ else {
                throw new BrutException("Could not identify platform: " + OSDetection.returnOS());
            }
        } catch (BrutException ex) {
            throw new BrutException(ex);
        }

        if (aaptBinary.setExecutable(true)) {
            return aaptBinary;
        }
        throw new BrutException("Can't set aapt binary as executable");
    }

    public static String getAaptExecutionCommand(String aaptPath, File aapt) throws BrutException {
        if (!aaptPath.isEmpty()) {
            File aaptFile = new File(aaptPath);
            if (aaptFile.canRead() && aaptFile.exists()) {
                aaptFile.setExecutable(true);
                return aaptFile.getPath();
            } else {
                throw new BrutException("binary could not be read: " + aaptFile.getAbsolutePath());
            }
        } else {
            return aapt.getAbsolutePath();
        }
    }

    public static int getAaptVersion(String aaptLocation) throws BrutException {
        return getApptVersion(new File(aaptLocation));
    }

    public static String getAaptBinaryName(Integer version) {
        return "aapt" + (version == 2 ? "2" : "");
    }

    public static int getAppVersionFromString(String version) throws BrutException {
        if (version.startsWith("Android Asset Packaging Tool (aapt) 2:")) {
            return 2;
        } else if (version.startsWith("Android Asset Packaging Tool (aapt) 2.")) {
            return 2; // Prior to Android SDK 26.0.2
        } else if (version.startsWith("Android Asset Packaging Tool, v0.")) {
            return 1;
        }

        throw new BrutException("aapt version could not be identified: " + version);
    }

    public static int getApptVersion(File aapt) throws BrutException {
        if (!aapt.isFile()) {
            throw new BrutException("Could not identify aapt binary as executable.");
        }
        aapt.setExecutable(true);

        List<String> cmd = new ArrayList<>();
        cmd.add(aapt.getAbsolutePath());
        cmd.add("version");

        String version = OS.execAndReturn(cmd.toArray(new String[0]));

        if (version == null) {
            throw new BrutException("Could not execute aapt binary at location: " + aapt.getAbsolutePath());
        }

        return getAppVersionFromString(version);
    }
}
