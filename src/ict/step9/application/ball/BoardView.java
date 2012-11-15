package ict.step9.application.ball;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class BoardView extends View {
	
	public Timer timer;
	public Bitmap ball;
	public int w,h;
	public static int ballR=16;
	public static Context mContext;
	public static Vibrator vib;
	
	public float new_y=0;
	public float speed_y=0;
	public float new_x=0;
	public float speed_x=0;
	public float scale=25f;
	public float time=0.04f;
	

	public BoardView(Context context) {
		super(context);
		// TODO 自動生成されたコンストラクター・スタブ
		
		mContext = context;
		
		vib=((Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE));
		this.setFocusable(true);
		Display disp = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		this.w=disp.getWidth();
		this.h=disp.getHeight();
		
		if(w>480)
			ballR =16;
		else
			ballR = 8;
		
		this.ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		this.ball = Bitmap.createScaledBitmap(ball, ballR*2, ballR*2,false );
		
	}
	
	public void startTimer(){
		
		if(this.timer!=null)
			this.timer.cancel();
		this.timer=new Timer();
		final android.os.Handler handler = new android.os.Handler();
		this.timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO 自動生成されたメソッド・スタブ
						newPos();
						//定期的描画しなおす
						BoardView.this.invalidate();
					}
				});
			}
		},0, (int)(1000/this.scale));
	}
	
	public void newPos(){
		
		//加速度センサー分を加算
		this.new_x = this.new_x + (((BallActivity.acceler_x*this.time*this.scale)/2)+this.speed_x*this.time*this.scale);
		this.new_y = this.new_y + (((BallActivity.acceler_y*this.time*this.scale)/2)+this.speed_y*this.time*this.scale);
		
		//はみ出した際の処理　壁にぶつかったらバイブ
		if(this.new_x >= this.w-(ballR)){
			this.new_x = w -(ballR);
			this.speed_x =- Math.abs(this.speed_x)*0.8f;
			if(Math.abs(this.speed_x)>1){
				vib.vibrate(50);
			}
		}else if(this.new_x <= ballR){
			this.new_x = ballR;
			this.speed_x=Math.abs(this.speed_x)*0.8f;
			if(Math.abs(this.speed_x)>1){
				vib.vibrate(50);
			}
		}else{
			this.speed_x = (this.speed_x + (BallActivity.acceler_x*this.time*this.scale))*0.95f;
		}
		
		if(this.new_y >= this.h-(ballR)){
			this.new_y = this.h -(ballR);
			this.speed_y =- Math.abs(this.speed_y)*0.8f;
			//this.speed_y = 0;
			if(Math.abs(this.speed_y)>1){
				vib.vibrate(50);
			}
		}else if(this.new_y <= ballR){
			this.new_y = ballR;
			this.speed_y=Math.abs(this.speed_y)*0.8f;
			//this.speed_y = 0;
			if(Math.abs(this.speed_y)>1){
				vib.vibrate(50);
			}
		}else{
			this.speed_y = (this.speed_y + (BallActivity.acceler_y*this.time*this.scale))*0.95f;
		}
		
		//Log.d("ball",String.valueOf(this.speed_x)+":"+String.valueOf(this.new_y));
	}

	//画面の表示状態が変化したときに呼ばれる
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		// TODO 自動生成されたメソッド・スタブ
		super.onWindowVisibilityChanged(visibility);
		
		if(visibility == View.VISIBLE){
			startTimer();
		}else{
			this.timer.cancel();
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		// TODO 自動生成されたメソッド・スタブ
		super.onDraw(canvas);
		
		canvas.drawColor(Color.rgb(0, 128, 0));
		canvas.drawBitmap(this.ball, this.new_x - ballR, this.new_y - ballR,null);
		//APILv11以降のみ追記 this.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
	}

}
