package com.survivingwithandroid.nfcwriter;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class addURL extends AppCompatActivity {

    private View v;
    private String userID = "1"; //only for debug!!
    private String givenURL;
    private ProgressDialog dialog;
    private AlertDialog dialog2;
    EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = (EditText) findViewById(R.id.newURL);
                String givenURL = et.getText().toString();
                String url = "http://thijs-jan.aoweb.nl/addURL.php?uid=";
                String defUrl = url + userID + "&url=" + givenURL;
                try {
                    run(defUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void run(String url) throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                addURL.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //LoadedXML = myResponse;
                        dialog = new ProgressDialog(addURL.this);
                        dialog.setMessage("Link toevoegen...");
                        dialog.show();;
                        if(myResponse != null) {
                            dialog.hide();
                            dialog = new ProgressDialog(addURL.this);
                            dialog.setMessage(myResponse);
                            dialog.show();;
                        }
                    }
                });
            }
        });
    }
}
