package peter.util.searcher.tab;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.update.AsynWindowHandler;
import peter.util.searcher.update.UpdateController;

/**
 *
 * Created by peter on 2016/11/18.
 *
 */

public class SettingTab extends LocalViewTab {

    AsynWindowHandler windowHandler;

    public SettingTab(MainActivity activity) {
        super(activity);
    }

    @Override
    public int onCreateViewResId() {
        return R.layout.tab_setting;
    }

    @Override
    public void onCreate() {
        windowHandler = new AsynWindowHandler(mainActivity);
        ListView settings = (ListView) mainActivity.findViewById(R.id.setting_list);
        if(settings != null) {
            settings.setAdapter(new ArrayAdapter<>(mainActivity, R.layout.setting_item, mainActivity.getResources().getStringArray(R.array.settings_name)));
            settings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0://share
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, mainActivity.getString(R.string.app_url));
                            sendIntent.setType("text/plain");
                            mainActivity.startActivity(Intent.createChooser(sendIntent, mainActivity.getString(R.string.share_title)));
                            break;
                        case 1://clear
                            mainActivity.clearCacheFolder(mainActivity.getCacheDir(), 0);
                            mainActivity.ClearCookies(mainActivity);
                            Toast.makeText(mainActivity, R.string.setting_clear, Toast.LENGTH_LONG).show();
                            break;
                        case 2://feedback
                            mainActivity.sendMailByIntent();
                            break;
                        case 3://update
                            UpdateController.instance().checkVersion(windowHandler, true);
                            break;
                        case 4://about
                            mainActivity.showAlertDialog(mainActivity.getString(R.string.action_about), mainActivity.getString(R.string.setting_about));
                            break;
                        case 5://enter
                            AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                            builder.setPositiveButton(R.string.fast_enter_setting, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                                    mainActivity.startActivity(intent);
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.setTitle(R.string.action_one_key);
                            dialog.setMessage(mainActivity.getString(R.string.action_one_key_txt));
                            dialog.show();

                    }
                }
            });
        }
    }

    @Override
    public void onDestory() {
    }

    @Override
    public String getSearchWord() {
        return "";
    }

    @Override
    public String getTitle() {
        return mainActivity.getString(R.string.fast_enter_setting);
    }

    @Override
    public String getUrl() {
        return URL_SETTING;
    }

}
