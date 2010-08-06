package ca.idi.tekla.inputmethod;

import java.util.List;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class TeklaIME  extends InputMethodService 
	implements KeyboardView.OnKeyboardActionListener {

	// TODO: SCAN_DELAY should be set from settings.
	private static final int SCAN_DELAY = 1000; //in milliseconds
	private static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	
	private Handler timerHandler = new Handler();
    private int mScanCount = -1;

    private int mLastDisplayWidth;
	private KeyboardView mKeyboardView;    
    private TeklaKeyboard mTeklaKeyboard;
    private TeklaKeyboard mCurKeyboard;
    private InputMethodManager imManager;    
    
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
    	super.onCreate();      
    }
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override
    public View onCreateInputView() {
        mKeyboardView = (KeyboardView) getLayoutInflater().inflate(
                R.layout.tekla_keyboardview, null);
        mKeyboardView.setOnKeyboardActionListener(this);
        mKeyboardView.setKeyboard(mTeklaKeyboard);
        
    	imManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return mKeyboardView;
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        if (mTeklaKeyboard != null) {
            // Configuration changes can happen after the keyboard gets
            // recreated, so we need to be able to re-build the keyboards if
            // the available space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mTeklaKeyboard = new TeklaKeyboard(this, R.xml.ui_navigation);

    }
    
	/**
	* This is the main point where we do our initialization of the input
	* method to begin operating on an application.  At this point we have
	* been bound to the client, and are now receiving all of the detailed
	* information about the target of our edits.
	*/
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		super.onStartInput(attribute, restarting);
		mCurKeyboard = mTeklaKeyboard;        
		mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        mKeyboardView.setKeyboard(mCurKeyboard);
        mKeyboardView.closing();
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        return null; // No candidates view!
    }

	/**
	* Use this (and onKeyUp below) to monitor hardware keyboard events being
	* delivered to the application. We get first crack at them, and can
	* either consume them or let them continue to the app.
	*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_BACK:
	            // The InputMethodService already takes care of the back
	            // key for us, to dismiss the input method if it is shown.
	            // However, our keyboard could be showing a pop-up window
	            // that back should dismiss, so we first allow it to do that.
	            if (event.getRepeatCount() == 0 && mKeyboardView != null) {
	                if (mKeyboardView.handleBack()) {
	                    return true;
	                }
	            }
	            break;
	            
	        case KeyEvent.KEYCODE_DEL:
	            break;
	            
	        case KeyEvent.KEYCODE_ENTER:
	            // Let the underlying text editor always handle these.
	            return false;
	       
	        default:
	            // For all other keys, if we want to do transformations on
	            // text being entered with a hard keyboard, we need to process
	            // it and do the appropriate action.

	    }
	    return super.onKeyDown(keyCode, event);
	}

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

	/**
	* This is called every time the soft IME window is hidden from the user.
	*/
    @Override
    public void onWindowHidden() {
    	showWindow(true); // Never hide the window!
        Toast.makeText(this, "Window Hidden", Toast.LENGTH_SHORT).show();
    }

	/**
	* This is called to determine weather the IME window should be
	* shown to the user.
	*/
    @Override
    public boolean onEvaluateInputViewShown() {
    	return true; // Always encourage the IME window to be displayed.
    }

	/**
	* This is called to determine weather the IME window should be
	* displayed in fullscreen mode.
	*/
    @Override
    public boolean onEvaluateFullscreenMode() {
    	return false; // Never use fullscreen mode.
    }

	/**
	* From OnKeyboardActionListener implementation. Called when a key is
	* pressed on the soft IME.
	*/
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		Keyboard kb = mKeyboardView.getKeyboard();
		List<Key> kl = kb.getKeys();
		if(mScanCount==-1) {
			mScanCount = 0;
			Key k = kl.get(0);
			
			k.icon = getResources().getDrawable(R.drawable.up_pressed);
			timerHandler.removeCallbacks(updateKeyDrawables );
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
		} else {
			Key k = kl.get(mScanCount%6);
			switch (mScanCount%6) {
				case 0:
					k.icon = getResources().getDrawable(R.drawable.up); 
					keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
					break;
				case 1:
					k.icon = getResources().getDrawable(R.drawable.down);
					keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case 2:
					k.icon = getResources().getDrawable(R.drawable.left);
					keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case 3:
					k.icon = getResources().getDrawable(R.drawable.right);
					keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
				case 4:
					k.icon = getResources().getDrawable(R.drawable.enter);
					keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);
					break;
				case 5:
					k.icon = getResources().getDrawable(R.drawable.back);
					keyDownUp(KeyEvent.KEYCODE_BACK);
					break;
				default:
					break;
			}
			mScanCount = -1;
		}
		mKeyboardView.invalidateAllKeys(); // Redraw keyboard
		// TODO: Uncomment code below to allow for direct touch functionality.
		// TODO: Add optional direct touch functionality to settings.
		/*
		if(primaryCode == 19) {
	    	keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
	    } else if(primaryCode == 20) {
	    	keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
	    } else if(primaryCode == 21) {
	    	keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
	    } else if(primaryCode == 22) {
	    	keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
	    } else if(primaryCode == 23) {
	    	keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);
	    } else if(primaryCode == 4) {
	    	keyDownUp(KeyEvent.KEYCODE_BACK);
	    } 		
		*/
	}

	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub
	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub
	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub
	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub
	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub
	}
    
	private Runnable updateKeyDrawables = new Runnable() {
		public void run() {
			
			if(mScanCount==-1) return;
			   
			Keyboard kb = mKeyboardView.getKeyboard();
			List<Key> kl = kb.getKeys();
			Key k = kl.get(mScanCount%6);
			
			switch (mScanCount%6) {
				case 0:
					k.icon = getResources().getDrawable(R.drawable.up);
					break;
				case 1:
					k.icon = getResources().getDrawable(R.drawable.down);
					break;
				case 2:
					k.icon = getResources().getDrawable(R.drawable.left);
					break;
				case 3:
					k.icon = getResources().getDrawable(R.drawable.right);
					break;
				case 4:
					k.icon = getResources().getDrawable(R.drawable.enter);
					break;
				case 5:
					k.icon = getResources().getDrawable(R.drawable.back);
					break;
				default:
					break;
			}
			
			++mScanCount;
			if(mScanCount==18) mScanCount = -1;
			else {
				k = kl.get(mScanCount%6);
				switch (mScanCount%6) {
					case 0:
						k.icon = getResources().getDrawable(R.drawable.up_pressed);
						break;
					case 1:
						k.icon = getResources().getDrawable(R.drawable.down_pressed);
						break;
					case 2:
						k.icon = getResources().getDrawable(R.drawable.left_pressed);
						break;
					case 3:
						k.icon = getResources().getDrawable(R.drawable.right_pressed);
						break;
					case 4:
						k.icon = getResources().getDrawable(R.drawable.enter_pressed);
						break;
					case 5:
						k.icon = getResources().getDrawable(R.drawable.back_pressed);
						break;
					default:
						break;
				}
			}	
				    
			// Should't mess with GUI from within a thread,
			// we'll use a handler for that.
			Message msg = Message.obtain();
			msg.what = REDRAW_KEYBOARD;
			uiDrawHandler.sendMessage(msg);  
			
			//Do this again in SCAN_DELAY milliseconds           
			timerHandler.postDelayed(this, SCAN_DELAY);
		}
	};
	
	/**
	* Helper handler to perform UI drawing operations.
	*/
	private Handler uiDrawHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REDRAW_KEYBOARD:
					mKeyboardView.invalidateAllKeys(); // Redraw keyboard
					break;          
				default:
					super.handleMessage(msg);
					break;          
			}
		}
	};
	
	/**
	* Helper to send a key down / key up pair to the current editor.
	*/
	private void keyDownUp(int keyEventCode) {
		getCurrentInputConnection().sendKeyEvent(
			new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
		getCurrentInputConnection().sendKeyEvent(
			new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
	}
    
}
