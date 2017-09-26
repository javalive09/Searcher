package peter.util.searcher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.R;
import peter.util.searcher.activity.BaseActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.bean.EnginesInfo;
import peter.util.searcher.bean.EnginesItem;
import peter.util.searcher.bean.ItemItem;
import peter.util.searcher.net.CommonRetrofit;
import peter.util.searcher.net.IEngineService;
import peter.util.searcher.utils.UrlUtils;

/**
 * 引擎列表fragment
 * Created by peter on 16/5/9.
 */
public class EngineInfoViewPagerFragment extends Fragment implements View.OnClickListener {

    @BindView(R.id.sliding_tabs)
    TabLayout mSlidingTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;
    @BindView(R.id.loading)
    View loading;

    private int getPageNo() {
        Bean bean = getArguments().getParcelable(BaseActivity.NAME_BEAN);
        if (bean != null) {
            return bean.pageNo;
        }
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_engine_viewpager, container, false);
        ButterKnife.bind(EngineInfoViewPagerFragment.this, rootView);
        init();
        return rootView;
    }

    private void init() {
        IEngineService iEngineService = CommonRetrofit.getInstance().getRetrofit().create(IEngineService.class);
        Observable<EnginesInfo> engineInfoObservable = iEngineService.getInfo();
        engineInfoObservable.subscribeOn(Schedulers.io())
                .retry(5)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(engineInfo -> {
                    loading.setVisibility(View.GONE);
                    EnginesAdapter adapter = new EnginesAdapter(EngineInfoViewPagerFragment.this, engineInfo.getEngines());
                    mViewPager.setAdapter(adapter);
                    mSlidingTabLayout.setupWithViewPager(mViewPager);
                    mViewPager.setCurrentItem(getPageNo());
                }, throwable -> {
                    Toast.makeText(getActivity(), throwable.getMessage(), Toast.LENGTH_LONG).show();
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
                    Bean bean = new Bean(searchWord, url);
                    bean.pageNo = mViewPager.getCurrentItem();
                    act.startBrowser(bean);
                }
                break;
            default:
                break;
        }
    }

    private static class EnginesAdapter extends PagerAdapter {
        EngineInfoViewPagerFragment f;
        List<EnginesItem> list;

        public EnginesAdapter(EngineInfoViewPagerFragment f, List<EnginesItem> list) {
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
        public Object instantiateItem(ViewGroup container, int position) {
            View view = f.getActivity().getLayoutInflater().inflate(R.layout.fragment_engine_grid,
                    container, false);
            GridView gv = (GridView) view;
            EnginesItem engines = list.get(position);
            gv.setAdapter(new EngineAdapter(f, position, engines.getItem()));
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }

    private static class EngineAdapter extends BaseAdapter {

        EngineInfoViewPagerFragment f;
        List<ItemItem> list;
        int pageNo;

        public EngineAdapter(EngineInfoViewPagerFragment f, int pageNo, List<ItemItem> list) {
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
                convertView = f.getActivity().getLayoutInflater().inflate(R.layout.fragment_engine_grid_item,
                        parent, false);
                holder = new EngineHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (EngineHolder) convertView.getTag();
            }

            ItemItem engine = getItem(position);
            engine.setPageNo(pageNo);
            Glide.with(f)
                    .load(engine.getIcon())
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.icon);
            holder.title.setText(engine.getName());
            convertView.setOnClickListener(f);
            convertView.setTag(R.id.grid_view_item, engine);
            return convertView;
        }
    }

    public static class EngineHolder {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.icon)
        ImageView icon;

        public EngineHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
