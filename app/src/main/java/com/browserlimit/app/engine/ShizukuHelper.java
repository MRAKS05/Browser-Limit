package com.example.engine;

import rikka.shizuku.Shizuku;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class ShizukuHelper {
    public static boolean uninstall(String packageName) {
        try {
            String[] cmd = new String[]{"pm", "uninstall", "--user", "0", packageName};
            Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
            m.setAccessible(true);
            Process process = (Process) m.invoke(null, cmd, null, null);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            return exitCode == 0 || output.toString().contains("Success");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
