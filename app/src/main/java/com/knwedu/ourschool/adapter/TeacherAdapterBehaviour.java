package com.knwedu.ourschool.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.utils.DataStructureFramwork.Subject;
import com.knwedu.ourschool.utils.SchoolAppUtils;


public class TeacherAdapterBehaviour extends BaseAdapter {
	ViewHolder holder;
	private LayoutInflater inflater;
	Context context;
	private String date;
	private boolean showNextImg;
	private ArrayList<Subject> subjects;
	public TeacherAdapterBehaviour(Context context, ArrayList<Subject> subjects, boolean showNextImg)
	{
		this.context = context;
		this.subjects = subjects;
		this.showNextImg = showNextImg;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public int getCount() {
		if(this.subjects != null)
		{
			return this.subjects.size();
		}
		return 10;
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
			convertView = inflater.inflate(R.layout.teacher_item, null);
			holder = new ViewHolder();
			holder.courseDisplay = (LinearLayout) convertView.findViewById(R.id.title_layout);
			holder.title = (TextView) convertView.findViewById(R.id.course_txt);
			holder.description 	= (TextView)convertView.findViewById(R.id.description_txt);
			holder.img = (ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(holder);
		}
		else 
		{
			holder = (ViewHolder) convertView.getTag();
		}
		if(subjects.get(position).check)
		{
			holder.courseDisplay.setVisibility(View.VISIBLE);
			holder.title.setText(subjects.get(position).classs);
		}
		else
		{
			holder.courseDisplay.setVisibility(View.GONE);
		}
		if(showNextImg)
		{
			holder.img.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.img.setVisibility(View.VISIBLE);
		}
		SchoolAppUtils.setFont(context, holder.description);
		SchoolAppUtils.setFont(context, holder.title);
		/*if(subjects.get(position).is_practical.equals("1"))
		{
		     holder.description.setText(subjects.get(position).sub_name+"(Practical)");
		}
		else if(subjects.get(position).is_project.equals("1"))
		{
			 holder.description.setText(subjects.get(position).sub_name+"(Project)");
				
		}
		else
		{
			*/
		holder.description.setText(subjects.get(position).section_name);
		/*		
		}*/
		
		return convertView;
	}
	private class ViewHolder
	{
		LinearLayout courseDisplay;
		TextView description, title;
		ImageView img;
	}
}