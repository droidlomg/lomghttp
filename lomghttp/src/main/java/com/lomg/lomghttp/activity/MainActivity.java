package com.lomg.lomghttp.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lomg.lomghttp.Lomg;
import com.lomg.lomghttp.LomgCallBack;
import com.lomg.lomghttp.R;
import com.lomg.lomghttp.Request;
import com.lomg.lomghttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String BASE_URL = "http://www.baidu.com/";
    public static Context context;
    private Lomg lomg;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = (TextView) findViewById(R.id.t1);
        textView.setOnClickListener(this);
        lomg = new Lomg.Builder(BASE_URL).build();

    }

    @Override
    public void onClick(View view) {
        lomg.withPath("")
                .tag(MainActivity.class)
                .addParam("s", "lomghttp")
                .get(new LomgCallBack() {
                    @Override
                    public void onFail(Request request, IOException exception) {
                        Log.e("onFail", new String(request.getUrl()) + " ");
                    }

                    @Override
                    public void onSuccess(Response response) throws IOException {
                        Log.e("onSuccess", new String(response.body) + " ");
                    }
                });
    }
}
