package com.example.shop.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shop.HttpHandler;
import com.example.shop.R;
import com.example.shop.SMSsender;
import com.example.shop.adapter.IncomingAddListener;
import com.example.shop.adapter.ItemAdapter;
import com.example.shop.adapter.ItemSlaveAdapter;
import com.example.shop.adapter.ProductAdapter;
import com.example.shop.model.Product;
import com.example.shop.model.Slave;
import com.example.shop.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.sudar.zxingorient.ZxingOrient;
import me.sudar.zxingorient.ZxingOrientResult;

public class MainActivity extends AppCompatActivity {

    private LinearLayout main_changed1;
    private LinearLayout main_changed2;
    private static final String TAG = "MyLog";
    private SearchView searchView;
    private ListView listView;
    private ListView listView2;
    private ArrayList<Slave> list;
    private ArrayList<Slave> list2;
    private ItemSlaveAdapter adapter;
    private ItemAdapter adapter2;
    private ImageView save;
    private ImageView calsel;
    private EditText price_product_count;
    private EditText price_inproduct_count;
    private Slave selectProduct;
    private Slave selectProductOnList;
    private static Double selectProductSum = 0.0;
    private static Integer selectedProduct = 0;
    private static Double sum = 0.0;
    private static Integer asosId;
    private Integer type;
    private Integer haridorId;
    private TextView sumPrice;
    private TextView selectProductView;

    private static User thisuUser;

    private ProgressDialog progressDialog;
    private static String ip = "192.168.43.57";
    private static String urlAsos = "http://192.168.43.52:8080/application/json/asos";

    private Intent intent;
    private Intent typeIntent;
    private ImageView barcodeImageView;
    private SMSsender smSsender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Сотув");
        setSupportActionBar(toolbar);
        barcodeImageView = findViewById(R.id.action_image_barcode);
        sumPrice = (TextView) findViewById(R.id.sum_price);
        selectProductView = (TextView) findViewById(R.id.select_product);
        barcodeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent=new Intent("com.google.zxing.client.android.SCAN");
                intent.setPackage("com.google.zxing.client.android");
                intent.putExtra("SCAN_FORMATS", "CODE_39,CODE_93,CODE_128,DATA_MATRIX,ITF,CODABAR,EAN_13,EAN_8,UPC_A,QR_CODE");
                startActivityForResult(intent, 0);*/
                new ZxingOrient(MainActivity.this).setIcon(R.mipmap.ic_launcher).initiateScan();
            }
        });
        main_changed1 = (LinearLayout) findViewById(R.id.linerLayout1);
        main_changed2 = (LinearLayout) findViewById(R.id.linerLayout2);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (list2.size() > 0) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    new Finish().execute();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setMessage("Ҳаридни якунлашни истайсизми?").setPositiveButton("Ха", dialogClickListener)
                            .setNegativeButton("Йўқ", dialogClickListener).show();
                } else {
                    Toast.makeText(MainActivity.this, "Хеч қандай махсулот йўқ !!!", Toast.LENGTH_LONG).show();
                }
            }
        });



        save = (ImageView) findViewById(R.id.save);
        calsel = (ImageView) findViewById(R.id.calsel);

        price_product_count = (EditText) findViewById(R.id.price_product_count);
        price_inproduct_count = (EditText) findViewById(R.id.price_inproduct_count);


        searchView = (SearchView) findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.listView);
        listView2 = (ListView) findViewById(R.id.listView2);

        smSsender = new SMSsender();
        list = new ArrayList<>();
        list2 = new ArrayList<>();
        Log.v(TAG, "Ha ina hohiray");
        intent = getIntent();
        thisuUser = (User) intent.getSerializableExtra("user");
        ip = intent.getStringExtra("ip");
        type = intent.getIntExtra("type", 1);
        sumPrice.setText(intent.getStringExtra("sumprice"));
        Log.v(TAG, ip + "");
        asosId = (Integer) intent.getIntExtra("asosId", -1);
        Log.v(TAG, thisuUser.getId().toString());
        Log.v(TAG, intent.getStringExtra("ip"));
        new GetProducts().execute();
        Log.v("MainAsosId", asosId + "");


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
        });

        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                      price_product_count.requestFocus();
                        showSoftKeyboard(price_product_count);
                        Slave slave = (Slave) view.getTag();
                        adapter.setPosition(i);
                        adapter2.setPosition(-1);
                        adapter.notifyDataSetChanged();
                        adapter2.notifyDataSetChanged();

                        selectedProduct = 1;
                        setProduct(slave);
                    }
                });

        listView2.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                      price_product_count.requestFocus();
                        showSoftKeyboard(price_product_count);
                        Slave item = (Slave) view.getTag();
                        if (item == null) {
                            Log.v(TAG, "Ah sani");
                        }
                        adapter.setPosition(-1);
                        adapter2.setPosition(i);
                        adapter.notifyDataSetChanged();
                        adapter2.notifyDataSetChanged();

                        selectedProduct = 2;
                        Log.v(TAG, item.toString());

                        setProduct(item);
                    }
                });

        save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedProduct != 0) {
                            Integer price_product_count_int = tryParse(price_product_count.getText().toString());
                            Integer price_inproduct_count_int = tryParse(price_inproduct_count.getText().toString());

                            Log.v(TAG, price_inproduct_count_int + " " + price_product_count_int);

                            if (price_product_count_int > 0 || price_inproduct_count_int > 0) {
                                Log.v(TAG + "in 1:", selectProduct.getTovar_nom() + price_product_count_int + " " + price_inproduct_count_int);
                                selectProduct.setKol(price_product_count_int);
                                selectProduct.setKol_in(price_inproduct_count_int);
                                if (selectedProduct == 1) {
                                    Log.v(TAG, "begin AddProduct().execute()");
                                    new AddProduct().execute();
                                    Log.v(TAG, "end AddProduct().execute()" + selectProduct.toString());
                                } else if (selectedProduct.equals(2)) {
                                    new PutProduct().execute();
                                }
//                               searchView.requestFocus();
                                showSoftKeyboard(searchView);
                            } else {
                                Toast.makeText(MainActivity.this, "Сонини киритинг !!!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Махсулотни танланг", Toast.LENGTH_LONG).show();
                        }

                    }
                }
        );
        calsel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                selectedProduct = 0;
                                setProduct(selectProduct);
                                searchView.requestFocus();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Сиз ростан хам бекор қилишни истайсизми?").setPositiveButton("Ха", dialogClickListener)
                        .setNegativeButton("Йўқ", dialogClickListener).show();
            }
        });
    }


    public void onOneChangedList() {
        Log.v(TAG, "onOneChangedList: " + adapter.getCount());
        if (adapter.getCount() == 1) {
            adapter.setPosition(0);
            adapter2.setPosition(-1);
            adapter.notifyDataSetChanged();
            adapter2.notifyDataSetChanged();

            selectedProduct = 1;
            setProduct(adapter.getItem(0));
            price_product_count.requestFocus();
        } else {
            selectedProduct = 0;
            setProduct(selectProduct);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.item1) {
//           new Finish().execute();
            Intent intent = new Intent(MainActivity.this, TypeChangeActivity.class);
            setDownIntent(intent);
            startActivity(intent);
            finish();
        }
        if (id == R.id.item3) {
            Intent intent = new Intent(MainActivity.this, ProductsList.class);
            setDownIntent(intent);
            startActivity(intent);
            finish();
        }
        if (id == R.id.item4) {
            Intent intent = new Intent(MainActivity.this, IncomingProducts.class);
            setDownIntent(intent);
            startActivity(intent);
            finish();
        }
        if (id == R.id.item5) {
            SharedPreferences preferences = getSharedPreferences("LoginPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Дастурдан чикишни истайсизми?").setPositiveButton("Ха", dialogClickListener)
                .setNegativeButton("Йўқ", dialogClickListener).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        ZxingOrientResult scanResult =
                ZxingOrient.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null && scanResult.toString().length() == 12 ) {
            // handle the result
            searchView.setQuery(scanResult.getContents(), false);
        }
        else if (scanResult.toString().length() > 12){
            Log.d("Shtrix" , "12 dan katta");
        }
    }

    private void setProduct(Slave slave) {
        if (selectedProduct != 0) {
            selectProductOnList = slave;
            selectProductView.setText(slave.getTovar_nom());
            if (selectedProduct == 2) {
                main_changed1.setBackgroundResource(R.drawable.backgroun3ch);
                main_changed2.setBackgroundResource(R.drawable.backgroun3ch);
                if (slave.getKol() > 0) {
                    CharSequence c = "" + slave.getKol();
                    price_product_count.setText(c, EditText.BufferType.EDITABLE);
                }
                if (slave.getKol_in() > 0) {
                    CharSequence c = "" + slave.getKol_in();
                    price_inproduct_count.setText(c, EditText.BufferType.EDITABLE);
                }
            } else {
                main_changed1.setBackgroundResource(R.drawable.backgroun4ch);
                main_changed2.setBackgroundResource(R.drawable.backgroun4ch);
            }
            selectProduct = slave;
        } else {
            selectProductView.setText(R.string.product);
            main_changed1.setBackgroundResource(R.drawable.backgroun4);
            main_changed2.setBackgroundResource(R.drawable.backgroun4);
            adapter.setPosition(-1);
            adapter2.setPosition(-1);
            adapter.notifyDataSetChanged();
            adapter2.notifyDataSetChanged();
            selectProduct = slave;
        }
        price_product_count.getText().clear();
        price_inproduct_count.getText().clear();
    }

    public Integer tryParse(Object obj) {
        Integer retVal;
        try {
            retVal = Integer.parseInt((String) obj);
        } catch (NumberFormatException nfe) {
            retVal = 0; // or null if that is your preference
        }
        return retVal;
    }

    public void setDownIntent(Intent nextIntent) {
        nextIntent.putExtra("user", intent.getSerializableExtra("user"));
        nextIntent.putExtra("ip", intent.getStringExtra("ip"));
        nextIntent.putExtra("asosId", intent.getIntExtra("asosId", 0));
        nextIntent.putExtra("type", intent.getIntExtra("type", 0));
        nextIntent.putExtra("sumprice", intent.getStringExtra("sumprice"));
        nextIntent.putExtra("stovar", intent.getSerializableExtra("stovar"));
    }


    private void addList(Slave slave) {
        list2.add(slave);
    }

     public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    private class AddProduct extends AsyncTask<Void, Void, Void> {
        String urlRequest = "http://" + ip + ":8080/application/json/asosslave/asosid=" + asosId + "/userid=" + thisuUser.getId();
        Integer i = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Малумот сақланяпти");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            i = httpHandler.makeServiceAddProduct(urlRequest, selectProduct);
            Log.v(TAG, "makeServiceAddProduct: " + i);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
//            selectProductSum = (selectProduct.getPrice() * selectProduct.getCount() + selectProduct.getInprice() * selectProduct.getIncount());
//            sum += selectProductSum;
//            selectProductOnList.setCount(selectProductOnList.getCount() - selectProduct.getCount());
//            selectProductOnList.setIncount(selectProductOnList.getIncount() - selectProduct.getIncount());

            addList(selectProduct);
            adapter2.notifyDataSetChanged();

            selectedProduct = 0;
            sumPrice.setText("Умуммий сумма: " + sum + " Сўм");
            intent.putExtra("sumprice", sumPrice.getText().toString());
            setProduct(selectProduct);
        }

    }


    private class GetProducts extends AsyncTask<Void, Void, Void> {
        //        http://localhost:8080/application/json/clientid=4/4/products
        private String urlProducts = "http://" + ip + ":8080/application/json/" + thisuUser.getClient_id() + "/" + type + "/products";
        private String urlAddProducts = "http://" + ip + ":8080/application/json/products/" + asosId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Малумот юкланяпти");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.makeServiceCall(urlProducts);
            Log.d("jsonStr", jsonStr);

            String jsonStr2 = httpHandler.makeServiceCall(urlAddProducts);

            Log.v(TAG, "URL:" + urlProducts);
            Log.v(TAG, "URL:" + urlAddProducts);
            if (jsonStr2 != null) {
                Log.d("jsonStr2", jsonStr2);
                try {
                    JSONArray jsonArray2 = new JSONArray(jsonStr2);
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        Slave item = new Slave();
                        JSONObject object2 = jsonArray2.getJSONObject(i);
                        item.setPutId(object2.getInt("id"));
                        item.setId(object2.getInt("productId"));
                        item.setTovar_nom(object2.getString("tovar_nom"));
                        item.setKol(object2.getInt("kol"));
                        item.setKol_in(object2.getInt("kol_in"));
                        item.setSena(object2.getDouble("sena"));
                        item.setSena_in(object2.getDouble("sena_in"));
                        item.setSotish(object2.getDouble("sotish"));
                        item.setSotish_in(object2.getDouble("sotish_in"));
                        list2.add(item);
                    }
                } catch (JSONException e) {
                    Log.v(TAG, e.getMessage());
                }

            }
            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Slave product = new Slave();
                        JSONObject object = jsonArray.getJSONObject(i);
                        product.setPutId(object.getInt("0"));
                        product.setId(object.getInt("id"));
                        product.setTovar_nom(object.getString("tovar_nom"));
                        product.setKol(object.getInt("kol"));
                        product.setKol_in(object.getInt("kol_in"));
                        product.setKol_ost(object.getInt("kol_ost"));
                        product.setKol_in_ost(object.getInt("kol_in_ost"));
                        product.setSena(object.getDouble("sena"));
                        product.setSena_in(object.getDouble("sena_in"));
                        product.setSotish(object.getDouble("sotish_in"));
                        list.add(product);

                    }
                } catch (final JSONException e) {
                    Log.v("MyTag2", e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Хатолик юз берди", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } else {
                Log.v("MyTag2", "serverdan galmadi");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Сервер билан муамо бор", Toast.LENGTH_LONG).show();
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
            adapter = new ItemSlaveAdapter(MainActivity.this, R.layout.products_item, list,ip, asosId,  new IncomingAddListener() {
                @Override
                public void itemSeriesClick(ArrayList<Slave> products, Integer position) {

                }

                @Override
                public void itemSlaveClick(ArrayList<Slave> items, int position) {

                }
            }
            );
            listView.setAdapter(adapter);
            adapter2 = new ItemAdapter(MainActivity.this, R.layout.list_item, list2, ip, asosId, new IncomingAddListener() {
                @Override
                public void itemSeriesClick(ArrayList<Slave> products, Integer position) {

                }

                @Override
                public void itemSlaveClick(ArrayList<Slave> items, int position) {

                }

            });
            listView2.setAdapter(adapter2);
        }
    }

    private class PutProduct extends AsyncTask<Void, Void, Void> {
        private String urlPutProducts = "http://" + ip + ":8080/application/json/asosslaveput/" + asosId + "/" + thisuUser.getId();
//        http://localhost:8080/application/json/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Малумот сақланйапти");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            httpHandler.putProduct(urlPutProducts, selectProduct);
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
//            sum -= selectProductSum;
//            selectProductSum = (selectProduct.getPrice() * selectProduct.getCount() + selectProduct.getInprice() * selectProduct.getIncount());
//            sum += selectProductSum;

            addList(selectProduct);
            adapter2.notifyDataSetChanged();

            selectedProduct = 0;

            sumPrice.setText("Умуммий сумма: " + sum + " Сўм");
            intent.putExtra("sumprice", sumPrice.getText().toString());
            setProduct(selectProduct);

        }
    }


    private class Finish extends AsyncTask<Void, Void, Void> {
        String urlBlockAsos = "http://" + ip + ":8080/application/json/asosblock";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Малумот сақланйапти");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler httpHandler = new HttpHandler();
            Log.v("MyLog3", "" + asosId);
            httpHandler.makeServiceBlockAsos(urlBlockAsos, asosId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            typeIntent = new Intent(MainActivity.this, TypeChangeActivity.class);
            typeIntent.putExtra("user", intent.getSerializableExtra("user"));
            typeIntent.putExtra("ip", intent.getStringExtra("ip"));
            typeIntent.putExtra("asosId", intent.getIntExtra("asosId", 0));
            typeIntent.putExtra("type", intent.getIntExtra("type", 0));
//            boolean x=smSsender.smsSend("998999671042","ha");
//            Log.v("sms",x+"");
            startActivity(typeIntent);
            finish();
        }


    }


}
