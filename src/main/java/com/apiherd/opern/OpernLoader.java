package com.apiherd.opern;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class OpernLoader {

    private Class<?> callerClass;
    private static OpernLoader loader;

    private OpernLoader(Class<?> css) {
        this.callerClass = css;
    }

    public static OpernLoader getLoader(Class<?> css) {

        if (loader == null)
            loader = new OpernLoader(css);

        return loader;
    }

    public String readFileEnd(String strName) {
        int pos = 0;
        InputStream stream = null;
        byte[] data = new byte[512*1024];
        try {
            if (strName.startsWith("."))
                stream = new FileInputStream(strName);
            else
                stream = callerClass.getClassLoader().getResourceAsStream(strName);

            pos = stream.read(data);
            stream.close();
        } catch (Exception exp) {
        }

        return new String(data, 0, pos, Charset.forName("UTF-8")).trim();
    }
}
