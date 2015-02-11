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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.Toast;
//import android.R;

public class ChatActivity extends Activity {
    SharedPreferences prefs;
    List<NameValuePair> params;
    EditText chat_msg;
    Button send_btn;
    Bundle bundle;
   
    public static final boolean FROMOTHER = true;
    public static final boolean FROMMYSELF = false;

    public static final int INT_OTHER = 1;
    public static final int INT_SELF = 0;
    
    
    
	// bubble code
	private DiscussArrayAdapter ADAPTER;
	private ListView LV;
	
	
    SQLiteDatabase db;
    String newQuery = "create table msgbox (id integer primary key , name text, msg text, isother integer);";
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
       
        
        //bubble code
        LV = (ListView) findViewById(R.id.list_view_messages);
        ADAPTER = new DiscussArrayAdapter(getApplicationContext(), R.layout.listitem_discuss);

		LV.setAdapter(ADAPTER);
        
        

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
        
 
        selectData();
        
        /* 상대방이 한말 표시 */
        if(bundle.getString("name") != null){
        	
        	ADAPTER.add(new OneComment(FROMOTHER, bundle.getString("msg")));
        }

        //내가 한말 표시
        //내가 입력하는 칸:chat_msg
        chat_msg = (EditText)findViewById(R.id.chat_msg);
        send_btn = (Button)findViewById(R.id.sendbtn);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	
                insertData("You",chat_msg.getText().toString(), INT_SELF);
                Log.d("test", "you: " + chat_msg.getText().toString());
                
    			ADAPTER.add(new OneComment(FROMMYSELF, chat_msg.getText().toString()));
				
                new Send().execute();
            }
        });
    }

    
    private void insertData(String name,String msg, int isother){
    	 
        db.beginTransaction();
 
        try{
            String sql = "insert into msgbox (name,msg,isother) values ('"+ name +"','"+ msg +"','"+ isother +"');";
            db.execSQL(sql);
            db.setTransactionSuccessful();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            db.endTransaction();
        }
 
    }
    
    public void selectData(){
    	boolean selfOrNot = true;
        String sql = "select * from msgbox";
        
        Cursor result = db.rawQuery(sql, null);
        result.moveToFirst();
        while(!result.isAfterLast()){
        	
        	
        	//table에 누가 보냈는지 값도 있어야 함....
        	
        	if( result.getInt(3) == 1)
        	{selfOrNot = FROMOTHER;}
        	
        	else if(result.getInt(3) == 0)
        	{selfOrNot = FROMMYSELF;}
        	
        	
        	result.getString(3);
        	ADAPTER.add(new OneComment(selfOrNot, result.getString(2)));
            
            result.moveToNext();
        }
        result.close();
    }
    
    
    
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String p_msg = intent.getStringExtra("msg");
            String str1 = intent.getStringExtra("fromname");
            String str2 = intent.getStringExtra("fromu");
            if(str2.equals(bundle.getString("mobno"))){

                insertData(str1, p_msg, INT_OTHER);
                Log.d("test", "2  name: " + str1 + " msg: " + p_msg); 
                
    			ADAPTER.add(new OneComment(FROMOTHER, p_msg));
				
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