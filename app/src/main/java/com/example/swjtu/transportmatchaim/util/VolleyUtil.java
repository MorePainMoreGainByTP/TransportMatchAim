package com.example.swjtu.transportmatchaim.util;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by tangpeng on 2017/3/3.
 */

public class VolleyUtil {

    public static RequestQueue requestQueue;

    public static String stringResult;
    public static JSONObject jsonObjectResult;

    public static void stringRequest(Context context, String url, Response.ErrorListener errorListener, final Map<String, String> params) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                stringResult = s;
            }
        }, errorListener) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(stringRequest);
    }

    public static void jsonObjectRequest(Context context, String url, JSONObject objectParam, Response.ErrorListener errorListener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, objectParam, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject object) {
                jsonObjectResult = object;
            }
        }, errorListener);
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        requestQueue.add(jsonObjectRequest);
    }
}
