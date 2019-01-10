package com.mods.grx.circularbattery;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.DarkIconDispatcherImpl;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;

public class MainActivity extends AppCompatActivity {


    int currentDark = 0;

    GrxBatteryMeterView grxBatteryMeterView, grxBatteryMeterView1;

    Button buttonlighter, buttondarker, buttonupdate;

    LinearLayout container;

    Dependency dependency;

    Drawable background;

    DarkIconDispatcherImpl darkIconDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dependency = new Dependency(this);

        setContentView(R.layout.activity_main);


        background = new ColorDrawable(0xff666666);

        grxBatteryMeterView = (GrxBatteryMeterView) findViewById(R.id.rootview).findViewWithTag("grxbattery");
        grxBatteryMeterView1 = (GrxBatteryMeterView) findViewById(R.id.rootview).findViewWithTag("grxbattery1");
        container  = findViewById(R.id.batcontainer);
        container.setBackground(background);

        darkIconDispatcher = (DarkIconDispatcherImpl) Dependency.get(DarkIconDispatcher.class);

        buttonlighter = findViewById(R.id.buttonlighter);
        buttonlighter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentDark<10) {
                    currentDark+=2;
                    background.setAlpha(  (int) (255-(255* currentDark / 10)   ));
                    darkIconDispatcher.simulateTint((float)currentDark/10f);
                }
            }
        });

        buttondarker = findViewById(R.id.buttondarker);
        buttondarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentDark>0){
                    currentDark-=2;
                    background.setAlpha(  (int) (255-(255*currentDark/10)   ));
                    darkIconDispatcher.simulateTint((float) currentDark / 10f);
                }
            }
        });

        buttonupdate = findViewById(R.id.update);
        buttonupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grxBatteryMeterView.updateBatteryOptions();
                grxBatteryMeterView1.updateBatteryOptions();
            }
        });

    }

}

