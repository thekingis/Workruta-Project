package com.workruta.android.Interface;

import org.json.JSONException;
import org.json.JSONObject;

public interface InfoChangeListener {
    void infoChanged(String key, JSONObject object) throws Exception;
}
