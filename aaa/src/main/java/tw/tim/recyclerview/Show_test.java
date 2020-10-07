package tw.tim.recyclerview;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Show_test extends AppCompatActivity
{
    private TextView showtest;
    private String test1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_layout);
        setupViewComponent();
    }

    private void setupViewComponent()
    {
        showtest = (TextView)findViewById(R.id.new_t003);
        Bundle bundle1 = new Bundle();
        bundle1 = this.getIntent().getExtras();
        test1 = bundle1.getString("bundle1");
        showtest.setText(test1);
    }
}
