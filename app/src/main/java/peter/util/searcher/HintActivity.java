package peter.util.searcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.umeng.analytics.MobclickAgent;

public class HintActivity extends Activity {

    private ObjectAnimator handAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hint_exit_dialog);

        setResult(RESULT_OK);
        View hand = findViewById(R.id.hand);
        handAnimator = ObjectAnimator.ofFloat(hand, "translationX", 0, -PullView.mTouchSlop * 3);
        handAnimator.setDuration(1000);
        handAnimator.start();
        handAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                View title = findViewById(R.id.title);
                title.setVisibility(View.VISIBLE);
                ObjectAnimator titleAnim = ObjectAnimator.ofFloat(title, "alpha", 0, 1).setDuration(500);
                titleAnim.start();
                titleAnim.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        View subTitle = findViewById(R.id.sub_title);
                        subTitle.setVisibility(View.VISIBLE);
                        ObjectAnimator subTitleAnim = ObjectAnimator.ofFloat(subTitle, "alpha", 0, 1).setDuration(500);
                        subTitleAnim.start();
                        subTitleAnim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                View ok = findViewById(R.id.ok);
                                ok.setVisibility(View.VISIBLE);
                                ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
