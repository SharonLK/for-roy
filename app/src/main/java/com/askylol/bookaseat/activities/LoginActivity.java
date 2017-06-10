package com.askylol.bookaseat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.utils.Data;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Data.INSTANCE.username != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void loginClick(View view) {
        EditText username = (EditText) findViewById(R.id.emailEditText);

        Data.INSTANCE.username = username.getText().toString();
        startActivity(new Intent(this, MainActivity.class));
        finish();
//        new AsyncTask<String, Void, JSONObject>() {
//            @Override
//            protected JSONObject doInBackground(String... params) {
//                try {
//                    return LocationService.track(LoginActivity.this, Data.INSTANCE.username);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute(Data.INSTANCE.username);
    }
}
