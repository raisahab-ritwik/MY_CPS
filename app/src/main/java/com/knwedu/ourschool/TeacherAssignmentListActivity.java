package com.knwedu.ourschool;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.adapter.TeacherAssignmentsAdapter;
import com.knwedu.ourschool.db.DatabaseAdapter;
import com.knwedu.ourschool.db.SchoolApplication;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork.Assignment;
import com.knwedu.ourschool.utils.DataStructureFramwork.Subject;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;

public class TeacherAssignmentListActivity extends AppCompatActivity {
    private ArrayList<Assignment> assignments;
    private ProgressDialog dialog;
    private ListView listView;
    private TeacherAssignmentsAdapter adapter;
    public DatabaseAdapter mDatabase;
    ImageButton addAssignment;
    private Subject subject;
    private Button monthlyWeekly;
    private TextView textEmpty;
    private boolean internetAvailable = false;
    private String page_title = "";

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = ((SchoolApplication) getApplication()).getDatabase();
        setContentView(R.layout.activity_teacher_assignment_list);
        page_title = getIntent().getStringExtra(Constants.PAGE_TITLE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(""+page_title);

        SchoolAppUtils.loadAppLogo(TeacherAssignmentListActivity.this,
                (ImageButton) findViewById(R.id.app_logo));

        TextView header = (TextView) findViewById(R.id.header_text);
        header.setText(page_title);
        addAssignment = (ImageButton) findViewById(R.id.add_assignment);
        if ((SchoolAppUtils.GetSharedParameter(
                TeacherAssignmentListActivity.this,
                Constants.ASSIGNMENT_ADD_PERMISSION)).equalsIgnoreCase("0")) {
            addAssignment.setVisibility(View.INVISIBLE);
        }
        monthlyWeekly = (Button) findViewById(R.id.monthly_weekly_btn);
        textEmpty = (TextView) findViewById(R.id.textEmpty);

        listView = (ListView) findViewById(R.id.listview);
        assignments = new ArrayList<Assignment>();
        internetAvailable = getIntent().getBooleanExtra(Constants.IS_ONLINE,
                false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (internetAvailable) {
            initialize();
        } else {
            findViewById(R.id.txt_offline).setVisibility(View.VISIBLE);
            monthlyWeekly.setVisibility(View.GONE);
            new OfflineSubjectListenerAsync().execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Google Analytics", "Tracking Start");
        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Google Analytics", "Tracking Stop");
        EasyTracker.getInstance(this).activityStop(this);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    private void initialize() {

        handleMonthlyWeekly();

        if (getIntent().getExtras() != null) {

            String temp = getIntent().getExtras().getString(
                    Constants.TASSIGNMENT);
            if (temp != null) {
                JSONObject object = null;
                try {
                    object = new JSONObject(temp);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (object != null) {
                    subject = new Subject(object);

                }

            }
            if (subject != null) {
                loadData();
            }
            addAssignment.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(TeacherAssignmentListActivity.this, TeacherAssignmentAddActivity.class);
                    intent.putExtra(Constants.IS_ONLINE, internetAvailable);
                    intent.putExtra(Constants.TSUBJECT, subject.object.toString());
                    intent.putExtra(Constants.PAGE_TITLE, page_title);
                    startActivity(intent);
                }
            });

        }
    }

    private void loadData() {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
        nameValuePairs.add(new BasicNameValuePair("user_type_id",
                SchoolAppUtils.GetSharedParameter(getApplicationContext(),
                        Constants.USERTYPEID)));
        nameValuePairs.add(new BasicNameValuePair("organization_id",
                SchoolAppUtils.GetSharedParameter(getApplicationContext(),
                        Constants.UORGANIZATIONID)));
        nameValuePairs
                .add(new BasicNameValuePair("user_id", SchoolAppUtils
                        .GetSharedParameter(getApplicationContext(),
                                Constants.USERID)));
        if (monthlyWeekly.getText().toString()
                .equals(getString(R.string.weekly))) {
            nameValuePairs.add(new BasicNameValuePair("list_type", "1"));
        } else if (monthlyWeekly.getText().toString()
                .equals(getString(R.string.monthly))) {

            nameValuePairs.add(new BasicNameValuePair("list_type", "2"));
        } else {
            nameValuePairs.add(new BasicNameValuePair("list_type", "3"));
        }
        nameValuePairs.add(new BasicNameValuePair("type", "1"));
        nameValuePairs.add(new BasicNameValuePair("sub_id", subject.id));
        //nameValuePairs
        //.add(new BasicNameValuePair("class_id", subject.class_id));

        nameValuePairs.add(new BasicNameValuePair("section_id", subject.section_id));

        new GetAssignmentsAsyntask().execute(nameValuePairs);

    }

    private void handleMonthlyWeekly() {

        monthlyWeekly.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String myStr = monthlyWeekly.getText().toString();
                if ((myStr.equals(getString(R.string.all)))) {
                    monthlyWeekly.setText(getString(R.string.weekly));
                } else if ((myStr.equals(getString(R.string.weekly)))) {
                    monthlyWeekly.setText(getString(R.string.monthly));
                } else if ((myStr.equals(getString(R.string.monthly)))) {
                    monthlyWeekly.setText(getString(R.string.all));
                }
                loadData();

            }
        });
    }

    OnItemClickListener itemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            Intent intent = new Intent(TeacherAssignmentListActivity.this, TeacherAssignmentDetailActivity.class);
            intent.putExtra(Constants.IS_ONLINE, internetAvailable);
            intent.putExtra(Constants.PAGE_TITLE, page_title);
            if (internetAvailable) {
                intent.putExtra(Constants.ASSIGNMENT, assignments.get(arg2).object.toString());
                intent.putExtra(Constants.SUBJECT, subject.object.toString());
            } else {

                intent.putExtra(Constants.OFFLINE_SUBJECT_ID, subject.id);
                intent.putExtra(Constants.OFFLINE_ASSIGNMENT_ROWID, assignments.get(arg2).row_id);
            }
            startActivity(intent);
        }
    };

    private class GetAssignmentsAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(TeacherAssignmentListActivity.this);
            dialog.setTitle(page_title);
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            assignments.clear();
            List<NameValuePair> namevaluepair = params[0];
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(namevaluepair,
                    Urls.api_activity_get);
            // Log parameters:
            Log.d("url extension", Urls.api_activity_get);
            String parameters = "";
            for (NameValuePair nvp : namevaluepair) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        JSONArray array;
                        try {
                            array = json.getJSONArray("data");
                        } catch (Exception e) {
                            error = "Error in Data";
                            return false;
                        }

                        for (int i = 0; i < array.length(); i++) {
                            Assignment assignment = new Assignment(
                                    array.getJSONObject(i));
                            assignments.add(assignment);
                        }
                        return true;
                    } else {
                        try {
                            error = json.getString("data");
                        } catch (Exception e) {

                        }
                        return false;
                    }
                } else {
                    error = getResources().getString(R.string.unknown_response);
                }

            } catch (JSONException e) {
                error = "Connection Failure";
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            super.onPostExecute(result);
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            if (result) {
                if (assignments != null) {
                    String temp = null;
                    for (int i = 0; i < assignments.size(); i++) {
                        assignments.get(i).sub_name = subject.sub_name;
                        if (i == 0) {
                            assignments.get(0).check = true;
                            temp = assignments.get(0).submit_date;
                        } else {
                            if (!temp.equalsIgnoreCase(assignments.get(i).submit_date)) {
                                assignments.get(i).check = true;
                                temp = assignments.get(i).submit_date;
                            }
                        }
                    }
                    if (assignments.size() > 0) {
                        textEmpty.setVisibility(View.GONE);
                        adapter = new TeacherAssignmentsAdapter(TeacherAssignmentListActivity.this,
                                assignments, subject, 1, true,
                                SchoolAppUtils.GetSharedParameter(TeacherAssignmentListActivity.this,
                                        Constants.ASSIGNMENT_MARK_PERMISSION),
                                page_title);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        //listView.setOnItemClickListener(itemClickListener);
                    } else {
                        textEmpty.setText("No data available");
                        textEmpty.setVisibility(View.VISIBLE);
                    }

                }
            } else {
                textEmpty.setText(error.toString());
                textEmpty.setVisibility(View.VISIBLE);
            }
        }

    }

    class OfflineSubjectListenerAsync extends AsyncTask<String, Subject, Void> {

        protected void onPreExecute() {
            super.onPreExecute();
            assignments.clear();
        }

        protected Void doInBackground(String... params) {
            final String subject_id = getIntent().getExtras().getString(
                    Constants.OFFLINE_SUBJECT_ID);
            mDatabase.open();
            subject = mDatabase.getSubject(subject_id);
            assignments = mDatabase.getAllAssignments(1, subject_id);
            mDatabase.close();

            addAssignment.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(
                            TeacherAssignmentListActivity.this,
                            TeacherAssignmentAddActivity.class);
                    intent.putExtra(Constants.IS_ONLINE, internetAvailable);
                    intent.putExtra(Constants.OFFLINE_SUBJECT_ID, subject_id);
                    intent.putExtra(Constants.PAGE_TITLE, page_title);
                    startActivity(intent);
                }
            });

            return null;
        }

        @Override
        protected void onProgressUpdate(Subject... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (assignments != null) {
                String temp = null;
                for (int i = 0; i < assignments.size(); i++) {
                    assignments.get(i).sub_name = subject.sub_name;
                    if (i == 0) {
                        assignments.get(0).check = true;
                        temp = assignments.get(0).submit_date;
                    } else {
                        if (!temp
                                .equalsIgnoreCase(assignments.get(i).submit_date)) {
                            assignments.get(i).check = true;
                            temp = assignments.get(i).submit_date;
                        }
                    }
                }
                if (assignments.size() > 0) {
                    textEmpty.setVisibility(View.GONE);
                    adapter = new TeacherAssignmentsAdapter(
                            TeacherAssignmentListActivity.this, assignments,
                            subject, 1, false,
                            SchoolAppUtils.GetSharedParameter(
                                    TeacherAssignmentListActivity.this,
                                    Constants.ASSIGNMENT_MARK_PERMISSION),
                            page_title);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(itemClickListener);
                } else {
                    textEmpty.setText("No data available in Offline mode");
                    textEmpty.setVisibility(View.VISIBLE);
                }

            }
        }

    }

}
