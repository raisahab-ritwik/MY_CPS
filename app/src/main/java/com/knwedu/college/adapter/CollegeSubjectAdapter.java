package com.knwedu.college.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knwedu.college.utils.CollegeAppUtils;
import com.knwedu.college.utils.CollegeDataStructureFramwork.SubjectList;
import com.knwedu.calcuttapublicschool.R;

public class CollegeSubjectAdapter extends BaseAdapter {
	ViewHolder holder;
	private LayoutInflater inflater;
	Context context;
	private String date;
	private boolean showNextImg;
	private ArrayList<SubjectList> subjects;

	public CollegeSubjectAdapter(Context context, ArrayList<SubjectList> subjects,
			boolean showNextImg) {
		this.context = context;
		this.subjects = subjects;
		this.showNextImg = showNextImg;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {

		return subjects.size();
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
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.college_teacher_item, null);
			holder = new ViewHolder();
			holder.courseDisplay = (LinearLayout) convertView
					.findViewById(R.id.title_layout);
			holder.title = (TextView) convertView.findViewById(R.id.course_txt);
			holder.description = (TextView) convertView
					.findViewById(R.id.description_txt);
			holder.img = (ImageView) convertView.findViewById(R.id.img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (subjects.get(position).check) {
			holder.courseDisplay.setVisibility(View.VISIBLE);
			holder.title.setText(subjects.get(position).program_name + " "
					+ subjects.get(position).term_name);
		} else {
			holder.courseDisplay.setVisibility(View.GONE);
		}
		if (showNextImg) {
			holder.img.setVisibility(View.VISIBLE);
		} else {
			holder.img.setVisibility(View.VISIBLE);
		}
		CollegeAppUtils.setFont(context, holder.description);
		CollegeAppUtils.setFont(context, holder.title);
		if (subjects.get(position).subject_type.equals("0")) {
			holder.description.setText(subjects.get(position).subject_name +
					 ""+""+subjects.get(position).group_name);
		} else if (subjects.get(position).subject_type.equals("1")) {
			holder.description.setText(subjects.get(position).subject_name
					+ "(Project)" + " " +""+subjects.get(position).group_name);
		} else if (subjects.get(position).subject_type.equals("2")) {
			holder.description.setText(subjects.get(position).subject_name
					+ "(Practical)" + " " +""+subjects.get(position).group_name);
		}return convertView;
	}

	private class ViewHolder {
		LinearLayout courseDisplay;
		TextView description, title;
		ImageView img;
	}

}
