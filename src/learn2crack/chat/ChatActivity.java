package learn2crack.chat;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
//import android.R;

public class ChatActivity extends Activity {
    SharedPreferences prefs;
    List<NameValuePair> params;
    EditText chat_msg;
    Button send_btn;
    Bundle bundle;
    TableLayout tab;
    
    ArrayList<String> arrlist = null;
    ArrayList<String> arr_id_list = null;
    
    
    SQLiteDatabase db;
    String newQuery = "create table dialogue (id integer primary key , name text, msg text);";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        setContentView(R.layout.activity_chat);
        tab = (TableLayout)findViewById(R.id.tab);

        prefs = getSharedPreferences("Chat", 0);
        bundle = getIntent().getBundleExtra("INFO");
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("CURRENT_ACTIVE", bundle.getString("mobno"));
        edit.commit();
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));
        
        db =  openOrCreateDatabase("dbname", MODE_WORLD_WRITEABLE, null);
        try{
            db.execSQL(newQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //insertData("tests");
        
        arrlist = new ArrayList<String>();
        arr_id_list = new ArrayList<String>();
 
        selectData();
        
        /* 상대방이 한말 표시 */
        if(bundle.getString("name") != null){
            TableRow tr1 = new TableRow(getApplicationContext());
            tr1.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView textview = new TextView(getApplicationContext());
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(Html.fromHtml("<b>"+bundle.getString("name")+" : </b>"+bundle.getString("msg")));
            tr1.addView(textview);
            tab.addView(tr1);
        }

        //내가 한말 표시
        //내가 입력하는 칸:chat_msg
        chat_msg = (EditText)findViewById(R.id.chat_msg);
        send_btn = (Button)findViewById(R.id.sendbtn);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tr2 = new TableRow(getApplicationContext());
                tr2.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                TextView textview = new TextView(getApplicationContext());
                textview.setTextSize(20);
                textview.setTextColor(Color.parseColor("#A901DB"));
                textview.setText(Html.fromHtml("<b>You : </b>" + chat_msg.getText().toString()));
                
                //
                insertData("You: ",chat_msg.getText().toString());
                Log.d("test", "you: " + chat_msg.getText().toString());
                
                tr2.addView(textview);
                tab.addView(tr2);
                new Send().execute();
            }
        });
    }

    
    private void insertData(String name,String msg){
    	 
        db.beginTransaction();
 
        try{
            String sql = "insert into dialogue (name,msg) values ('"+ name +"','"+ msg +"');";
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
 
    }
    
    public void selectData(){
        String sql = "select * from dialogue";
        
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();
        while(!result.isAfterLast()){
        	
        	TableRow tr1 = new TableRow(getApplicationContext());
            tr1.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            TextView textview = new TextView(getApplicationContext());
            textview.setTextSize(20);
            textview.setTextColor(Color.parseColor("#0B0719"));
            textview.setText(Html.fromHtml("<b>"+result.getString(1)+" : </b>"+result.getString(2)));
            tr1.addView(textview);
            tab.addView(tr1);
            
            result.moveToNext();
        }
        result.close();
    }
    
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String str = intent.getStringExtra("msg");
            String str1 = intent.getStringExtra("fromname");
            String str2 = intent.getStringExtra("fromu");
            if(str2.equals(bundle.getString("mobno"))){

                TableRow tr1 = new TableRow(getApplicationContext());
                tr1.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                TextView textview = new TextView(getApplicationContext());
                textview.setTextSize(20);
                textview.setTextColor(Color.parseColor("#0B0719"));
                textview.setText(Html.fromHtml("<b>"+str1+" : </b>"+str));
                
                insertData(str1, str);
                Log.d("test", "2  name: " + str1 + " msg: " + str); 
                
                tr1.addView(textview);
                tab.addView(tr1);
            }
        }
    };
    private class Send extends AsyncTask<String, String, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser json = new JSONParser();
            params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("from", prefs.getString("REG_FROM","")));
            params.add(new BasicNameValuePair("fromn", prefs.getString("FROM_NAME","")));
            params.add(new BasicNameValuePair("to", bundle.getString("mobno")));
            params.add((new BasicNameValuePair("msg",chat_msg.getText().toString())));

            JSONObject jObj = json.getJSONFromUrl("http://54.65.196.112:8000/send",params);
            //JSONObject jObj = json.getJSONFromUrl("http://54.65.196.112:8000/send",params);
            return jObj;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            chat_msg.setText("");

            String res = null;
            try {
                res = json.getString("response");
                if(res.equals("Failure")){
                    Toast.makeText(getApplicationContext(),"The user has logged out. You cant send message anymore !",Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}