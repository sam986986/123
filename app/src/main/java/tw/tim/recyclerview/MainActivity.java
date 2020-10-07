package tw.tim.recyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
    ArrayList<String> arrayList = new ArrayList<>();
    RecyclerView recyclerView;
    MyAdapter myAdapter;

    private FriendDbHelper dbHper;
    private String DB_FILE = "friends.db";
    private int DBversion = 1;
    private ArrayList<String> recSet;
    private ProgressDialog pd;
    private Handler handler = new Handler();
    private String title, content, url, createdate;
    private HashMap<String, Object> item;
    private ArrayList<Map<String, Object>> mList;  //非固定數量陣列


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
//        if (dbHper.RecCount() == 0) {
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("資料下載中...");
        pd.setCancelable(false);
        pd.show();

        handler.postDelayed(updateTimer, 2000); // 延遲
//        }
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

//        //生成資料
//        for (int i = 0; i < A2J.length; i++) {
//            arrayList.add(A2J[i]);
//        }
        //-----讀取SQLlite裡的Opendata-----
        recSet = dbHper.getRecSet();
        //SimpleAdapter adapter = null;
        //List<Map<String, Object>> mList=null;
        for (int i = 0; i < recSet.size(); i++) {
            String[] fld = recSet.get(i).split("#");
            arrayList.add(fld[0] + "\n" + fld[1] + "\n" + fld[2] + "\n" + fld[3]);
        }

//        SimpleAdapter adapter = new SimpleAdapter(
//                getApplicationContext(),
//                mList,
//                R.layout.item,
//                new String[]{"title"},
//                new int[]{R.id.textView_item}
//        );
//        listView.setAdapter(adapter);
//        listView.setTextFilterEnabled(true);
//        listView.setOnItemClickListener(liON);

//        設置RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        //設置RecyclerView滑動事件
        recyclerViewAction(recyclerView, arrayList, myAdapter);

    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.textView_item);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(arrayList.get(position));
        }

        @Override
        public int getItemCount() {
            return arrayList.size();
        }


    }

    private void recyclerViewAction(RecyclerView recyclerView, final ArrayList<String> choose, final MyAdapter myAdapter) {
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

                if (choose.contains("K") && choose.indexOf("K") != choose.size() - 1) {
                    Collections.swap(choose, position_target, position_dragged);
                    myAdapter.notifyItemMoved(position_target, position_dragged);
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                switch (direction) {
                    case ItemTouchHelper.LEFT:
                    case ItemTouchHelper.RIGHT:
                        choose.remove(position);
                        myAdapter.notifyItemRemoved(position);
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

}