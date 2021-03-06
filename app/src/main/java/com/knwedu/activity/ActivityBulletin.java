package com.knwedu.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.AdvertisementWebViewActivity;
import com.knwedu.ourschool.MainActivity;
import com.knwedu.ourschool.NewsActivity;
import com.knwedu.ourschool.ParentMainActivity;
import com.knwedu.ourschool.TeacherMainActivity;
import com.knwedu.ourschool.adapter.NewsAdapter;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork.News;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityBulletin extends AppCompatActivity {
    private Context mContext;
    private ListView list;
    private NewsAdapter adapter;
    private ProgressDialog dialog;
    private ArrayList<News> news;
    private Spinner spinner;
    private int position;
    private String type;
    private TextView textnodata;
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_news_list);
        mContext = ActivityBulletin.this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Bulletins");
        SchoolAppUtils.loadAppLogo(mContext, (ImageButton) findViewById(R.id.app_logo));
        initialize();
    }

    private void initialize() {
        // handleButton();
        list = (ListView) findViewById(R.id.listview);
        spinner = (Spinner) findViewById(R.id.spinner);
        textnodata = (TextView) findViewById(R.id.textnodata);


//		ImageView adImageView = (ImageView)view.findViewById(R.id.adImageView);
//		if(Integer.parseInt(SchoolAppUtils.GetSharedParameter(getActivity(),
//				Constants.USERTYPEID)) == 5){
//			adImageView.setVisibility(View.VISIBLE);
//
//		}else{
//			adImageView.setVisibility(View.GONE);
//		}

        //-----------------for insurance-----------------------
        ImageView adImageView = (ImageView) findViewById(R.id.adImageView);

        if (Integer.parseInt(SchoolAppUtils.GetSharedParameter(mContext,
                Constants.USERTYPEID)) == 5 && (SchoolAppUtils.GetSharedParameter(mContext, Constants.INSURANCE_AVIALABLE).equalsIgnoreCase("1"))) {
            adImageView.setVisibility(View.VISIBLE);

        } else {
            adImageView.setVisibility(View.GONE);
        }
        adImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AD_URL));
//				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				browserIntent.setPackage("com.android.chrome");
//				try {
//					startActivity(browserIntent);
//				} catch (ActivityNotFoundException ex) {
//					// Chrome browser presumably not installed so allow user to choose instead
//					browserIntent.setPackage(null);
//					startActivity(browserIntent);
//				}
                startActivity(new Intent(mContext,
                        AdvertisementWebViewActivity.class));
            }
        });


        if (news != null) {
            adapter = new NewsAdapter(mContext, news);
            list.setAdapter(adapter);
            list.setOnItemClickListener(clickListener);
        } else {

        }
        ArrayList<String> temp = new ArrayList<String>();
        type = SchoolAppUtils.GetSharedParameter(mContext, Constants.USERTYPEID);
        /*
         * if(type.equals("3")){
		 * 
		 * temp.add("School"); temp.add("Class");
		 * 
		 * } else {
		 */
        temp.add("School");
        temp.add("Class");
        if (!SchoolAppUtils.GetSharedParameter(mContext,
                Constants.APP_TYPE).equals(
                Constants.APP_TYPE_COMMON_STANDARD)) {
            temp.add("Me");
        }
        /* } */
        ArrayAdapter<String> data = new ArrayAdapter<String>(mContext, R.layout.simple_spinner_item_custom_new, temp);
        data.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_custom_new);

        spinner.setAdapter(data);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                loadData(arg2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        adImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.AD_URL));
//				browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				browserIntent.setPackage("com.android.chrome");
//				try {
//					startActivity(browserIntent);
//				} catch (ActivityNotFoundException ex) {
//					// Chrome browser presumably not installed so allow user to choose instead
//					browserIntent.setPackage(null);
//					startActivity(browserIntent);
//				}
                startActivity(new Intent(mContext,
                        AdvertisementWebViewActivity.class));
            }
        });
    }

    private void loadData(int arg2) {

        this.position = arg2;
        String url = null;
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
        if (arg2 == 0) {
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.UORGANIZATIONID)));
            nameValuePairs.add(new BasicNameValuePair("user_id", SchoolAppUtils
                    .GetSharedParameter(mContext, Constants.USERID)));
            if (SchoolAppUtils.GetSharedBoolParameter(mContext,
                    Constants.MONTHLYWEEKLYANNOUNCEMENT)) {
                nameValuePairs.add(new BasicNameValuePair("list_type", "1"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("list_type", "2"));

            }
            nameValuePairs.add(new BasicNameValuePair("type", "1"));
            nameValuePairs.add(new BasicNameValuePair("child_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.CHILD_ID)));
        } else if (arg2 == 1) {
            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.UORGANIZATIONID)));
            nameValuePairs.add(new BasicNameValuePair("user_id", SchoolAppUtils
                    .GetSharedParameter(mContext, Constants.USERID)));
            if (SchoolAppUtils.GetSharedBoolParameter(mContext,
                    Constants.MONTHLYWEEKLYANNOUNCEMENT)) {
                nameValuePairs.add(new BasicNameValuePair("list_type", "1"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("list_type", "2"));

            }
            nameValuePairs.add(new BasicNameValuePair("type", "2"));
            nameValuePairs.add(new BasicNameValuePair("child_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.CHILD_ID)));
        } else if (arg2 == 2) {

            nameValuePairs.add(new BasicNameValuePair("user_type_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.USERTYPEID)));
            nameValuePairs.add(new BasicNameValuePair("organization_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.UORGANIZATIONID)));
            nameValuePairs.add(new BasicNameValuePair("user_id", SchoolAppUtils
                    .GetSharedParameter(mContext, Constants.USERID)));
            if (SchoolAppUtils.GetSharedBoolParameter(mContext,
                    Constants.MONTHLYWEEKLYANNOUNCEMENT)) {
                nameValuePairs.add(new BasicNameValuePair("list_type", "1"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("list_type", "2"));

            }
            nameValuePairs.add(new BasicNameValuePair("type", "3"));
            nameValuePairs.add(new BasicNameValuePair("child_id",
                    SchoolAppUtils.GetSharedParameter(mContext,
                            Constants.CHILD_ID)));
        }

        new GetBulletinListAsyntask().execute(nameValuePairs);

    }

    OnItemClickListener clickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            Intent intent = new Intent(mContext, NewsActivity.class);
            intent.putExtra(Constants.NEWS, news.get(arg2).object.toString());
            intent.putExtra("type", Integer.toString(position));
            intent.putExtra(Constants.PAGE_TITLE, "Bulletin");
            startActivity(intent);
        }
    };

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressLint("NewApi")
    @Override
    public void onStop() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
        super.onStop();
    }

    private class GetBulletinListAsyntask extends
            AsyncTask<List<NameValuePair>, Void, Boolean> {
        String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mContext);
            dialog.setTitle("Bulletin");
            dialog.setMessage(getResources().getString(R.string.please_wait));
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(List<NameValuePair>... params) {
            List<NameValuePair> nameValuePairs = params[0];
            // Log parameters:
            Log.d("url extension: ", Urls.api_broadcast_viewbulletin);
            String parameters = "";
            for (NameValuePair nvp : nameValuePairs) {
                parameters += nvp.getName() + "=" + nvp.getValue() + ",";
            }
            Log.d("Parameters: ", parameters);

            news = new ArrayList<News>();
            JsonParser jParser = new JsonParser();
            JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs,
                    Urls.api_broadcast_viewbulletin);
            try {

                if (json != null) {
                    if (json.getString("result").equalsIgnoreCase("1")) {

                        JSONArray array = json.getJSONArray("data");
                        news = new ArrayList<News>();
                        for (int i = 0; i < array.length(); i++) {
                            News newD = new News(array.getJSONObject(i));
                            news.add(newD);
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
                if (news != null) {
                    list.setVisibility(View.VISIBLE);
                    textnodata.setVisibility(View.GONE);
                    adapter = new NewsAdapter(mContext, news);
                    list.setAdapter(adapter);
                    list.setOnItemClickListener(clickListener);
                }
            } else {

                if (error.length() > 0) {
                    //SchoolAppUtils.showDialog(mContext, mContext.getTitle().toString(), error);
                    list.setAdapter(null);
                    list.setVisibility(View.GONE);
                    textnodata.setVisibility(View.VISIBLE);
                    textnodata.setText("No Bulletin Created yet");
                } else {
                    SchoolAppUtils.showDialog(mContext, "Bulletin", getResources().getString(R.string.error));
                }

            }
        }

    }

    private void handleButton() {

        if (MainActivity.showMonthWeek != null) {
            MainActivity.showMonthWeek
                    .setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (SchoolAppUtils.GetSharedBoolParameter(
                                    mContext,
                                    Constants.MONTHLYWEEKLYBULLETIN)) {
                                changeStateButton(false);
                                loadData(position);
                            } else {
                                changeStateButton(true);
                                loadData(position);
                            }
                        }
                    });
        }

        if (TeacherMainActivity.showMonthWeek != null) {
            TeacherMainActivity.showMonthWeek
                    .setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (SchoolAppUtils.GetSharedBoolParameter(
                                    mContext,
                                    Constants.MONTHLYWEEKLYBULLETIN)) {
                                changeStateButton(false);
                                loadData(position);
                            } else {
                                changeStateButton(true);
                                loadData(position);
                            }
                        }
                    });
        }

        if (ParentMainActivity.showMonthWeek != null) {
            ParentMainActivity.showMonthWeek
                    .setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (SchoolAppUtils.GetSharedBoolParameter(
                                    mContext,
                                    Constants.MONTHLYWEEKLYBULLETIN)) {
                                changeStateButton(false);
                                loadData(position);

                            } else {
                                changeStateButton(true);
                                loadData(position);
                            }
                        }
                    });
        }
    }

    private void changeStateButton(boolean state) {
        SchoolAppUtils.SetSharedBoolParameter(mContext,
                Constants.MONTHLYWEEKLYBULLETIN, state);
        if (MainActivity.showMonthWeek != null) {
            if (state) {
                MainActivity.showMonthWeek.setText(R.string.weekly);
            } else {
                MainActivity.showMonthWeek.setText(R.string.monthly);
            }
        }
        if (TeacherMainActivity.showMonthWeek != null) {
            if (state) {
                TeacherMainActivity.showMonthWeek.setText(R.string.weekly);
            } else {
                TeacherMainActivity.showMonthWeek.setText(R.string.monthly);
            }
        }
        if (ParentMainActivity.showMonthWeek != null) {
            if (state) {
                ParentMainActivity.showMonthWeek.setText(R.string.weekly);
            } else {
                ParentMainActivity.showMonthWeek.setText(R.string.monthly);
            }
        }
    }
}
