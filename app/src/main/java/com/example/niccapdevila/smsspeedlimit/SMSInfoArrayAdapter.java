package com.example.niccapdevila.smsspeedlimit;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

/**
 * Created by niccapdevila on 4/7/15.
 */
public class SMSInfoArrayAdapter extends ArrayAdapter<SMSInfo> {

    private final Context mContext;
    private final int mLayoutResourceID;
    private List<SMSInfo> mSMSInfos = null;


    public SMSInfoArrayAdapter(Context context, List<SMSInfo> SMSInfos) {
        super(context, R.layout.activity_main, SMSInfos);
        this.mContext = context;
        this.mLayoutResourceID = R.layout.sms_list_view_item;
        this.mSMSInfos = SMSInfos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;


        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceID, parent, false);
            holder = new ViewHolder();
            holder.tvAddress = (TextView) convertView.findViewById(R.id.textViewAddress);
            holder.tvDate = (TextView) convertView.findViewById(R.id.textViewDate);
            holder.tvSpeed = (TextView) convertView.findViewById(R.id.textViewSpeed);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // object item based on the position
        SMSInfo smsInfo = mSMSInfos.get(position);

        // get the TextView and then set the text (item name) and tag (item ID) values

        holder.tvAddress.setText(smsInfo.getAddress());
        holder.tvAddress.setTag(position);

        String sDate = new Date( Long.parseLong(smsInfo.getDate())).toString();
        holder.tvDate.setText(sDate);
        holder.tvDate.setTag(position);

        holder.tvSpeed.setTag(position);
        holder.tvSpeed.setText("");

        if(smsInfo.getSpeed()!=null) {
            holder.tvSpeed.setText(smsInfo.getSpeed() + "MPH");
            if (Float.parseFloat(smsInfo.getSpeed()) < 15f)
                holder.tvSpeed.setTextColor(Color.argb(255,0,100,0));


        }

       return convertView;

    }

    static class ViewHolder{
        TextView tvDate;
        TextView tvAddress;
        TextView tvSpeed;
    }
}
