package com.browserlimit.app.engine;

import rikka.shizuku.Shizuku;

public class ShizukuHelper {
    public static boolean uninstall(String packageName) {
        try {
            String[] cmd = new String[]{"pm", "uninstall", "--user", "0", packageName};
            Process process = Shizuku.newProcess(cmd, null, null);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
