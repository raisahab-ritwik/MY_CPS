package com.knwedu.college.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.knwedu.college.utils.CollegeAppUtils;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.college.utils.CollegeDataStructureFramwork.PaymentReminder;

import java.util.ArrayList;


public class PaymentReminderAdapter extends BaseAdapter {
	ViewHolder holder;
	private LayoutInflater inflater;
	Context context;
	private ArrayList<PaymentReminder> paymentReminders;
	public PaymentReminderAdapter(Context context, ArrayList<PaymentReminder> paymentReminders)
	{
		this.context = context;
		this.paymentReminders = paymentReminders;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		if(this.paymentReminders != null)
		{
			return this.paymentReminders.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) 
		{
			convertView = inflater.inflate(R.layout.fees_reminder_list_view, null);
			holder = new ViewHolder();
			holder.reminderDate 	= (TextView)convertView.findViewById(R.id.txtRemimder);
			holder.reminderDueDate 	= (TextView)convertView.findViewById(R.id.txtDueDate);
			holder.reminderGraceDate 	= (TextView)convertView.findViewById(R.id.txtGraceDate);
			holder.reminderMessage 	= (TextView)convertView.findViewById(R.id.txtReminderMessage);
			convertView.setTag(holder);
		}
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}
		CollegeAppUtils.setFont(context, holder.reminderDate);
		CollegeAppUtils.setFont(context,holder.reminderDueDate);
		CollegeAppUtils.setFont(context, holder.reminderGraceDate);
		CollegeAppUtils.setFont(context, holder.reminderMessage );
		
		holder.reminderDate.setText(paymentReminders.get(position).reminder_send_date);
		holder.reminderDueDate.setText(paymentReminders.get(position).payment_date);
		holder.reminderGraceDate.setText(paymentReminders.get(position).fine_grace_date);
		holder.reminderMessage.setText(paymentReminders.get(position).fees_reminder_description);
		
		return convertView;
	}
	private class ViewHolder
	{
		TextView reminderDate, reminderDueDate, reminderGraceDate, reminderMessage;
	}
}