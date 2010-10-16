package ca.idi.tekla.sep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SEPBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent sepIntent = new Intent(SwitchEventProvider.INTENT_START_SERVICE);
		context.startService(sepIntent);
	}

}
