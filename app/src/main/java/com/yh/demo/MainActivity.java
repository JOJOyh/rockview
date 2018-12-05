package com.yh.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jojo.myrockview.MyView;


public class MainActivity extends AppCompatActivity {
    private MyView myView, myView1;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myView = findViewById(R.id.my_view);
        textView = findViewById(R.id.text);
        myView.setCallback(new MyView.Callback() {
            @Override
            public void call(MyView.Forward forward) {
                switch (forward) {
                    case UP:
                        textView.setText("前进");
                        break;
                    case DOWN:
                        textView.setText("后退");
                        break;
                    case STOP:
                        textView.setText("停止");
                        break;
                    case FORCE_UP:
                        textView.setText("加速前进");
                        break;
                }
            }
        });
        myView1 = findViewById(R.id.my_view1);
        myView1.setCallback(new MyView.Callback() {
            @Override
            public void call(MyView.Forward forward) {
                switch (forward) {
                    case LEFT:
                        textView.setText("左转");
                        break;
                    case RIGHT:
                        textView.setText("右转");
                        break;
                    case STOP:
                        textView.setText("停止");
                        break;
                }
            }
        });
    }

}
