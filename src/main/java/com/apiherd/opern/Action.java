package com.apiherd.opern;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Action {
    private String name;
    private Assertion assertion;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Assertion getAssertion() {
        return assertion;
    }

    public void setAssertion(Assertion assertion) {
        this.assertion = assertion;
    }

    public static Action parseAction(LinkedHashMap<String, Object> objs) {
        Action act = new Action();
        act.setName(objs.get("Name").toString());
        act.setAssertion(Assertion.parseAssertion(
                (LinkedHashMap<String, Object>)objs.get("Assertion")));
        return act;
    }

    public static ArrayList<Action> parseActions(ArrayList<Object> objs) {
        ArrayList<Action> actions = new ArrayList<>();
        for(Object obj:objs){
            Action act = parseAction((LinkedHashMap<String, Object>)obj);
            actions.add(act);
        }
        return actions;
    }
}
