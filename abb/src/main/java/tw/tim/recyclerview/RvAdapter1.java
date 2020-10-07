package tw.tim.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RvAdapter1 extends RecyclerView.Adapter<RvAdapter1.DataViewHolder> implements View.OnClickListener{
    private Context mContext;
    private RecyclerView recyclerView;
    private ArrayList<String> mList;
    public RvAdapter1() {}
    public RvAdapter1(Context mContext, ArrayList<String> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }
    /**
     * 用於建立ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item,null);
        view.setOnClickListener(this);
//使用程式碼設定寬高（xml佈局設定無效時）
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        DataViewHolder holder = new DataViewHolder(view);
        return holder;
    }
    /**
     * 繫結資料
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        holder.tv_data.setText(mList.get(position));
    }
    /**
     * 選項總數
     * @return
     */
    @Override
    public int getItemCount() {
        return mList.size();
    }
    @Override
    public void onClick(View view) {
//根據RecyclerView獲得當前View的位置
        int position = recyclerView.getChildAdapterPosition(view);
//程式執行到此，會去執行具體實現的onItemClick()方法
        if (onItemClickListener!=null){
            onItemClickListener.onItemClick(recyclerView,view,position,mList.get(position));
        }
    }
    /**
     * 建立ViewHolder
     */
    public static class DataViewHolder extends RecyclerView.ViewHolder{
        TextView tv_data;
        public DataViewHolder(View itemView) {
            super(itemView);

            //     先抓這個 textView_item
//            tv_data = (TextView) itemView.findViewById(R.id.textView_item);
        }
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    /**
     * 定義RecyclerView選項單擊事件的回撥介面
     */
    public interface OnItemClickListener{
        //引數（父元件，當前單擊的View,單擊的View的位置，資料）
        void onItemClick(RecyclerView parent,View view, int position, String data);
    }
    /**
     *   將RecycleView附加到Adapter上
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView= recyclerView;
    }
    /**
     *   將RecycleView從Adapter解除
     */
    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }
}
