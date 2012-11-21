package ict.step9.application.ball;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Paint.Align;
import android.graphics.Path.Direction;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class BoardView extends View {
	
	/*
	 * Handlerを完全修飾子で指定している理由
	 * startTimer()のハンドラ内でのタイムの減算処理がありえん
	 * スタート位置のランダム化とか？
	 */
	
	public Timer timer;
	public Bitmap ball;
	public int w,h;
	public static int ballR=16;
	public static Context mContext;
	public static Vibrator vib;
	public Path hole,holeCenter;
	public Region screen,rHole,rHoleCenter;
	public Boolean inTheHole=false;
	
	public float new_y=0;
	public float speed_y=0;
	public float new_x=0;
	public float speed_x=0;
	public float scale=25f;
	public float time=0.04f;
	
	public static final int gameDuration = 20000;//制限時間ms
	public int score,hiScore,timeLeft;//-,-,残り時間
	

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
		
		this.hole = new Path();
		hole.addCircle(w/2, h/2-(ballR/8), (int)(ballR*1.5), Direction.CW);
		this.screen = new Region(0,0,w,h);
		this.rHole = new Region();
		this.rHole.setPath(hole, screen);
		this.holeCenter = new Path();
		this.holeCenter.addCircle(w/2, h/2, (int)(ballR*1.2), Direction.CCW);
		this.rHoleCenter = new Region();
		this.rHoleCenter.setPath(this.holeCenter, this.screen);
		
		//スコアの処理
		SharedPreferences prefs = mContext.getSharedPreferences("BallScorePrefs", Context.MODE_PRIVATE);
		this.hiScore = prefs.getInt("hiScore", 0);
		this.timeLeft = 0;
		
		//Viewに対してのタッチリスナーの登録
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO 自動生成されたメソッド・スタブ
				
				if(timeLeft <= 0){
					new_x = 0;
					new_y = 0;
					score = 0;
					timeLeft = gameDuration;
					startTimer();
				}
				return false;
			}
		});
		
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
						//定期的に描画する
						BoardView.this.invalidate();
						
						//いくらなんでも手動過ぎるだろうｗ
						//timeLeft = timeLeft-40;
						
						timeLeft = timeLeft-(int)(1000/scale);						
						if(timeLeft<=0)
							timer.cancel();
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
		
		//穴にボールがあるとき
		if(rHole.contains((int)new_x, (int)new_y)){
			//穴に落とすかチェック
			if(this.rHoleCenter.contains((int)new_x, (int)new_y)&Math.abs(BallActivity.acceler_x)<=1.0&
					Math.abs(BallActivity.acceler_y)<=1.0&
					Math.abs(speed_x)<1.50 & Math.abs(speed_y)<=1.50){
				this.getIn();
			}else{
				vib.vibrate(50);
				speed_x = (int)(speed_x+(w/2-new_x)*0.2);
				speed_y = (int)(speed_y+(h/2-new_y)*0.2);
			}
		}
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
		//ホールを描く
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);		
		canvas.drawPath(hole, paint);
		paint.setColor(Color.BLACK);
		canvas.drawPath(holeCenter,paint);
		
		
		canvas.drawBitmap(this.ball, this.new_x - ballR, this.new_y - ballR,null);
		//APILv11以降のみ追記 this.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
		//穴に落ちたときの処理
		if(this.inTheHole){
			paint.setColor(Color.argb(180, 0, 0, 0));
			canvas.drawPath(holeCenter, paint);
		}
		
		paint.setColor(Color.BLACK);
		paint.setTextSize(h/16);
		paint.setTextAlign(Align.RIGHT);
		
		canvas.drawText(getResources().getString(R.string.label_timeleft)+String.valueOf((int)(timeLeft/1000)),(int)(w*0.95) , h/12, paint);
		canvas.drawText(getResources().getString(R.string.label_score)+String.valueOf(score), (int)(w*0.95), h/6, paint);
		canvas.drawText(getResources().getString(R.string.label_hiscore)+String.valueOf(hiScore),(int)(w*0.95) , h/4, paint);
		
		if(this.timeLeft<=0){
			paint.setTextAlign(Align.CENTER);
			canvas.drawText(getResources().getString(R.string.message_replay), w/2, (int)(h*0.9),paint);
		}
		
	}
	
	//穴に入ったとき
	public void getIn(){
		
		this.speed_x=0;
		this.speed_y=0;
		this.new_x=w/2;
		this.new_y=h/2;
		this.inTheHole = true;
		
		if(this.timer!=null){
			this.timer.cancel();
		}
		
		this.addPoint(10);
		
		this.timer = new Timer();
		final android.os.Handler handler = new android.os.Handler();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO 自動生成されたメソッド・スタブ
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// TODO 自動生成されたメソッド・スタブ
						new_x = 0;
						new_y = 0;
						inTheHole = false;
						startTimer();
					}
				});
				
			}
		},500);
	}
	
	public void addPoint(int point){
		this.score = score+point;
		if(this.score>this.hiScore){
			
			this.hiScore = this.score;
			SharedPreferences prefs = mContext.getSharedPreferences("BallScorePrefs",Context.MODE_PRIVATE);
			Editor editor = prefs.edit();
			editor.putInt("hiScore", this.hiScore);
			editor.commit();
		}
	}

}
