package ict.step9.application.ball;

import java.util.List;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.TextView;

public class BallActivity extends Activity implements SensorEventListener{

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder ab = new AlertDialog.Builder(this);
			ab.setMessage(R.string.message_exit);
			ab.setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO 自動生成されたメソッド・スタブ
					finish();
					
				}
			});
			ab.setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					;
				}
			});
			ab.show();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

	public static Float acceler_y,acceler_x;
	private SensorManager sensorMasnager;
	public PowerManager.WakeLock wl;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new BoardView(this));
        
        this.sensorMasnager = (SensorManager)this.getSystemService(SENSOR_SERVICE);
    }

    @Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		
		List<Sensor> sensors = this.sensorMasnager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if(sensors.size()>0){
			Sensor sensor = sensors.get(0);
			this.sensorMasnager.registerListener(this, sensor,1);
		}
		
		PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
		this.wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK+PowerManager.ON_AFTER_RELEASE, "My Tag");
		this.wl.acquire();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_ball, menu);
        return true;
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO 自動生成されたメソッド・スタブ
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO 自動生成されたメソッド・スタブ
		//event.values[0]がX軸 values[1]がY軸　アプリの仕様によりここでは入れ替える
		acceler_y=event.values[0];
		acceler_x=event.values[1];
		//((TextView)this.findViewById(R.id.textView1)).setText(acceler_x+" , "+acceler_y);
	}

	//アプリを閉じてもSensorの読み取りが継続するので停止される
	@Override
	protected void onStop() {
		// TODO 自動生成されたメソッド・スタブ
		super.onStop();
		this.sensorMasnager.unregisterListener(this);
		if(this.wl.isHeld())
			wl.release();
	}
	
	
}
