package ca.idi.tekla;

import ca.idi.tekla.sep.SwitchEventProvider;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Demo extends Activity {

	private Button startBtn, stopBtn;	
	private EditText outEditText;
	private String logText = "";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //Views
        startBtn = (Button) findViewById(R.id.Button01);
        stopBtn = (Button) findViewById(R.id.Button02);
        outEditText = (EditText) findViewById(R.id.EditText01);
        
        //Intents & Intent Filters
        final Intent serviceIntent = new Intent();
        serviceIntent.setAction(SwitchEventProvider.INTENT_START_SERVICE);
        IntentFilter switchEventFilter = new IntentFilter(SwitchEventProvider.ACTION_SWITCH_EVENT_RECEIVED);

        outEditText.setText("");

        //Start service when Activity is run
        //startService(serviceIntent);

    	registerReceiver(switchBroadcastReceiver, switchEventFilter);

    	startBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(serviceIntent);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	stopService(serviceIntent);
            }
        });

  }

	// Updates text in EditText view
	public void updateOutText(String s) {
    	logText += s;
		outEditText.setText(logText);
	}

	// Switch event provider events will be processed here
	private BroadcastReceiver switchBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			switch (extras.getInt(SwitchEventProvider.EXTRA_SWITCH_EVENT)) {
				case SwitchEventProvider.SWITCH_FWD:
					updateOutText("F");
					break;
				case SwitchEventProvider.SWITCH_BACK:
					updateOutText("B");
					break;
				case SwitchEventProvider.SWITCH_RIGHT:
					updateOutText("R");
					break;
				case SwitchEventProvider.SWITCH_LEFT:
					updateOutText("L");
					break;
			}
		}
	};

	@Override
	protected void onResume() {
		// Activity woke up
		super.onResume();
	}

	@Override
	protected void onPause() {
		// The activity went to sleep
		//unregisterReceiver(mIntentReceiver);
		super.onPause();
	}

	//private void updateRXbyte(String s) {
		//rxByteString = s;
		//logHandler.sendEmptyMessage(1);
	//}

	//private void updateTXbyte(String s) {
		//txByteString = s;
		//logHandler.sendEmptyMessage(2);
	//}

	//private Handler logHandler = new Handler() {
		//@Override
		//public void handleMessage(Message msg) {
			//switch (msg.what) {
			//case 0: logView.setText(logString);
			//case 1: rxByte.setText(rxByteString);
			//case 2: txByte.setText(txByteString);
			//}
		//}
	//};
    
}