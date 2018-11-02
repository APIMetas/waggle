package com.apiherd.opern;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Tone {
    private String name;
    private LinkedHashMap<String,String> parameters;

    public Tone() {
        this.parameters = new LinkedHashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public static Tone parseTone(LinkedHashMap<String, Object> objs) {
        Tone tn = new Tone();
        tn.setName(objs.get("Name").toString());
        LinkedHashMap<String, String> map = (LinkedHashMap<String, String>)objs.get("Parameters");
        if (null != map)
            tn.setParameters(map);
        return tn;
    }
}
