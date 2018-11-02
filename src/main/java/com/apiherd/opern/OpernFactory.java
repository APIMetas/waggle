package com.apiherd.opern;

import com.apiherd.waggle.SocketClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class OpernFactory {
    private static OpernLoader loader;
    private static ArrayList<Opern> opers = new ArrayList<>();

    public static ArrayList<Opern> parseOperns(OpernLoader lder, String opnName) {
        opers.clear();
        loader = lder;
        return parseOperns(opers, lder, opnName);
    }

    public static ArrayList<Opern> parseOperns(ArrayList<Opern> operns, OpernLoader lder, String opnName) {
        Yaml yaml = new Yaml();
        String strFile = lder.readFileEnd(opnName);
        LinkedHashMap<String, Object> objss =
                (LinkedHashMap<String, Object>)yaml.load(strFile);
        for(String name:objss.keySet()) {
            LinkedHashMap<String, Object> objs =
                    (LinkedHashMap<String, Object>)objss.get(name);
            Opern opr = new Opern();
            opr.setName(name);
            opr.setMeta(Tone.parseTone((LinkedHashMap<String, Object>)objs.get("Meta")));
            opr.setRequest(Tone.parseTone((LinkedHashMap<String, Object>)objs.get("Request")));
            opr.setActions(Action.parseActions((ArrayList<Object>) objs.get("Actions")));
            operns.add(opr);
        }
        return operns;
    }

    public static ArrayList<Opern> parseOperns(OpernLoader lder) {
        return parseOperns(lder, "Opern.yaml");
    }

    protected static ArrayList<APILog> execRequest(
            Opern opn, SocketClient client, JSONObject jsnRequest, JSONObject jsnMeta) {
        boolean status = true;
        String response = null;
        ArrayList<APILog> logs = new ArrayList<>();
        for (String key:opn.getMeta().getParameters().keySet()) {
            jsnMeta.put(key, opn.getMeta().getParameters().get(key));
        }
        for (String key:opn.getRequest().getParameters().keySet()) {
            jsnRequest.put(key, opn.getRequest().getParameters().get(key));
        }

        for (Action act:opn.getActions()) {
            String needResponse = jsnMeta.optString("NeedResponse");
            if (null == needResponse || "".equals(needResponse))
                needResponse = "true";
            jsnMeta.put("ActionName", act.getName());
            jsnRequest.put("APIMetas", jsnMeta);
            APILog log = new APILog(jsnRequest.toString());
            client.writeString(jsnRequest.toString());
            if (needResponse.startsWith("t")) {
                response = client.readString();
                status = act.getAssertion().makeAssert(response);
            } else {
                response = "{\"NeedResponse\":\"false\"}";
                log.setLateAssert(act.getAssertion());
            }
            log.setResponse(response);
            log.setStatusCode(String.valueOf(status));
            logs.add(log);
        }
        return logs;
    }

    public static ArrayList<APILog> runOperns(SocketClient client) {
        return runOperns(client, new HashMap<>(), new HashMap<>());
    }

    public static ArrayList<APILog> runOperns(SocketClient client, HashMap<String, Object> dynamics) {
        return runOperns(client, dynamics, new HashMap<>());
    }

    public static ArrayList<APILog> runOperns(OpernLoader lder, SocketClient client,
                                            ArrayList<Opern> operons, HashMap<String, Object> dynamics,
                                            HashMap<String, Integer> sleeps) {
        ArrayList<APILog> logs = new ArrayList<>();
        for(Opern opn:operons) {
            String meta = lder.readFileEnd(opn.getMeta().getName());
            String request = lder.readFileEnd(opn.getRequest().getName());
            JSONObject jsnMeta = new JSONObject(meta);
            if(request.startsWith("[")) {
                JSONArray jsnRequests = new JSONArray(request);
                for(int i = 0; i < jsnRequests.length(); i++)
                    logs.addAll(execRequest(opn, client, jsnRequests.getJSONObject(i), jsnMeta));
            } else {
                JSONObject jsnRequest = new JSONObject(request);
                Object obj = dynamics.get(opn.getName());
                if (null != obj) {
                    HashMap<String, Object> maps = (HashMap<String, Object>)obj;
                    for (String key : maps.keySet())
                        jsnRequest.put(key, maps.get(key));
                }
                Integer sleep = sleeps.get(opn.getName());
                if (null != sleep) {
                    try {
                        Thread.sleep((int) sleep);
                    } catch (Exception exp) {
                        ;
                    }
                }
                logs.addAll(execRequest(opn, client, jsnRequest, jsnMeta));
            }
        }
        return logs;
    }

    public static ArrayList<APILog> runOperns(SocketClient client,
                                              HashMap<String, Object> dynamics,
                                              HashMap<String, Integer> sleeps) {
        return runOperns(loader, client, opers, dynamics, sleeps);
    }

}
