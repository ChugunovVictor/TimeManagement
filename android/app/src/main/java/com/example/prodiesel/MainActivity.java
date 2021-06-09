package com.example.prodiesel;

import android.app.ActivityOptions;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity {

    class ScheduleTask extends TimerTask {
        @Override
        public void run() {
            loadUsers(null);
        }
    };

    static String url_ping = "/api/ping";
    static String url_log_in_out = "/api/logInOut/:id/:password";
    // static String url_server = "http:/192.168.0.103:9999";
    Timer timer = new Timer();

    static boolean DEBUG = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startLockTask();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();
        timer = new Timer();
        timer.schedule(new ScheduleTask(), 0,15000);
    }

    @Override
    protected void onResume(){
        super.onResume();
        timer = new Timer();
        timer.schedule(new ScheduleTask(), 0,15000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timer.cancel();
        timer.purge();
    }

    class User {
        String userId, userName, workingTime;
        Boolean isLogged;

        User(String userId, String userName, Boolean isLogged, String workingTime) {
            this.userId = userId;
            this.userName = userName;
            this.isLogged = isLogged;
            this.workingTime = workingTime;
        }
    }

    public void addUserButton(GridLayout view, User user) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 0f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = 5;
        layoutParams.rightMargin = 5;
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;

        Button button = (Button) getLayoutInflater().inflate(user.isLogged ? R.layout.logout : R.layout.login, null);
        //button.setBackgroundResource(R.drawable.button_custom_green);

        button.setText(user.userName + " " + user.workingTime);
        button.setLayoutParams(layoutParams);

        Context c = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.server_url_file), Context.MODE_PRIVATE);

                String url = sharedPref.getString("url_server", "") + url_log_in_out;
                Intent intent = new Intent(c, PasswordActivity.class);
                intent.putExtra("url", url.replaceAll(":id", user.userId) );
                startActivity(intent);
            }
        });
        view.addView(button);
    }

    public void addLoadUsersButton(GridLayout view) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 0f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        layoutParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        layoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 2);
        layoutParams.leftMargin = 5;
        layoutParams.rightMargin = 5;
        layoutParams.topMargin = 0;
        layoutParams.bottomMargin = 0;

        Button button = (Button) getLayoutInflater().inflate(R.layout.logout, null);
        //button.setBackgroundResource(R.drawable.button_custom_green);

        button.setText("Load Users");
        button.setLayoutParams(layoutParams);

        Context c = this;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUsers(null);
            }
        });
        view.addView(button);
    }

    public void loadUsers(View view) {
        final TextView textView = (TextView) findViewById(R.id.logger);
        final GridLayout userList = (GridLayout) findViewById(R.id.user_list);

        RequestQueue queue = Volley.newRequestQueue(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.server_url_file), Context.MODE_PRIVATE);
        String url = sharedPref.getString("url_server", "") + url_ping;

        Context ctx = this;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (DEBUG) {
                            System.out.println(response);
                            textView.setText(response);
                        }

                        userList.removeAllViews();

                        try {
                            JSONArray arr = new JSONArray(response);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                String userId = obj.getString("userId");
                                String userName = obj.getString("userName");
                                Boolean isLogged = obj.getBoolean("isLogged");
                                String workingTime = obj.getString("workingTime");

                                addUserButton(userList, new User(userId, userName, isLogged, workingTime));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                userList.removeAllViews();
                addLoadUsersButton(userList);
                // textView.setText(error.getLocalizedMessage());
            }
        });

        queue.add(stringRequest);
    }

    public void checkSettings(View view) {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
    }
}