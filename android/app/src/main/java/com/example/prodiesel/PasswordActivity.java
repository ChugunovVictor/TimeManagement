package com.example.prodiesel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class PasswordActivity extends BaseActivity {

    String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password);
    }

    @Override
    protected void onStart(){
        super.onStart();
        password = "";
    }

    @Override
    protected void onResume(){
        super.onResume();
        password = "";
    }

    public void updateUI(){
        ((TextView)findViewById(R.id.number_0)).setText( password.length() > 0 ? "*" /*Character.toString(password.charAt(0))*/ : "");
        ((TextView)findViewById(R.id.number_1)).setText( password.length() > 1 ? "*" /*Character.toString(password.charAt(1))*/ : "");
        ((TextView)findViewById(R.id.number_2)).setText( password.length() > 2 ? "*" /*Character.toString(password.charAt(2))*/ : "");
        ((TextView)findViewById(R.id.number_3)).setText( password.length() > 3 ? "*" /*Character.toString(password.charAt(3))*/ : "");
    }

    public void enterSymbol(View view) {
        if(password.length() < 4) {
            password += ((TextView) view).getText();
            updateUI();
        }
    }

    public void back(View view) {
        finish();
    }

    public void deleteSymbol(View view) {
        if(password.length() > 0){
            password = password.substring(0, password.length() - 1);
            updateUI();
        }
    }

    public void showToast(String toastText){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(toastText);
/*
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
*/
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setView(layout);
        alert.show();
    }

    public void enter(View view) {
        Intent intent = getIntent();
        String url = intent.getExtras().getString("url");
        if(password.length() > 0)
            sendRequest(view, url.replaceAll(":password", password));
        else{
            showToast("Empty password");
        }
    //    finish();
    }

    public void sendRequest(View view, String url) {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String resp = response.replaceAll("\"", "");
                        if(resp.equalsIgnoreCase("success")){
                            finish();
                        } else showToast(resp);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.err.println(error.getLocalizedMessage());
            }
        });
        queue.add(stringRequest);
    }
}