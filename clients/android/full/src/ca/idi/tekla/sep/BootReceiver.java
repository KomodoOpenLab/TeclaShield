package ca.idi.tekla.sep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction(SEPService.INTENT);
		context.startService(serviceIntent);
	}
}
