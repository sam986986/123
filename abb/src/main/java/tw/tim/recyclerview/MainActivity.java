package tw.tim.recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {
    String[] A2J = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"};
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
    RecyclerView recyclerView;

    private FriendDbHelper dbHper;
    private String DB_FILE = "friends.db";
    private int DBversion = 1;
    private ArrayList<String> recSet;
    private ProgressDialog pd;
    private Handler handler = new Handler();
    private String title, content, url, createdate;
    private HashMap<String, Object> item;
    private ArrayList<Map<String, Object>> mList;  //非固定數量陣列

    RvAdapter1 adapter;
    private Intent test_intent = new Intent();

    private MyAdapter myAdapter;
    private String[] fld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDB();
        dowload();
    }

    private void initDB() {
        if (dbHper == null) {
            dbHper = new FriendDbHelper(getApplicationContext(), DB_FILE, null, DBversion);
        }
        recSet = dbHper.getRecSet();
    }

    private void dowload() {
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("資料下載中...");
        pd.setCancelable(false);
        pd.show();

        handler.postDelayed(updateTimer, 2000); // 延遲
    }

    private Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            getOpenData();
            pd.cancel();
            showdata();
        }
    };

    private void getOpenData() {
        //if (dbHper.RecCount_news() == 0) {
        try {
            String Task_opendata
//                    = new TransTask().execute("https://www.fda.gov.tw/DataAction").get();
                    = new TransTask().execute("https://opendata.cwb.gov.tw/api/v1/rest/datastore/F-C0032-001?Authorization=CWB-F06F9A02-627A-4B10-84FD-D1A3E4923685").get();

            JSONObject jsonData = new JSONObject(Task_opendata);
            JSONObject records = jsonData.getJSONObject("records");
            JSONArray location = records.getJSONArray("location");

            dbHper.clearRec();

            for (int i = 0; i < location.length(); i++) {
                JSONObject taipei = location.getJSONObject(i);  //目前只抓台北市  可用 i 代替 改為各縣市
                JSONArray weatherElement = taipei.getJSONArray("weatherElement");
                JSONObject MinT = weatherElement.getJSONObject(2);
                JSONArray time = MinT.getJSONArray("time");
                JSONObject nowT = time.getJSONObject(0);
                String startTime = nowT.getString("startTime");
                String endTime = nowT.getString("endTime");
                JSONObject parameter = nowT.getJSONObject("parameter");
                String parameterName = parameter.getString("parameterName");
                String parameterUnit = parameter.getString("parameterUnit");
                String parameterStr = parameterName + parameterUnit;

                String msg = null;
                long rowID = dbHper.insertRec_news(startTime, endTime, parameterStr); //真正執行SQL
                if (rowID != -1) {
                    msg = "新增記錄  成功 ! \n" + "目前資料表共有 " + dbHper.RecCount() + " 筆記錄 !";
                } else {
                    msg = "新增記錄  失敗 ! ";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void showdata() {
        //-----讀取SQLlite裡的Opendata-----
        recSet = dbHper.getRecSet();
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("startTime",fld[1]);
            hashMap.put("endTime",fld[2]);
            hashMap.put("parameterStr",fld[3]);
            hashMap.put("VIEW_TYPE","0");
            arrayList.add(hashMap);
            HashMap<String,String> hashMap2 = new HashMap<>();
            hashMap2.put("VIEW_TYPE","1");
            arrayList.add(hashMap2);
        }
//        設置RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        myAdapter = new MyAdapter(arrayList);
        recyclerView.setAdapter(myAdapter);
        //設置RecyclerView滑動事件
        recyclerViewAction(recyclerView, arrayList, myAdapter);

        myAdapter.setOnTransButtonClick(hashMap -> {
            String getTrans = hashMap.get("startTime")+"\n"+hashMap.get("endTime")+"\n"+hashMap.get("parameterStr");
//            String getTrans1 = hashMap.get("endTime");
//            String getTrans2 = hashMap.get("parameterStr");
            //在這裡intent去下一頁，參數值修改上面bundle過去 TODO
            test_intent.setClass(MainActivity.this, Show_test.class);
            Bundle bundle1 = new Bundle();
            bundle1.putString("bundle1", getTrans);
//            bundle1.putString("bundle1", getTrans1);
//            bundle1.putString("bundle1", getTrans2);
            test_intent.putExtras(bundle1);
            startActivity(test_intent);
//            Toast.makeText(this, getTrans, Toast.LENGTH_SHORT).show();
        });//Click
    }

    private void recyclerViewAction(RecyclerView recyclerView, final ArrayList<HashMap<String, String>> choose, final RecyclerView.Adapter myAdapter) {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN
                        , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView
                    , @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int position_dragged = viewHolder.getAdapterPosition();
                int position_target = target.getAdapterPosition();
                Collections.swap(choose, position_dragged, position_target);
                myAdapter.notifyItemMoved(position_dragged, position_target);

//                if (choose.contains("K") && choose.indexOf("K") != choose.size() - 1) {
//                    Collections.swap(choose, position_target, position_dragged);
//                    myAdapter.notifyItemMoved(position_target, position_dragged);
//                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                    case ItemTouchHelper.RIGHT:
                        int aa = choose.size();
                        myAdapter.notifyItemRemoved(position);
                        choose.remove(position);
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark))
                        .addActionIcon(R.drawable.ic_android_black_24dp)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        helper.attachToRecyclerView(recyclerView);
    }

    private class TransTask extends AsyncTask<String, Void, String> {
        String ans;

        @Override
        protected String doInBackground(String... params) {
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                String line = in.readLine();
                while (line != null) {
                    Log.d("HTTP", line);
                    sb.append(line);
                    line = in.readLine();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ans = sb.toString();
            //------------
            return ans;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("s", "s:" + s);
            parseJson(s);
        }

        private void parseJson(String s) {

        }
    }

    private static class MyAdapter extends RecyclerView.Adapter{

        private final ArrayList<HashMap<String, String>> arrayList;
        public OnTransClick onItemClick;
        public MyAdapter(ArrayList<HashMap<String,String>> arrayList) {
            this.arrayList = arrayList;
        }

        @Override
        public int getItemViewType(int position) {
//            return position % 2;
            int getType = Integer.parseInt(arrayList.get(position).get("VIEW_TYPE"));
            return getType;
        }

        /**設置將資料傳回Activity的接口*/
        public void setOnTransButtonClick(OnTransClick onItemClick){
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                return new B_TypeMyView(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.b_type_item, parent, false));
            } else {
                return new A_TypeMyView(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.a_type_item, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            /**將holder強制轉型為"MyViewHolders"類別，使介面Ａ/Ｂ都可以獲得onBindViewHolder內容*/
            ((MyViewHolders) holder).bindViewHolder(arrayList.get(position));
            /**判斷該item的介面是處於哪一個介面*/
            if (holder instanceof A_TypeMyView){
                //你可以試著為他加入點擊事件～
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onItemClick.OnTransButtonClick(arrayList.get(position));
                        int aa=0;
                    }
                });
            }else if (holder instanceof B_TypeMyView){
                B_TypeMyView bTypeMyView = (B_TypeMyView) holder;
                /**設置翻譯按鈕的點擊事件*/

            }
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }

        /**設置點擊方法，使點擊後取得到的內容能傳回MainActivity*/
        public interface OnTransClick{
            void OnTransButtonClick(HashMap<String,String> hashMap);
        }
    }
//private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    public class ViewHolder extends RecyclerView.ViewHolder {
//        TextView textView;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textView = itemView.findViewById(R.id.textView_Word);
//        }
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.textView.setText(arrayList.get(position));
//    }
//
//    @Override
//    public int getItemCount() {
//        return arrayList.size();
//    }
//
//
//}
}