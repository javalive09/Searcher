package peter.util.searcher.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import peter.util.searcher.R;
import peter.util.searcher.net.UpdateController;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(SettingActivity.this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true); // this sets the button to the back icon
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView settings = (ListView) findViewById(R.id.setting_list);
        if (settings != null) {
            settings.setAdapter(new ArrayAdapter<>(this, R.layout.setting_item, getResources().getStringArray(R.array.settings_name)));
            settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0://share
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_url));
                            sendIntent.setType("text/plain");
                            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                            break;
                        case 1://clear
                            clearCacheFolder(getCacheDir(), 0);
                            ClearCookies();
                            Toast.makeText(SettingActivity.this, R.string.setting_clear, Toast.LENGTH_LONG).show();
                            break;
                        case 2://feedback
                            sendMailByIntent();
                            break;
                        case 3://update
                            UpdateController.instance().checkVersion(SettingActivity.this, true);
                            break;
                        case 4://about
                            showAlertDialog(R.string.action_about, R.string.setting_about);
                            break;
                        case 5: //one step
                            showAlertDialog(R.string.one_step_title, R.string.one_step_txt);
                    }
                }
            });
        }
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void sendMailByIntent() {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

}
