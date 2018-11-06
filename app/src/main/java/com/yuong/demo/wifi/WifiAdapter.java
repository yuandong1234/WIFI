package com.yuong.demo.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yuong.demo.R;

import java.util.List;

/**
 * WIFI 热点适配器
 * Created by yuandong on 2018/10/19.
 */

public class WifiAdapter extends BaseAdapter {

    private Context mContext;
    private List<ScanResult> list;
    private LayoutInflater inflater;

    public WifiAdapter(Context mContext, List<ScanResult> list) {
        this.mContext = mContext;
        this.list = list;
        this.inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        if (list != null && list.size() > 0) {
            return list.size();
        }
        return 0;
    }

    public void setData(List<ScanResult> data) {
        list.clear();
        if (data != null && data.size() > 0) {
            list.addAll(data);
            notifyDataSetChanged();
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_wifi, null);
            vh.title = convertView.findViewById(R.id.title);
            vh.level = convertView.findViewById(R.id.level);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        ScanResult bean = list.get(position);
        vh.title.setText(bean.SSID);

        //计算信号等级
        int level = WifiManager.calculateSignalLevel(bean.level, 4);
        //是否加密
        boolean isEncrypted = WifiUtil.isEncrypted(bean.capabilities);
        switch (level) {
            case 0:
                if(isEncrypted){
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_locked_signal_level_0));
                }else{
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_level_0));
                }
                break;
            case 1:
                if(isEncrypted){
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_locked_signal_level_1));
                }else{
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_level_1));
                }

                break;
            case 2:
                if(isEncrypted){
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_locked_signal_level_2));
                }else{
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_level_2));
                }

                break;
            case 3:
                if(isEncrypted){
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_locked_signal_level_3));
                }else{
                    vh.level.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_wifi_signal_level_3));
                }
                break;
        }


        return convertView;
    }

    private class ViewHolder {
        TextView title;
        ImageView level;
    }
}
