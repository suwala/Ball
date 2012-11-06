package ict.step9.application.ball;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class BallActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ball);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ball, menu);
        return true;
    }
}
