package peter.util.searcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EngineAdapter extends BaseAdapter {

        private final LayoutInflater factory;
        MainActivity act;
        private int[] drawableRes;

        public EngineAdapter(int[] drawableRes, MainActivity act) {
            this.act = act;
            factory = LayoutInflater.from(act);
            this.drawableRes = drawableRes;
        }

        @Override
        public int getCount() {
            return drawableRes.length;
        }

        @Override
        public Integer getItem(int position) {
            return drawableRes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = factory.inflate(R.layout.engine_item, parent, false);
            }
            int resId = getItem(position);
            convertView.findViewById(R.id.icon).setBackgroundResource(resId);

            String name = act.getResources().getStringArray(R.array.engine_web_names)[position];
            TextView tv = (TextView) convertView.findViewById(R.id.name);
            tv.setText(name);
            convertView.setOnClickListener(act);
            convertView.setTag(position);
            return convertView;
        }

    }