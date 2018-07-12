package com.wecare.app.module.setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wecare.app.R;
import com.wecare.app.data.entity.DeviceBean;
import com.wecare.app.module.BaseActivity;
import com.wecare.app.util.DeviceUtils;
import com.wecare.app.util.SystemUtils;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseActivity {
    private ListView listView;

    private List<DeviceBean> mDataList = new ArrayList<>();

    private TextView titleTV;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        listView = findViewById(R.id.list_view);
        findViewById(R.id.back_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.right_layout).setVisibility(View.GONE);
        findViewById(R.id.back_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        titleTV = findViewById(R.id.title);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        titleTV.setText("设备信息");
        mDataList.clear();
        mDataList.add(new DeviceBean("IMEI", DeviceUtils.getDeviceIMEI(this)));
        mDataList.add(new DeviceBean("型号", SystemUtils.getSystemModel()));
        mDataList.add(new DeviceBean("运行内存 ", DeviceUtils.getSysteTotalMemorySize(this)));
        mDataList.add(new DeviceBean("系统容量", "总容量:" + DeviceUtils.getTotalInternalStorgeSize() + "-可用:" + DeviceUtils.getAvailableInternalStorgeSize()));
        mDataList.add(new DeviceBean("Android版本", SystemUtils.getAndroidVersion()));
        mDataList.add(new DeviceBean("版本号", SystemUtils.getSystemVersion()));
        mDataList.add(new DeviceBean("序列号", SystemUtils.getSerialNumber()));
        mDataList.add(new DeviceBean("ICCID", SystemUtils.getICCID(this)));
        SettingAdapter adapter = new SettingAdapter();
        listView.setAdapter(adapter);
    }

    class SettingAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(SettingActivity.this).inflate(R.layout.item_setting, null, false);
                holder.titleTv = convertView.findViewById(R.id.title_tv);
                holder.contentTv = convertView.findViewById(R.id.content_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (!mDataList.isEmpty()) {
                DeviceBean bean = mDataList.get(position);
                holder.titleTv.setText(bean.getName());
                holder.contentTv.setText(bean.getValue());
            }

            return convertView;
        }

        class ViewHolder {
            TextView titleTv, contentTv;
        }
    }
}
