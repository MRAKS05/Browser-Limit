package com.browserlimit.app.engine;

import java.lang.reflect.Method;

public class ShizukuHelper {
    public static boolean uninstall(String packageName) {
        try {
            String[] cmd = new String[]{"pm", "uninstall", "--user", "0", packageName};
            Class<?> shizukuClass = Class.forName("rikka.shizuku.Shizuku");
            Method newProcess = shizukuClass.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            newProcess.setAccessible(true);
            Process process = (Process) newProcess.invoke(null, cmd, null, null);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
