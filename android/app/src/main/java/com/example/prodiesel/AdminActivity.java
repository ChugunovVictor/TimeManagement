package com.example.prodiesel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class AdminActivity extends BaseActivity {

    String secretPassword = "123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
    }

    public void closeAdmin(View view) {
        finish();
    }

    public void enterPassword(View view) {
        if( ((EditText)findViewById(R.id.passwordField)).getText().toString().equals(secretPassword) ){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }
}