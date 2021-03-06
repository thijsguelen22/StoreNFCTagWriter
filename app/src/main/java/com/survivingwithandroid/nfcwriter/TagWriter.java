package com.survivingwithandroid.nfcwriter;

/*
 * Copyright (C) 2016, francesco Azzola
 *
 *(http://www.survivingwithandroid.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 03/01/16
 */


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class TagWriter extends AppCompatActivity {

    private NFCManager nfcMger;

    private View v;
    private NdefMessage message = null;
    private ProgressDialog dialog;
    Tag currentTag;

    // API urls

    public List SitesList;
    public String SetURL;
    public String url = "http://thijs-jan.aoweb.nl/getUrls.php";
    public String LoadedXML;
    TextView ChosenURL;
    private EditText content;
    private String XMLString;
    public ArrayList<School> schools;

    private String JSONString;
    public Spinner mySpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_writer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nfcMger = new NFCManager(this);

        v = findViewById(R.id.mainLyt);

        //code voor de URLSpinner om de JSON met verschillende sites op te halen

        //debug voor het lezen van JSON
        ChosenURL= (TextView)findViewById(R.id.ChosenURL);
        content= (EditText)findViewById(R.id.content);
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String newString;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
            } else {
                newString= extras.getString("JSONValues");
            }
        } else {
            newString= (String) savedInstanceState.getSerializable("JSONValues");
        }


        String readFeed = newString;

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

        FloatingActionButton btn = (FloatingActionButton) findViewById(R.id.fab);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mySpinner.getSelectedItemPosition();
                School sch = schools.get(pos);
                String s = sch.getRedirect();
                message =  nfcMger.createUriMessage(s, "http://");
                ChosenURL.setText(s);


                if (message != null) {

                    dialog = new ProgressDialog(TagWriter.this);
                    dialog.setMessage("Houd de chip tegen uw toestel");
                    dialog.show();;
                }
            }
        });

    }

    void run() throws IOException {

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

                TagWriter.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //LoadedXML = myResponse;
                        JSONString = myResponse;
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    protected void onResume() {
        super.onResume();

        try {
            nfcMger.verifyNFC();
            //nfcMger.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NFCNotSupported nfcnsup) {
            Snackbar.make(v, "NFC not supported", Snackbar.LENGTH_LONG).show();
        }
        catch(NFCManager.NFCNotEnabled nfcnEn) {
            Snackbar.make(v, "NFC Not enabled", Snackbar.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            nfcMger.writeTag(currentTag, message);
            dialog.dismiss();
            Snackbar.make(v, "Tag written", Snackbar.LENGTH_LONG).show();

        }
        else {
            // Handle intent

        }
    }




}