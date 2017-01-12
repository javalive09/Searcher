package peter.util.searcher;

import android.Manifest;
import android.app.Activity;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by peter on 2017/1/12.
 */

public class VoiceRecognizer {
    // 语音听写对象
    private SpeechRecognizer mIat;

    private static final class HOLDER{
        private static VoiceRecognizer INSTANCE = new VoiceRecognizer();
    }

    public static VoiceRecognizer instance() {
        return HOLDER.INSTANCE;
    }

    private VoiceRecognizer() {
        mIat = SpeechRecognizer.createRecognizer(Searcher.context(), new InitListener() {

            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(Searcher.context(), "error code " + code, Toast.LENGTH_SHORT).show();
                }
            }
        });
        setParam();
    }

    private void initAndShow(final Activity activity, RecognizerDialogListener mRecognizerDialogListener) {
        RecognizerDialog mIatDialog = new RecognizerDialog(activity, new InitListener() {

            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Toast.makeText(activity, "error code " + code, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mIatDialog.setListener(mRecognizerDialogListener);
        mIatDialog.show();
    }

    public void showVoiceDialog(final Activity activity, final RecognizerDialogListener mRecognizerDialogListener) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        initAndShow(activity, mRecognizerDialogListener);
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(activity, R.string.record_audio_permission, Toast.LENGTH_LONG).show();
                    }
                });
    }


    public String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);

            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                // 转写结果词，默认使用第一个结果
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }


    private void setParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

    }

}
