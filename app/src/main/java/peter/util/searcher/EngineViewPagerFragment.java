package peter.util.searcher;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import peter.util.searcher.net.GsonRequest;
import peter.util.searcher.net.RequestManager;

/**
 * Created by peter on 16/5/9.
 */
public class EngineViewPagerFragment extends Fragment implements View.OnClickListener {

    String url = "http://7xoxmg.com1.z0.glb.clouddn.com/engines.json";
    private SlidingTabLayout mSlidingTabLayout;
    private ViewPager mViewPager;
    View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_engine_viewpager, container, false);
        mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        mSlidingTabLayout = (SlidingTabLayout) rootView.findViewById(R.id.sliding_tabs);
        init();
        return rootView;
    }

    private void init() {
        Type collectionType = new TypeToken<ArrayList<TypeEngines<Engine>>>() {
        }.getType();
        RequestManager.addRequest(new GsonRequest<>(url, collectionType,
                responseListener(), errorListener()), getActivity());
    }

    private Response.Listener<ArrayList<TypeEngines<Engine>>> responseListener() {
        return new Response.Listener<ArrayList<TypeEngines<Engine>>>() {

            @Override
            public void onResponse(ArrayList<TypeEngines<Engine>> response) {
                View loading = rootView.findViewById(R.id.loading);
                if (loading != null) {
                    loading.setVisibility(View.GONE);
                }
                EnginesAdapter adapter = new EnginesAdapter(EngineViewPagerFragment.this, response);
                mViewPager.setAdapter(adapter);
                mSlidingTabLayout.setViewPager(mViewPager);
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.engine_item:
                Activity act = getActivity();
                String searchWord;
                SearchActivity searchAct = (SearchActivity) act;
                searchWord = searchAct.getSearchWord();
                if (!TextUtils.isEmpty(searchWord)) {
                    Engine engine = (Engine) v.getTag(R.id.grid_view_item);
                    String url = UrlUtils.smartUrlFilter(searchWord, true, engine.url);
                    searchAct.startBrowserFromSearch(getActivity(), url, searchWord);
                }
                break;
            default:
                break;
        }
    }

    private static class EnginesAdapter extends PagerAdapter {
        EngineViewPagerFragment f;
        List<TypeEngines<Engine>> list;

        public EnginesAdapter(EngineViewPagerFragment f, List<TypeEngines<Engine>> list) {
            this.list = list;
            this.f = f;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return list.get(position).title;
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
            TypeEngines engines = list.get(position);
            gv.setAdapter(new EngineAdapter(f, engines.item));
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

    protected Response.ErrorListener errorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private static class EngineAdapter extends BaseAdapter {

        EngineViewPagerFragment f;
        List<Engine> list;

        public EngineAdapter(EngineViewPagerFragment f, List<Engine> list) {
            this.f = f;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Engine getItem(int position) {
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
                holder = new EngineHolder();
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.icon = (NetworkImageView) convertView.findViewById(R.id.icon);
                convertView.setTag(holder);
            } else {
                holder = (EngineHolder) convertView.getTag();
            }

            Engine engine = getItem(position);
            ImageLoader imageLoader = RequestManager.getImageLoader();
            holder.icon.setImageUrl(engine.icon, imageLoader);
            holder.icon.setDefaultImageResId(R.drawable.searcher_icon);
            holder.title.setText(engine.name);
            convertView.setOnClickListener(f);
            convertView.setTag(R.id.grid_view_item, engine);
            return convertView;
        }
    }

    public static class EngineHolder {
        TextView title;
        NetworkImageView icon;
    }

    @Override
    public void onStop() {
        super.onStop();
        RequestManager.cancelAll(this);
    }

}
