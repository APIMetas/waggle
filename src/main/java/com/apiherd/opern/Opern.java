package com.apiherd.opern;

import java.util.ArrayList;

public class Opern {
    private String name;
    private Tone meta;
    private Tone request;
    private ArrayList<Action> actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Tone getMeta() {
        return meta;
    }

    public void setMeta(Tone meta) {
        this.meta = meta;
    }

    public Tone getRequest() {
        return request;
    }

    public void setRequest(Tone request) {
        this.request = request;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void setActions(ArrayList<Action> actions) {
        this.actions = actions;
    }
}
