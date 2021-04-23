package com.example.shop.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LongDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shop.HttpHandler;
import com.example.shop.R;
import com.example.shop.adapter.SeriesAdapter;
import com.example.shop.model.STovar;
import com.example.shop.model.SeriesModel;
import com.example.shop.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

public class IncomingWork extends AppCompatActivity {

    private Intent intent;
    private ImageView barcodescan;
    private ImageView add;
    private ImageView next;
    private ImageView item_deleteW;

    private ListView listView;
    private TextView searchView;
    private ArrayList<SeriesModel> list;
    private SeriesAdapter adapter;
    private ProgressDialog progressDialog;
    private String ip;
    private User thisuUser;
    private STovar sTovar;
    private String barcode;
    private SeriesModel seriesModel;
    private Integer slaveId;
    private String name;
    private TextView soni;
    private MutableLiveData<ArrayList<SeriesModel>> liveData;
    private Integer count = 0;
    private Integer mainSlaveId = 55;
    private Integer counts = 0;
    private String allNumber = "";
    private Integer idForGetList;
    List<Integer> mainId;
    List<String> serial;
    private Integer selectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_work);


        item_deleteW = findViewById(R.id.item_deleteW);
        add = findViewById(R.id.products_list_input_add);
        barcodescan = findViewById(R.id.products_list_barcodescan);
        listView = findViewById(R.id.products_list_list_view);
        searchView = findViewById(R.id.searchView);
        soni = findViewById(R.id.soni);

        intent = getIntent();
        ip = intent.getStringExtra("ip");
        thisuUser = (User) intent.getSerializableExtra("user");
        sTovar = (STovar) intent.getSerializableExtra("stovar");
        slaveId = intent.getIntExtra("slave_id", 0);
        name = intent.getStringExtra("name");
        allNumber = intent.getStringExtra("soni");
        idForGetList = intent.getIntExtra("id", 0);
        Log.d("getid", idForGetList.toString());
        next = findViewById(R.id.next);
        liveData = new MutableLiveData<>();
        if (idForGetList > 0) {
            slaveId = idForGetList;
            new GetSeries().execute();
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
        Log.d("names", name);
        list = new ArrayList<>();
        mainId = new ArrayList<>();
        serial = new ArrayList<>();
        seriesModel = new SeriesModel();

        next.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(IncomingWork.this, ProductsList.class);
                        setDownIntent(intent);
                        startActivity(intent);
                        finish();
                    }
                });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Seriya", list.toString());

                Integer seriyaid = list.get(position).getId();
                Log.d("Seriya", String.valueOf(seriyaid));
                selectedId = seriyaid;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                // xa
                                new DelSeries().execute();
                                new GetSeries().execute();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //yo'q'
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(IncomingWork.this);
                builder.setMessage("O'chirishni xohlaysizmi?").setPositiveButton("Xa", dialogClickListener)
                        .setNegativeButton("Yo'q", dialogClickListener).show();


                return false;
            }
        });

        barcodescan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ZxingOrient(IncomingWork.this).setIcon(R.mipmap.ic_launcher).initiateScan();

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        ZxingOrientResult scanResult =
                ZxingOrient.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null) {
            // handle the result
            CharSequence c = scanResult.getContents();
            if (c.length() >= 12) {
                setText(c);
            } else {
                Toast.makeText(IncomingWork.this, "Yaroqsiz Seriya raqam!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void setText(CharSequence sequence) {

        if (sequence.length() >= 12) {
            count = 1;
            searchView.setText(sequence, TextView.BufferType.EDITABLE); // list add
            seriesModel.setSerial(String.valueOf(sequence));
            seriesModel.setMain_id(mainSlaveId);
            list.add(seriesModel);
            new AddSeries().execute();
            new GetSeries().execute();
            Log.d("anotherone", "no");
            if ((int) mainSlaveId < 0) {
                Log.d("mainslaveid1", mainSlaveId.toString());
                //    Toast.makeText(this, "Bu malumtlar bazasida bor", Toast.LENGTH_LONG).show();
            } else {
                Log.d("mainslaveid2", mainSlaveId.toString());
                counts++;
                liveData.observe(this, new Observer<ArrayList<SeriesModel>>() {
                    @Override
                    public void onChanged(@Nullable ArrayList<SeriesModel> seriesModels) {
                        adapter = new SeriesAdapter(IncomingWork.this, R.layout.activity_incoming__item, seriesModels);
                        listView.setAdapter(adapter);
                    }
                });
            }


            soni.setText(allNumber + " dan " + counts);
        } else {
            Toast.makeText(IncomingWork.this, "Yaroqsiz Seriya raqam!", Toast.LENGTH_SHORT).show();
        }
    }

    public void setDownIntent(Intent nextIntent) {
        nextIntent.putExtra("user", thisuUser);
        nextIntent.putExtra("ip", ip);
      /*  nextIntent.putExtra("asosId",intent.getIntExtra("asosId",0));
        nextIntent.putExtra("type",intent.getIntExtra("type",0));
        nextIntent.putExtra("sumprice",intent.getStringExtra("sumprice"));*/
        nextIntent.putExtra("stovar", sTovar);
    }



    private class GetSeries extends AsyncTask<Void, Void, Void> {

        private String urlProducts = "http://" + ip + ":8080/application/json/getMainSlave/" + slaveId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(IncomingWork.this);
            progressDialog.setMessage("Малумот юкланяпти");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.makeServiceCall(urlProducts);

            if (jsonStr != null) {

                try {
                    list.clear();
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        SeriesModel tovar = new SeriesModel();
                        JSONObject object = jsonArray.getJSONObject(i);
                        Log.v("MyLog1", object.toString());
                        Log.d("idddd", String.valueOf(object.getInt("id")));
                        mainId.add(object.getInt("id"));
                        serial.add(object.getString("serial"));
                        tovar.setId(object.getInt("id"));
                        tovar.setSerial(object.getString("serial"));

                        list.add(tovar);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                liveData.postValue(list);
                            }
                        });
                    }
                } catch (final JSONException e) {
                    Log.v("MyTag2", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(IncomingWork.this, "Хатолик юз берди", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.v("MyTag2", "serverdan galmadi");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(IncomingWork.this, "Сервер билан муамо бор", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }



    private class AddSeries extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(IncomingWork.this);
            progressDialog.setMessage("Сақланмоқда !!!");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String reqUrl = "http://" + ip + ":8080/application/json/addSerial/" + seriesModel.getSerial();
            String reqUrl2 = "http://" + ip + ":8080/application/json/addMainSlave";
            Integer x = httpHandler.makeServicePostSeries(reqUrl, seriesModel, slaveId);
            mainSlaveId = httpHandler.makeServicePostSeriesWithSlave(reqUrl2, seriesModel, slaveId);
            Log.v("MyTag2:", x + " sTovar:" + seriesModel.getSerial());
            Log.v("Myssla:", String.valueOf(mainSlaveId));
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            //  new GetSeries().execute();
        }
    }

    private class DelSeries extends AsyncTask<Void, Void, Void> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(IncomingWork.this);
            progressDialog.setMessage("O'chirilmoqda!");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String reqUrldel = "http://" + ip + ":8080/application/json/delMainSlave/" + selectedId;
            Integer x = httpHandler.makeServiceDelete(reqUrldel);
            Log.d("DELETE IW", x.toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }
}