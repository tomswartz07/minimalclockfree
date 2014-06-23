package tom.clock;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class TomClockV1 extends AppWidgetProvider {
	private static final String TAG = "Tom-Clock";

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tom_clock_v1);
		remoteViews.setImageViewBitmap(R.id.clock_view, buildClock(context));
		// ---------------------------------------
		addAlarmIntend(context, remoteViews);
		// ---------------------------------------
		ComponentName widget = new ComponentName(context, TomClockV1.class);
		appWidgetManager.updateAppWidget(widget, remoteViews);
		context.startService(new Intent(context, TomClockV1Service.class));
	}

	@Override
	public void onEnabled(Context context) {
		context.startService(new Intent(context, TomClockV1Service.class));
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		context.stopService(new Intent(context, TomClockV1Service.class));
	}

	@Override
	public void onDisabled(Context context) {
		context.stopService(new Intent(context, TomClockV1Service.class));
}

	static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.tom_clock_v1);
		remoteViews.setImageViewBitmap(R.id.clock_view, buildClock(context));
		// ---------------------------------------
		addAlarmIntend(context, remoteViews);
		// ---------------------------------------
		ComponentName widget = new ComponentName(context, TomClockV1.class);
		appWidgetManager.updateAppWidget(widget, remoteViews);
	}

	private static void addAlarmIntend(Context context, RemoteViews remoteViews) {
		PackageManager packageManager = context.getPackageManager();

		try {
			Intent it = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = null;
			Log.d(TAG, "Android SDK version: "+Build.VERSION.SDK_INT);
			// Verify clock implementation
			String clockImpls[][] = {
					{"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
					{"Google Calendar", "com.google.android.calendar","com.android.calendar.AllInOneActivity"},
					{"DonutAlarm Clock", "com.android.alarmclock", "com.android.alarmclock.AlarmClock"},
					{"Standard Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
					{"Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
					{"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
					{"Samsung Galaxy Clock", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"},
			};

			for(int i=0; i<clockImpls.length; i++) {
				String vendor = clockImpls[i][0];
				String packageName = clockImpls[i][1];
				String className = clockImpls[i][2];
				try {
					cn = new ComponentName(packageName, className);
					@SuppressWarnings("unused")
					ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
					it.setComponent(cn);
					Log.d(TAG, "Found " + vendor + " --> " + packageName + "/" + className);
				} catch (PackageManager.NameNotFoundException e) {
					Log.d(TAG, vendor + " does not exists");
				}
			}
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, it, 0);
			remoteViews.setOnClickPendingIntent(R.id.clock_view, pendingIntent);
	} catch (Throwable e) {
		Log.d(TAG, "No default clock found.");
	}

	}

	private static Bitmap buildClock(final Context context) {
		final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		final float density = displayMetrics.density;
		final SharedPreferences preferences = context.getSharedPreferences(TomClockV1Configure.PREFS_KEY, 0);
		final boolean mode24 = preferences.getBoolean(TomClockV1Configure.TWENTY_FOUR_HOUR_MODE, false);

		final String[] days = context.getResources().getStringArray(R.array.days);
		final String[] months = context.getResources().getStringArray(R.array.months);
		final String am = context.getResources().getString(R.string.am);
		final String pm = context.getResources().getString(R.string.pm);

		final int color1 = context.getResources().getColor(R.color.hour_colour);
		final int color2 = context.getResources().getColor(R.color.minutes_colour);
		final int fontSize = (int) (18 * density);

		final Calendar calendar = Calendar.getInstance();
		final int ampm = calendar.get(Calendar.AM_PM);
		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		final int monthOfYear = calendar.get(Calendar.MONTH);
		final int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		final int minute = calendar.get(Calendar.MINUTE);

		int hourDigitOne = 0;
		int hourDigitTwo = 0;
		if (mode24) {
			final int hour = calendar.get(Calendar.HOUR_OF_DAY);
			hourDigitOne = (hour < 10 ? 0 : hour < 20 ? 1 : 2);
			hourDigitTwo = hour % 10;
		} else {
			final int hour = calendar.get(Calendar.HOUR);
			hourDigitOne = ((hour < 10 && hour != 0) ? 0 : 1);
			hourDigitTwo = (hour == 0 ? 2 : hour % 10);
		}
		final int minuteDigitOne = (minute < 10 ? 0 : minute / 10);
		final int minuteDigitTwo = (minute < 10 ? minute : minute % 10);

		final int width = 160;
		final int height = 300;
		final int numberWidth = (int) (72 * density);
		final double numberHeight = (182 / 1.5) * density;
		final int topPadding = 15;
		final int leftPadding = 10;
		final int numberGap = 2;

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setSubpixelText(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(fontSize);
		paint.setTextAlign(Align.LEFT);

		Bitmap hourBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digitshours);
		Bitmap minuteBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.digitsminutes);

		Bitmap bitmap = Bitmap.createBitmap((int) (width * density), (int) (height * density), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		paint.setColor(color1);
		if (!mode24) {
			canvas.drawText((ampm == Calendar.AM) ? am : pm, leftPadding + (int) (5 * density), fontSize, paint);
		}

		Rect source = new Rect();
		setRectToNumber(source, hourDigitOne, numberWidth, numberHeight);
		Rect dest = new Rect(leftPadding, topPadding + (int) (15 * density), leftPadding + numberWidth, (int) numberHeight + topPadding + (int) (15 * density));
		canvas.drawBitmap(hourBitmap, source, dest, paint);

		setRectToNumber(source, hourDigitTwo, numberWidth, numberHeight);
		setRect(dest, leftPadding + numberWidth + numberGap, topPadding + (int) (15 * density), leftPadding + numberWidth + numberGap + numberWidth,
				(int) (numberHeight + (int) (15 * density)));
		canvas.drawBitmap(hourBitmap, source, dest, paint);

		setRectToNumber(source, minuteDigitOne, numberWidth, numberHeight);
		setRect(dest, leftPadding, topPadding + (int) (90 * density), leftPadding + numberWidth, (int) numberHeight + topPadding + (int) (90 * density));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);

		setRectToNumber(source, minuteDigitTwo, numberWidth, numberHeight);
		setRect(dest, leftPadding + numberWidth + numberGap, topPadding + (int) (90 * density), leftPadding + numberWidth + numberGap + numberWidth, (int) (numberHeight
				+ topPadding + (int) (90 * density)));
		canvas.drawBitmap(minuteBitmap, source, dest, paint);

		canvas.drawText(days[dayOfWeek], leftPadding + (int) (9 * density), topPadding + (int) (235 * density), paint);
		canvas.drawText(months[monthOfYear] + ". " + dayOfMonth, leftPadding + (int) (9 * density), topPadding + (int) (251 * density), paint);

		paint.setColor(color2);
		canvas.drawLine(leftPadding + (int) (5 * density), topPadding + (int) (225 * density), leftPadding + (int) (5 * density), (int) (253 * density) + topPadding, paint);

		return bitmap;
	}

	static void setRectToNumber(final Rect rect, final int number, final int numberWidth, final double numberHeight) {
		rect.left = 0;
		rect.top = (int) (numberHeight * number);
		rect.right = numberWidth;
		rect.bottom = (int) (numberHeight * (number + 1));
	}

	static void setRect(final Rect rect, final int left, final int top, final int right, final int bottom) {
		rect.left = left;
		rect.top = top;
		rect.right = right;
		rect.bottom = bottom;
	}
}
