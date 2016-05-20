
package peter.util.searcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.List;
import java.util.Random;

import peter.util.searcher.factory.ExplodeParticleFactory;


public class Board extends RelativeLayout {

	int mTouchSlop;

	public Board(Context context, AttributeSet as) {
		super(context, as);
		setWillNotDraw(true);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	private Random sRNG = new Random();

	private float lerp(float a, float b, float f) {
		return (b - a) * f + a;
	}

	private float randfrange(float a, float b) {
		return lerp(a, b, sRNG.nextFloat());
	}

	public class Head extends TextView {

		float x, y, a;

		float vx, vy;

		float z;

		int h, w;

		private boolean grabbed;
		private float grabx, graby;
		private float grabx_offset, graby_offset;

		private float startDragX, startDragY;

		public Rect mRect;

		public boolean killed;

		int boardWidth, boardHeight;

		ObjectAnimator anim;

		private Head(Context context, AttributeSet as) {
			super(context, as);
		}

		private void show() {
			setVisibility(View.VISIBLE);
			if(anim == null) {
				PropertyValuesHolder pvScaleX = PropertyValuesHolder.ofFloat("scaleX", 0, 1);
				PropertyValuesHolder pvScaleY = PropertyValuesHolder.ofFloat("scaleY", 0, 1);
				anim = ObjectAnimator.ofPropertyValuesHolder(this, pvScaleX, pvScaleY).setDuration(1000);
				anim.setInterpolator(new EaseInOutBackInterpolator());
				anim.start();
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						super.onAnimationEnd(animation);
						anim = null;
					}
				});
			}
		}

		public boolean outEdges() {
			int cell = w / 4;
			if(x < cell//左
	        		||x > boardWidth - cell//右
	        		||y < cell//上
	        		||y > boardHeight - cell) {//下
				return true;
			}
			return false;
		}

		public void getHitRect(Rect outRect) {
		    if (mRect == null){
		        super.getHitRect(outRect);
		    } else {
		        outRect.set(mRect);
		    }
		}

		public String toString() {
			return "x =" + x + "; y=" + y + "; a=" + a + "; va=" + "; vx=" + vx + "; vy=" + vy
					+ "; z=" + z + "; w=" + w + "; h=" + h + "; pvX=" + getPivotX()
					+ "; pvY=" + getPivotY();
		}

		private void reset(boolean anim, long delayMillis) {
			setVisibility(View.INVISIBLE);
			a = randfrange(-10, 10);
//			va = randfrange(-10, 10);

			vx = randfrange(-20, 20) * z;
			vy = randfrange(-20, 20) * z;
			final float boardh = boardHeight;
			final float boardw = boardWidth;

			x = randfrange(w, boardw - w);
			y = randfrange(h, boardh - h);

			if(anim) {
				postDelayed(new Runnable() {
					@Override
					public void run() {
						show();
					}
				}, delayMillis);

			}
		}

		private void update(float dt) {
			if (grabbed) {
				vx = (vx * 0.75f) + ((grabx - x) / dt) * 0.25f;
				x = grabx;
				vy = (vy * 0.75f) + ((graby - y) / dt) * 0.25f;
				y = graby;
			} else {
				x = (x + vx * dt);
				y = (y + vy * dt);
//				a = (a + va * dt);
			}
		}



		@Override
		public boolean onTouchEvent(MotionEvent e) {
			switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				grabbed = true;
				grabx_offset = e.getRawX() - x;
				graby_offset = e.getRawY() - y;
				startDragX = e.getRawX() - grabx_offset;
				startDragY = e.getRawY() - graby_offset;
			case MotionEvent.ACTION_MOVE:
				grabx = e.getRawX() - grabx_offset;
				graby = e.getRawY() - graby_offset;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				grabbed = false;
				if(Math.abs(startDragX - grabx) < mTouchSlop
						&& Math.abs(startDragY - graby) < mTouchSlop) {
					//explosion
					ExplosionField explosionField = new ExplosionField(getContext(), new ExplodeParticleFactory());
					explosionField.explode(this, new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);
							Board.this.removeView(Head.this);
							EnterActivity act = (EnterActivity) getContext();
							String word = (String) getTag();
							act.startSearch(word);
						}
					});
				}
				break;
			}

			Log.i("peter", "grabx_offset= " + grabx_offset+ "; grabx =" + grabx);
			return true;
		}
	}

	private TimeAnimator mAnim;

	private int random(int small, int big) {
		return (int)(small + Math.random()*(big - small + 1));
	}


	private void setBackGroundDra(View v, int index) {
		switch (index) {
			case 0:
				v.setBackground(getResources().getDrawable(R.drawable.oval1));
				break;
			case 1:
				v.setBackground(getResources().getDrawable(R.drawable.oval2));
				break;
			case 2:
				v.setBackground(getResources().getDrawable(R.drawable.oval3));
				break;
			case 3:
				v.setBackground(getResources().getDrawable(R.drawable.oval4));
				break;
			case 4:
				v.setBackground(getResources().getDrawable(R.drawable.oval5));
				break;
			default:
				v.setBackground(getResources().getDrawable(R.drawable.oval5));
		}
	}

	private void init(List<String> list) {
		removeAllViews();
		int cell = getContext().getResources().getDimensionPixelOffset(R.dimen.hot_oval_d);
        for(int i = 0, size = list.size(); i < size; i++) {
			String hot = list.get(i);
			Head nv = new Head(getContext(), null);
			nv.z = ((float) random(0, size) / size);
			nv.z *= nv.z;

			nv.w = cell;
			nv.h= cell;
			nv.setGravity(Gravity.CENTER);
			nv.setTextColor(Color.WHITE);
			nv.setText(hot);
			setBackGroundDra(nv, i);
			nv.boardWidth = getMeasuredWidth();
			nv.boardHeight = getMeasuredHeight();
			nv.reset(true, 500);
			nv.x = (randfrange(nv.w/2, nv.boardWidth - nv.w/2));
			nv.y = (randfrange(nv.h/2, nv.boardHeight - nv.h/2));
			nv.setTag(hot);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(cell, cell);
			nv.setPadding(10, 10, 10, 10);
			addView(nv, params);
		}

		if (mAnim != null) {
			mAnim.cancel();
		}
		mAnim = new TimeAnimator();
		mAnim.setTimeListener(new TimeAnimator.TimeListener() {

			@Override
			public void onTimeUpdate(TimeAnimator animation, long totalTime,
					long deltaTime) {

				for (int i = 0, count = getChildCount(); i < count; i++) {
					View v = getChildAt(i);
					if (!(v instanceof Head)) {
						continue;
					}
					Head nv = (Head) v;
					if(nv.killed) {
						continue;
					}
					nv.update(deltaTime / 1000f);
					nv.setRotation(nv.a);

					float pvX = nv.getPivotX();
					float pvY = nv.getPivotY();
					float x = nv.x - pvX;
					float y = nv.y - pvY;
					nv.setX(x);
					nv.setY(y);
			        RectF rect = new RectF();
			        rect.top = 0;
			        rect.bottom = (float) nv.h;
			        rect.left = 0;
			        rect.right = (float) nv.w;
			        rect.offset(nv.getX(), nv.getY());

			        if (nv.mRect == null) {
			        	nv.mRect = new Rect();
			        }

			        rect.round(nv.mRect);

			        if(nv.outEdges()) {
						nv.reset(true, 0);
			        }

				}
			}
		});
	}

	public void startAnimation(final List<String> list) {
		stopAnimation();
		if (mAnim == null) {
			post(new Runnable() {
				public void run() {
					init(list);
					mAnim.start();
				}
			});
		} else {
			mAnim.start();
		}
	}

	public void stopAnimation() {
		if (mAnim != null) {
			mAnim.cancel();
			mAnim = null;
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopAnimation();
	}

	@Override
	public boolean isOpaque() {
		return false;
	}


}