package com.knwedu.ourschool.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.knwedu.calcuttapublicschool.R;
import com.knwedu.ourschool.MainActivity;
import com.knwedu.ourschool.ParentMainActivity;
import com.knwedu.ourschool.SigninActivity;
import com.knwedu.ourschool.SplashActivity;
import com.knwedu.ourschool.TeacherMainActivity;
import com.knwedu.ourschool.utils.Constants;
import com.knwedu.ourschool.utils.SchoolAppUtils;


import me.leolin.shortcutbadger.ShortcutBadger;

public class GcmIntentService extends IntentService {

	public static int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	public static int notificationCount;
	private int badgeCount = 0;
	public static final String NOTIFICATION_TYPE="notificationtype";
	SharedPreferences sharedpreferences;
	public int count=0;

	//Notification
//	public  static final String KEY_ROW_NOTIFICATION_NAME="Notification_name";
//	public static final String KEY_ROW_NOTIFICATION_VALUE="Notification_value";
//    // private DatabaseHelper DBHelper;
//	private SQLiteDatabase db;

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "KnwEdu_GCM";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		Log.d(TAG, "Notification Data Json :" + extras.getString("message"));

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		//sharedpreferences = getSharedPreferences("NotificationCount", Context.MODE_PRIVATE);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.



		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				/*
				 * generateNotification(this,"Send error: " +
				 * extras.toString());
				 */
				generateNotificationnew(this, extras.getString("message").toString(), extras.getString("title").toString(),extras.getString("menu").toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				/*
				 * generateNotification(this,"Deleted messages on server: " +
				 * extras.toString());
				 */
				generateNotificationnew(this, extras.getString("message")
						.toString(), extras.getString("title").toString(),extras.getString("menu").toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				// This loop represents the service doing some work.
				for (int i = 0; i < 5; i++) {
					Log.i(TAG,
							"Working... " + (i + 1) + "/5 @ "
									+ SystemClock.elapsedRealtime());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				// Post notification of received message.
				// generateNotification(this,extras.getString("message").toString());
				try {
//					generateNotificationnew(this, extras.getString("message")
//							.toString(), extras.getString("title").toString(),extras.getString("menu").toString(),extras.getString("notificationinfo").toString());
					generateNotificationnew(this, extras.getString("message")
							.toString(), extras.getString("title").toString(),extras.getString("menu").toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG, "Received: " + extras.toString());

				//-----------Start of Badge Implmentation------------------
				badgeCount = badgeCount+1;
				ShortcutBadger.applyCount(this, badgeCount); //for 1.1.4
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		/*
		 * PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new
		 * Intent(this, DemoActivity.class), 0);
		 */

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.app_icon)
				.setContentTitle("Al Wahda School")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg).setTicker("Al Wahda School Notification!")
				.setAutoCancel(true);

		/* mBuilder.setContentIntent(contentIntent); */
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.app_icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = "Al Wahda School";
		Intent notificationIntent = null;

		if (SchoolAppUtils.GetSharedBoolParameter(context, Constants.UISSIGNIN) == true) {
			notificationIntent = new Intent(context, MainActivity.class);
		} else if (SchoolAppUtils.GetSharedBoolParameter(context,
				Constants.ISPARENTSIGNIN) == true) {
			notificationIntent = new Intent(context, ParentMainActivity.class);
		} else if (SchoolAppUtils.GetSharedBoolParameter(context,
				Constants.ISSIGNIN) == true) {
			notificationIntent = new Intent(context, TeacherMainActivity.class);
		} else {
			notificationIntent = new Intent(context, SigninActivity.class);
		}
		// set intent so it does not start a new activity
		// notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
		// |Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		//notification.setLatestEventInfo(context, title, message, intent);
		// notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// notification.sound = Uri.parse("android.resource://" +
		// context.getPackageName() + "your_sound_file_name.mp3");

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

	}

	private static void generateNotificationnew(Context context, String message, String title,String menu) {
		Intent notificationIntent = null;
		//Remove Badge
		ShortcutBadger.removeCount(context);
		Log.i("Received_Val", Integer.parseInt(menu) + "");
		//Log.d("test 1",notifications);

		if (SchoolAppUtils.GetSharedBoolParameter(context, Constants.UISSIGNIN) == true) {
			notificationIntent = new Intent(context, SplashActivity.class);
		} else if (SchoolAppUtils.GetSharedBoolParameter(context,
				Constants.ISPARENTSIGNIN) == true) {
			notificationIntent = new Intent(context, SplashActivity.class);
		} else if (SchoolAppUtils.GetSharedBoolParameter(context,
				Constants.ISSIGNIN) == true) {
			notificationIntent = new Intent(context, SplashActivity.class);
		} else {
			notificationIntent = new Intent(context, SplashActivity.class);
		}
		//CommonUtilities.saveInSP(context,"notificationInfo",notifications);
		notificationIntent.putExtra("menu_val",Integer.parseInt(menu));
		try {

			int icon = R.drawable.app_icon;
			long when = System.currentTimeMillis();
			Notification notification = new Notification(R.drawable.app_icon,
					message, when);

			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);

			RemoteViews contentView = new RemoteViews(context.getPackageName(),
					R.layout.custom_notification);
			contentView.setImageViewResource(R.id.image, R.drawable.app_icon);
			/*contentView.setTextViewText(R.id.title, SchoolAppUtils
					.GetSharedParameter(context, Constants.PUSH_TITLE));
			*/
			contentView.setTextViewText(R.id.title, title);
			contentView.setTextViewText(R.id.text, message);
			//contentView.setTextViewText(R.id.notification,notifications);
			//contentView.setTextViewText(R.id.count.msg_count);
			notification.contentView = contentView;

			// Intent notificationIntent = new Intent(this, MainActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = contentIntent;

			notification.flags |= Notification.FLAG_AUTO_CANCEL; // Do not clear
			// the
			// notification
			notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
			notification.defaults |= Notification.DEFAULT_VIBRATE; // Vibration
			notification.defaults |= Notification.DEFAULT_SOUND; // Sound

			notification.number = notificationCount++;

			int notification_id = (int) System.currentTimeMillis();
			mNotificationManager.notify(notification_id, notification);

		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
