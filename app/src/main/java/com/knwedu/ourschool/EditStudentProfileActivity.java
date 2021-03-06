package com.knwedu.ourschool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.DataStructureFramwork.StudentProfileInfo;
import com.knwedu.ourschool.utils.InnerImageLoader;
import com.knwedu.ourschool.utils.JsonParser;
import com.knwedu.ourschool.utils.LoadImageAsyncTask;
import com.knwedu.ourschool.utils.SchoolAppUtils;
import com.knwedu.ourschool.utils.Urls;


public class EditStudentProfileActivity extends FragmentActivity {
	private EditText firstName, lastName, phone, address, religion, bloodGroup, ailment, email, middlename, pincode;
	private ImageView img;
	private StudentProfileInfo userInfo;
	private Button submite;
	private ProgressDialog dialog;
	AlertDialog.Builder dialoggg;
	private static final int SELECT_PICTURE = 11412;
	private static final int SELECT_CAMERA = 1337;
	private InnerImageLoader innerImageLoader;
	String fnameS = null, lnameS = null, phoneS = null, addressS = null
			, religionS = null, bloodGroupS = null, diseaseS = null, emailS = null;;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_profile_student);
		initialize();
		initializeData();
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
	}
	private void initialize() {
		((ImageButton)findViewById(R.id.back_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		submite = (Button) findViewById(R.id.confirm_btn);
		firstName = (EditText) findViewById(R.id.first_name_edt);
		middlename = (EditText) findViewById(R.id.middle_name_edt);
		lastName = (EditText) findViewById(R.id.last_name_edt);
		phone = (EditText) findViewById(R.id.phone_edt);
		email = (EditText) findViewById(R.id.email_edt);
		address = (EditText) findViewById(R.id.address_edt);
		religion = (EditText) findViewById(R.id.religion_edt);
		bloodGroup = (EditText) findViewById(R.id.blood_group_edt);
		ailment = (EditText) findViewById(R.id.disease_edt);
		pincode = (EditText) findViewById(R.id.pin_code_edt);
		img = (ImageView) findViewById(R.id.image_view);

      /*  options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.no_photo)
		.showImageOnFail(R.drawable.no_photo)
		.showStubImage(R.drawable.no_photo)
		.cacheInMemory()
		.cacheOnDisc()
		.build();*/
        String temp = getIntent().getExtras().getString(Constants.CHILD_INFO);
        if (temp != null) {
			JSONObject object = null;
			try {
				object = new JSONObject(temp);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (object != null) {
				userInfo = new StudentProfileInfo(object);
			}
		}

	}
	private void initializeData()
	{
		if(userInfo != null)
		{
			firstName.setText(userInfo.fname);
			middlename.setText(userInfo.mname);
			lastName.setText(userInfo.lname);
			phone.setText(userInfo.mobile_no);
			address.setText(userInfo.address);
			religion.setText(userInfo.religion);
			bloodGroup.setText(userInfo.blood_group);
			ailment.setText(userInfo.disease);
			pincode.setText(userInfo.pin_code);
			email.setText(userInfo.email);
			email.setEnabled(false);
		
			new LoadImageAsyncTask(EditStudentProfileActivity.this, img, Urls.image_url_userpic, userInfo.image, true).execute();
			
			img.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					showDialogBoxForImages();
				}
			});
			}
		submite.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(firstName.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter First Name", Toast.LENGTH_SHORT).show();
					return;
				}
				
				if(lastName.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Last Name", Toast.LENGTH_SHORT).show();
					return;
				}
				if(phone.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
					return;
				}
				if(address.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Address", Toast.LENGTH_SHORT).show();
					return;
				}
				if(religion.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Religion", Toast.LENGTH_SHORT).show();
					return;
				}
				if(bloodGroup.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Blood Group", Toast.LENGTH_SHORT).show();
					return;
				}
				if(pincode.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Pin Code", Toast.LENGTH_SHORT).show();
					return;
				}
				
				
				if(email.getText().toString().length() <= 0)
				{
					Toast.makeText(EditStudentProfileActivity.this, "Enter Email Address", Toast.LENGTH_SHORT).show();
					return;
				}
				
				 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
			        nameValuePairs.add(new BasicNameValuePair("user_id", userInfo.user_id));
			        nameValuePairs.add(new BasicNameValuePair("user_type_id", userInfo.usertypeid));
			        nameValuePairs.add(new BasicNameValuePair("organization_id", userInfo.organizationid));
			        nameValuePairs.add(new BasicNameValuePair("fname", firstName.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("mname", middlename.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("lname", lastName.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("mobile_no", phone.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("address", address.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("religion", religion.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("blood_group", bloodGroup.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("disease", ailment.getText().toString().trim()));
			        nameValuePairs.add(new BasicNameValuePair("pin_code", pincode.getText().toString().trim()));
				
				new UploadAsyntask().execute(nameValuePairs);
			}
		});
	}
	
	private class UploadAsyntask extends AsyncTask<List<NameValuePair>, Void, Boolean>
	{
		String error;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(EditStudentProfileActivity.this);
			dialog.setTitle(getResources().getString(R.string.edit_profile));
			dialog.setMessage(getResources().getString(R.string.please_wait));
			dialog.setCanceledOnTouchOutside(false);
			dialog.setCancelable(false);
			dialog.show();
		}
		@Override
		protected Boolean doInBackground(List<NameValuePair>... params) {
			List<NameValuePair> nameValuePairs = params[0];
			JsonParser jParser = new JsonParser();
			JSONObject json = jParser.getJSONFromUrlnew(nameValuePairs, Urls.api_edit_profile);
			// Log parameters:
			Log.d("url extension", Urls.api_edit_profile);
			String parameters = "";
			for (NameValuePair nvp : nameValuePairs) {
				parameters += nvp.getName() + "=" + nvp.getValue() + ",";
			}
			Log.d("Parameters: ", parameters);

			
            try {

				if(json!=null){
					if(json.getString("result").equalsIgnoreCase("1")){
						try{error = json.getString("data");}catch (Exception e) {}
						return true;
					}
					else
					{
						try{error = json.getString("data");}catch (Exception e) {}
						return false;
					}
				}

			}
			catch (JSONException e) {

			}
			return false;
		}
		@Override
		protected void onPostExecute(Boolean result) {

			super.onPostExecute(result);
			if(dialog != null)
			{
				dialog.dismiss();
				dialog = null;
			}
			if(result)
			{
				dialoggg = new AlertDialog.Builder(EditStudentProfileActivity.this);
				dialoggg.setTitle(getResources().getString(R.string.edit_profile));
				if(selectedImagePath != null)
				{
					error = error + ". Image will be uploaded in the background.";
				}
				dialoggg.setMessage(error);
				dialoggg.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(selectedImagePath != null)
						{
//							Intent intent = new Intent(ParentEditProfileActivity.this, UploadImagesService.class);
//							intent.putExtra("image_paths", selectedImagePath);
//							intent.putExtra("type", "parent");
//							startService(intent);
						}
						EditStudentProfileActivity.this.finish();
					}
				});
				dialoggg.show();
			}
			else
			{
				if(error != null)
				{
					if(error.length() > 0)
					{
						SchoolAppUtils.showDialog(EditStudentProfileActivity.this,getTitle().toString(), error);
					}
					else
					{
						SchoolAppUtils.showDialog(EditStudentProfileActivity.this,getTitle().toString(), 
								getResources().getString(R.string.please_check_internet_connection));
					}
				}
				else
				{
					SchoolAppUtils.showDialog(EditStudentProfileActivity.this,getTitle().toString(), 
							getResources().getString(R.string.please_check_internet_connection));
				}

			}
		}

	}

















	private void showDialogBoxForImages()
	{

		AlertDialog.Builder alertbox = new AlertDialog.Builder(
				EditStudentProfileActivity.this);
		alertbox.setTitle("Select Option");

		alertbox.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		alertbox.setNeutralButton(R.string.camera, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 
						SELECT_CAMERA);
			}
		});
		alertbox.setPositiveButton(R.string.gallery, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent,
						"Select Picture"), SELECT_PICTURE);
			}
		});
		alertbox.show();

	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////


	private String selectedImagePath = null;
	private Uri selectedImageUri = null;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		boolean checkError = true;
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			if(requestCode == SELECT_PICTURE || requestCode == SELECT_CAMERA)
			{
				try
				{

					Bitmap out = null;
					if(requestCode == SELECT_PICTURE)
					{
						Uri chosenImageUri = data.getData();
						out = Media.getBitmap(this.getContentResolver(), chosenImageUri);
					}
					else if(requestCode == SELECT_CAMERA)
					{
						Bundle extras = data.getExtras();
						out = (Bitmap) extras.get("data");
					}
					String dir=Environment.getExternalStorageDirectory() + "/SchoolApp/Images/";
					File temp = new File(dir);
					boolean cehk = temp.mkdirs();
					File file = new File(temp, "file" + ".jpeg");
					if(file.exists())
					{
						file.delete();
					}
					FileOutputStream fOut;
					try {
						fOut = new FileOutputStream(file);
						out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
						fOut.flush();
						fOut.close();
						out.recycle();

					} catch (Exception e) {

					}
					selectedImagePath = file.getAbsolutePath();
					checkError = false;
				
				}
				catch(Exception e)
				{
					e.printStackTrace();
					selectedImagePath = null;
					Toast.makeText(EditStudentProfileActivity.this, R.string.please_try_again, Toast.LENGTH_SHORT).show();
				}
				if(!checkError)
				{
					if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
					{
						displayImages();
						//((ImageView)findViewById(R.id.faxes_data_imageview)).setImageBitmap(getPreview(selectedImagePath));
					}
				}

			}
		}
	}


	public static Uri getImageContentUri(Context context, File imageFile) {
		String filePath = imageFile.getAbsolutePath();
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[] { BaseColumns._ID },
				MediaColumns.DATA + "=? ",
				new String[] { filePath }, null);

		if (cursor != null && cursor.moveToFirst()) {
			int id = cursor.getInt(cursor
					.getColumnIndex(BaseColumns._ID));
			Uri baseUri = Uri.parse("content://media/external/images/media");
			return Uri.withAppendedPath(baseUri, "" + id);
		} else {
			if (imageFile.exists()) {
				ContentValues values = new ContentValues();
				values.put(MediaColumns.DATA, filePath);
				return context.getContentResolver().insert(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			} else {
				return null;
			}
		}
	}
	/**
	 * Get bitmap
	 * @param file
	 * @param size
	 * @return
	 */
	public static Bitmap decodeFile(String file, int size) {
		//Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, o);

		//Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;//(int)Math.pow(2, (double)(scale-1));
		while (true) {
			if (width_tmp / 2 < size || height_tmp / 2 < size) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale++;
		}

		//Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeFile(file, o2);
	}
	public String getPath(Uri uri) {
		try
		{
			String[] projection = { MediaColumns.DATA };
			Cursor cursor = managedQuery(uri, projection, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaColumns.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get preview
	 * @param selectedImageUri2
	 * @return
	 */
	Bitmap getPreview(String selectedImageUri2) {
		File image = new File(selectedImageUri2);

		BitmapFactory.Options bounds = new BitmapFactory.Options();
		bounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), bounds);
		if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
			return null;

		int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
				: bounds.outWidth;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		/** if((int)Math.pow(2, (double)(originalSize-1)) > 0)
{*/
		//opts.inSampleSize = powerOfTwo(originalSize);// / (int)Math.pow(2, (double)(originalSize-1));
		/** }
else
{
opts.inSampleSize = originalSize;
}*/
		opts.inSampleSize = originalSize / 100;
		return BitmapFactory.decodeFile(image.getPath(), opts);     
	}	

	private void displayImages()
	{
		innerImageLoader = new InnerImageLoader(EditStudentProfileActivity.this);
		innerImageLoader.clearCache();
		innerImageLoader.DisplayImage(selectedImagePath, img);
	}
	
	
}
