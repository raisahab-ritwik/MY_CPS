package com.knwedu.ourschool;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.adapter.RequestStatusAdapter;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork.RequestsStatus;
import com.knwedu.ourschool.utils.DataStructureFramwork.StudentProfileInfo;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.LoadImageAsyncTask;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;

public class ParentRequestStatusListActivity extends AppCompatActivity {
    private ArrayList<RequestsStatus> requests;
    private ProgressDialog dialog;
    private Spinner spinner;
    private ListView listView;
    private RequestStatusAdapter adapter;
    private static ImageView image;
    private static TextView name;
    private TextView textnodata;
    private static TextView classs;

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // private static ImageLoader imageTemp = ImageLoader.getInstance();
    // private static DisplayImageOptions options;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);
        SchoolAppUtils.loadAppLogo(ParentRequestStatusListActivity.this,
                (ImageButton) findViewById(R.id.app_logo));

        image = (ImageView) findViewById(R.id.image_view);
        name = (TextView) findViewById(R.id.name_txt);
        classs = (TextView) findViewById(R.id.class_txt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Request Status");
        setUserImage(ParentRequestStatusListActivity.this);
        initialize();
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

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayList<String> request = new ArrayList<String>();
        request.add("All Requests");
        if (!SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this, Constants.LEAVE_REQUEST)
                .toString().equalsIgnoreCase("0")) {
            request.add(SchoolAppUtils.GetSharedParameter(ParentRequestStatusListActivity.this,
                    Constants.LEAVE_REQUEST).toString());
        }
        if (!SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this,
                        Constants.REQUEST_FOR_ID_CARD).toString()
                .equalsIgnoreCase("0")) {
            request.add(SchoolAppUtils.GetSharedParameter(ParentRequestStatusListActivity.this,
                    Constants.REQUEST_FOR_ID_CARD).toString());
        }
        if (!SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this,
                        Constants.REQUEST_FOR_UNIFORM).toString()
                .equalsIgnoreCase("0")) {
            request.add(SchoolAppUtils.GetSharedParameter(ParentRequestStatusListActivity.this,
                    Constants.REQUEST_FOR_UNIFORM).toString());
        }
        if (!SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this, Constants.REQUEST_FOR_BOOK)
                .toString().equalsIgnoreCase("0")) {
            request.add(SchoolAppUtils.GetSharedParameter(ParentRequestStatusListActivity.this,
                    Constants.REQUEST_FOR_BOOK).toString());
        }
        if (!SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this, Constants.SPECIAL_REQUEST)
                .toString().equalsIgnoreCase("0")) {
            request.add(SchoolAppUtils.GetSharedParameter(ParentRequestStatusListActivity.this,
                    Constants.SPECIAL_REQUEST).toString());
        }
        /*String[] temppp = getResources().getStringArray(
				R.array.request_type_list);*/
        ArrayAdapter<String> data = new ArrayAdapter<String>(
                ParentRequestStatusListActivity.this,
                R.layout.simple_spinner_item_custom_new, request);
        data.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom_new);
        spinner.setAdapter(data);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                System.out.print("Position------" + arg2);
                loadData(arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        textnodata = (TextView) findViewById(R.id.textnodata);
        listView = (ListView) findViewById(R.id.listview);
        ((TextView) findViewById(R.id.header_text)).setVisibility(View.GONE);

    }

    private void loadData(int type) {

        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
        nameValuePairs.add(new BasicNameValuePair("user_id", SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this,
                        Constants.USERID)));
        nameValuePairs.add(new BasicNameValuePair("user_type_id",
                SchoolAppUtils.GetSharedParameter(
                        ParentRequestStatusListActivity.this,
                        Constants.USERTYPEID)));
        nameValuePairs.add(new BasicNameValuePair("organization_id",
                SchoolAppUtils.GetSharedParameter(
                        ParentRequestStatusListActivity.this,
                        Constants.UORGANIZATIONID)));
        nameValuePairs.add(new BasicNameValuePair("child_id", SchoolAppUtils
                .GetSharedParameter(ParentRequestStatusListActivity.this,
                        Constants.CHILD_ID)));
        nameValuePairs
                .add(new BasicNameValuePair("type", String.valueOf(type)));

        new GetRequestsAsyntask().execute(nameValuePairs);
    }

    private void setUserImage(Context context) {
        JSONObject c = null;
        try {
            c = new JSONObject(SchoolAppUtils.GetSharedParameter(context,
                    Constants.SELECTED_CHILD_OBJECT));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (c != null) {
            StudentProfileInfo info = new StudentProfileInfo(c);
            name.setText(info.fullname);
            classs.setText("Class: " + info.class_section);

            new LoadImageAsyncTask(context, image, Urls.image_url_userpic,
                    info.image, true).execute();

        }
    }

    private class GetRequestsAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(ParentRequestStatusListActivity.this);
            dialog.setTitle("Loading "
                    + getResources().getString(R.string.requests_status));
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            List<NameValuePair> nameValuePairs = params[0];

            // Log parameters:
            Log.d("url extension", Urls.api_parent_request_status);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs,
                    Urls.api_parent_request_status);

            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {
                        JSONArray array = json.getJSONArray("data");
                        requests = new ArrayList<RequestsStatus>();
                        for (int i = 0; i < array.length(); i++) {
                            RequestsStatus request = new RequestsStatus(
                                    array.getJSONObject(i));
                            requests.add(request);
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
                if (requests != null) {
                    adapter = new RequestStatusAdapter(
                            ParentRequestStatusListActivity.this, requests);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            Intent intent = new Intent(
                                    ParentRequestStatusListActivity.this, ParentRequestStatusActivity.class);
                            intent.putExtra(Constants.REQUESTSTATUS,
                                    requests.get(arg2).object.toString());
                            startActivity(intent);
                        }
                    });
                }
            } else {
                if (error.length() > 0) {
                    SchoolAppUtils.showDialog(
                            ParentRequestStatusListActivity.this,
                            getResources().getString(R.string.requests_status),
                            error);
                    textnodata.setVisibility(View.VISIBLE);
                    textnodata.setText("No request created yet");
                } else {
                    SchoolAppUtils.showDialog(
                            ParentRequestStatusListActivity.this,
                            getResources().getString(R.string.requests_status),
                            getResources().getString(R.string.unknown_response));
                }
            }
        }

    }
}
