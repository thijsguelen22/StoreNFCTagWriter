package com.survivingwithandroid.nfcwriter;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class changeURL extends AppCompatActivity {

    public ArrayList<School> schools;
    public Spinner mySpinner;
    private String SetURL;
    private ProgressDialog dialog;
    TextView oldURLtxt;
    TextView newURLtxt;
    private String url;
    private String NewURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_url);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        oldURLtxt= (TextView)findViewById(R.id.oldURLtxt);
        newURLtxt= (TextView)findViewById(R.id.newURLtxt);

        String readFeed;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                readFeed= null;
            } else {
                readFeed= extras.getString("JSONValues");
            }
        } else {
            readFeed= (String) savedInstanceState.getSerializable("JSONValues");
        }

        // you can use this array to find the school ID based on name
        schools = new ArrayList<School>();
        // you can use this array to populate your spinner
        ArrayList<String> schoolNames = new ArrayList<String>();

        try {
            JSONObject json = new JSONObject(readFeed);
            JSONArray jsonArray = new JSONArray(json.optString("Urls"));
            Log.i(MainActivity.class.getName(),
                    "Number of entries " + jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                School school = new School();
                school.setUrl(jsonObject.optString("url"));
                school.setRedirect(jsonObject.optString("redirect"));
                schools.add(school);
                schoolNames.add(jsonObject.optString("url"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mySpinner = (Spinner)findViewById(R.id.my_spinner2);
        mySpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, schoolNames));
        SetURL = mySpinner.getSelectedItem().toString();

        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.fab2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText et = (EditText) findViewById(R.id.updatedURL);
                NewURL = et.getText().toString();
                newURLtxt.setText(NewURL);
                oldURLtxt.setText(SetURL);
                try {
                    run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void run() throws IOException {
        String u = "thijs-jan.aoweb.nl/changeURL.php?old=";
        String url = u  + SetURL + "&new=" + NewURL;

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

                changeURL.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (myResponse != null) {
                            dialog.hide();
                            dialog = new ProgressDialog(changeURL.this);
                            dialog.setMessage(myResponse);
                            dialog.show();
                            ;
                        }
                    }

                });
            }
        });
    }

    public void changeURLs(View view) {
        final EditText et = (EditText) findViewById(R.id.updatedURL);
        NewURL = et.getText().toString();
        newURLtxt.setText(NewURL);
        oldURLtxt.setText(SetURL);
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
