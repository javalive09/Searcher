package peter.util.searcher.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.activity.SearchActivity;
import peter.util.searcher.bean.EnginesInfo;
import peter.util.searcher.bean.EnginesItem;
import peter.util.searcher.bean.ItemItem;
import peter.util.searcher.databinding.FragmentEngineGridItemBinding;
import peter.util.searcher.databinding.FragmentEngineViewpagerBinding;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.net.CommonRetrofit;
import peter.util.searcher.net.IEngineService;
import peter.util.searcher.utils.UrlUtils;

/**
 * 引擎列表fragment
 * Created by peter on 16/5/9.
 */
public class EngineInfoViewPagerFragment extends Fragment implements View.OnClickListener {

    FragmentEngineViewpagerBinding binding;

    private int getPageNo() {
        TabData bean = (TabData) getArguments().getSerializable(BaseActivity.NAME_TAB_DATA);
        if (bean != null) {
            return bean.getPageNo();
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_engine_viewpager, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        IEngineService iEngineService = CommonRetrofit.getInstance().getRetrofit().create(IEngineService.class);
        Observable<EnginesInfo> engineInfoObservable = iEngineService.getInfo();
        engineInfoObservable.subscribeOn(Schedulers.io())
                .retry(5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(engineInfo -> {
                    binding.loading.setVisibility(View.GONE);
                    EnginesAdapter adapter = new EnginesAdapter(EngineInfoViewPagerFragment.this, engineInfo.getEngines());
                    binding.viewpager.setAdapter(adapter);
                    binding.slidingTabs.setupWithViewPager(binding.viewpager);
                    binding.viewpager.setCurrentItem(getPageNo());
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.engine_item:
                BaseActivity act = (BaseActivity) getActivity();
                String searchWord = act.getSearchWord();
                if (!TextUtils.isEmpty(searchWord)) {
                    ItemItem engine = (ItemItem) v.getTag(R.id.grid_view_item);
                    String url = UrlUtils.smartUrlFilter(searchWord, true, engine.getUrl());
                    act.finish();
                    act.overridePendingTransition(0, 0);
                    TabData tabData = ((SearchActivity) getActivity()).getTabData();
                    tabData.setSearchWord(searchWord);
                    tabData.setUrl(url);
                    tabData.setPageNo(binding.viewpager.getCurrentItem());
                    act.startBrowser(tabData);
                }
                break;
            default:
                break;
        }
    }

    private static class EnginesAdapter extends PagerAdapter {
        final EngineInfoViewPagerFragment f;
        final List<EnginesItem> list;

        EnginesAdapter(EngineInfoViewPagerFragment f, List<EnginesItem> list) {
            this.list = list;
            this.f = f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position).getTitle();
        }


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = f.getActivity().getLayoutInflater().inflate(R.layout.fragment_engine_grid,
                    container, false);
            GridView gv = (GridView) view;
            EnginesItem engines = list.get(position);
            gv.setAdapter(new EngineAdapter(f, position, engines.getItem()));
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, Object o) {
            return o == view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    private static class EngineAdapter extends BaseAdapter {

        final EngineInfoViewPagerFragment f;
        final List<ItemItem> list;
        final int pageNo;

        EngineAdapter(EngineInfoViewPagerFragment f, int pageNo, List<ItemItem> list) {
            this.f = f;
            this.list = list;
            this.pageNo = pageNo;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public ItemItem getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            EngineHolder holder;
            if (convertView == null) {
                FragmentEngineGridItemBinding binding = DataBindingUtil.inflate(f.getActivity().getLayoutInflater(),
                        R.layout.fragment_engine_grid_item,
                        parent, false);
                holder = new EngineHolder(binding);
                convertView = binding.getRoot();
                convertView.setTag(holder);
            } else {
                holder = (EngineHolder) convertView.getTag();
            }

            ItemItem engine = getItem(position);
            engine.setPageNo(pageNo);

            RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE);
            Glide.with(f)
                    .load(engine.getIcon())
                    .apply(options)
                    .into(holder.binding.icon);
            holder.binding.title.setText(engine.getName());
            convertView.setOnClickListener(f);
            convertView.setTag(R.id.grid_view_item, engine);
            return convertView;
        }
    }

    static class EngineHolder {
        FragmentEngineGridItemBinding binding;
        EngineHolder(FragmentEngineGridItemBinding binding) {
            this.binding = binding;
        }
    }


}
