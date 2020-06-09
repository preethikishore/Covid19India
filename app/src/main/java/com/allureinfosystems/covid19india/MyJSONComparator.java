package com.allureinfosystems.covid19india;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

class MyJSONComparator implements Comparator<JSONObject> {

    @Override
    public int compare(JSONObject o1, JSONObject o2) {
        int v1 = 0;
        try {
            v1 = Integer.parseInt(o1.get("active").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int v3 = 0;
        try {
            v3 = Integer.parseInt(o2.get("active").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Integer.compare(v3, v1);
    }

}