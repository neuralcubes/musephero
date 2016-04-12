package uk.co.neuralcubes.neuralates;

import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UIActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ui_activity);
        Resources res = getResources();
        TextView tv = (TextView)findViewById(R.id.control1).findViewById(R.id.player_label);
        tv.setText(res.getString(R.string.player, 1));
        tv = (TextView)findViewById(R.id.control2).findViewById(R.id.player_label);
        tv.setText(res.getString(R.string.player, 2));
    }
}
