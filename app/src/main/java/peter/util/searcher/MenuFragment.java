package peter.util.searcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MenuFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] colors = getResources().getStringArray(R.array.menus_name);
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.setting_item, R.id.setting_item, colors);
        setListAdapter(colorAdapter);
    }

    @Override
    public void onListItemClick(ListView lv, View v, int position, long id) {
        switch (position) {
            case 0://设置
                startActivity(new Intent(getActivity(), SettingActivity.class));
                ((MainActivity)getActivity()).getSlidingMenu().toggle(false);
                break;
            case 1://收藏
                startActivity(new Intent(getActivity(), FavoriteActivity.class));
                ((MainActivity)getActivity()).getSlidingMenu().toggle(false);
                break;
            case 2://历史记录
                startActivity(new Intent(getActivity(), HistoryActivity.class));
                ((MainActivity)getActivity()).getSlidingMenu().toggle(false);
                break;
            case 3://分享
                String url = ((MainActivity)getActivity()).getMainFragment().getCurrentUrl();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_title)));
                ((MainActivity)getActivity()).getSlidingMenu().toggle(false);
                break;
            case 4://收藏
                String currentUrl = ((MainActivity)getActivity()).getMainFragment().getCurrentUrl();
                if(!TextUtils.isEmpty(currentUrl)) {
                    Bean bean = new Bean();
                    bean.name = ((MainActivity) getActivity()).getMainFragment().getFavName();
                    bean.url = currentUrl;
                    bean.time = System.currentTimeMillis();
                    boolean result = SqliteHelper.instance(getActivity().getApplicationContext()).insertFav(bean);
                    if(result) {
                        Toast.makeText(getActivity(), R.string.favorite_txt, Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).getSlidingMenu().toggle(false);
                    }
                }
                break;
            case 5:
                getActivity().finish();
                break;
        }
    }


}
