/*
 *  Tom
 */
package tom.clock;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * @author tom
 *
 */
public class TomClockV1Service extends Service {
	
	private BroadcastReceiver broadcastReceiver;
	
	@Override
	public void onCreate() {
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				TomClockV1.updateAppWidget(context, appWidgetManager);
			}
		};

		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
	}
}
