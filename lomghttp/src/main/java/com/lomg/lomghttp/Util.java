package com.lomg.lomghttp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by lomg on 2016/8/21.
 */
public class Util {
    public static String readFile(File fileName) {
        String result = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);

            int length = fileInputStream.available();

            byte[] buffer = new byte[length];
            fileInputStream.read(buffer);

            result = new String(buffer, "UTF-8");

            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
