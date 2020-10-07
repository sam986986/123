package tw.tim.recyclerview;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class B_TypeMyView extends MyViewHolders {


    private final ImageView imageView01;
    public B_TypeMyView(@NonNull View itemView) {
        super(itemView);

        imageView01 = itemView.findViewById(R.id.imageView01);
    }
    /**將資料綁到介面Ｂ的內容*/
    @Override
    public void bindViewHolder(HashMap<String, String> hashMap) {

    }
}