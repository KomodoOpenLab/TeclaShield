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
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

public class TeklaIME  extends InputMethodService 
	implements KeyboardView.OnKeyboardActionListener{

	private Handler handlerTimer = new Handler();
	private static final int UPDATE_STUFF_ON_DIALOG = 99999; //this is a true arbitrary number.
	
    private KeyboardView mInputView;    
    private int mLastDisplayWidth;
    private TeklaKeyboard mTeklaKeyboard;
    private TeklaKeyboard mCurKeyboard;
    private InputMethodManager imManager;    
    private int mScanCount = -1;
    
    @Override public boolean onEvaluateInputViewShown(){
    	return true;
    }

    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
    	super.onCreate();      
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        if (mTeklaKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mTeklaKeyboard = new TeklaKeyboard(this, R.xml.teklakeyboard);
    }
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        mInputView = (KeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mTeklaKeyboard);

    	imManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        return mInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        return null;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        mCurKeyboard = mTeklaKeyboard;        
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        mCurKeyboard = mTeklaKeyboard;
        if (mInputView != null) {
            mInputView.closing();
        }
    }

    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        mInputView.setKeyboard(mCurKeyboard);
        mInputView.closing();
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
    	
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
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

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
    	if(mScanCount==-1) {
    		mScanCount = 0;
        	Keyboard kb = mInputView.getKeyboard();
        	List<Key> kl = kb.getKeys();
        	Key k = kl.get(0);
        	
        	k.icon = getResources().getDrawable(R.drawable.up_pressed);
        	handlerTimer.removeCallbacks(taskUpdateStuffOnDialog );
        	handlerTimer.postDelayed(taskUpdateStuffOnDialog , 1500); //set a 1500 millisecond delay for the initial post
    	} else {
        	Keyboard kb = mInputView.getKeyboard();
        	List<Key> kl = kb.getKeys();
        	Key k = kl.get(mScanCount%6);
        	switch (mScanCount%6) {
			case 0: k.icon = getResources().getDrawable(R.drawable.up); 
			keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
			break;
			case 1: k.icon = getResources().getDrawable(R.drawable.down);
			keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
			break;
			case 2: k.icon = getResources().getDrawable(R.drawable.left);
			keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
			break;
			case 3: k.icon = getResources().getDrawable(R.drawable.right);
			keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
			break;
			case 4: k.icon = getResources().getDrawable(R.drawable.enter);
			keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);
			break;
			case 5: k.icon = getResources().getDrawable(R.drawable.back);
			keyDownUp(KeyEvent.KEYCODE_BACK);
			break;
			default: break;
    		}
        	mScanCount = -1;
    	}
    	mInputView.invalidateAllKeys();
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
    
	private Runnable taskUpdateStuffOnDialog = new Runnable() {
	       public void run() {      

	    	   if(mScanCount==-1) return;
	    	   
	    	   Keyboard kb = mInputView.getKeyboard();
	        	List<Key> kl = kb.getKeys();
	        	Key k = kl.get(mScanCount%6);
	        	
	        	switch (mScanCount%6) {
				case 0: k.icon = getResources().getDrawable(R.drawable.up); break;
				case 1: k.icon = getResources().getDrawable(R.drawable.down); break;
				case 2: k.icon = getResources().getDrawable(R.drawable.left); break;
				case 3: k.icon = getResources().getDrawable(R.drawable.right); break;
				case 4: k.icon = getResources().getDrawable(R.drawable.enter); break;
				case 5: k.icon = getResources().getDrawable(R.drawable.back); break;
				default: break;
	    		}
	        	
	        	++mScanCount;
	        	if(mScanCount==18) mScanCount = -1;
	        	else {
	        		k = kl.get(mScanCount%6);
	        		switch (mScanCount%6) {
	    			case 0: k.icon = getResources().getDrawable(R.drawable.up_pressed); break;
	    			case 1: k.icon = getResources().getDrawable(R.drawable.down_pressed); break;
	    			case 2: k.icon = getResources().getDrawable(R.drawable.left_pressed); break;
	    			case 3: k.icon = getResources().getDrawable(R.drawable.right_pressed); break;
	    			case 4: k.icon = getResources().getDrawable(R.drawable.enter_pressed); break;
	    			case 5: k.icon = getResources().getDrawable(R.drawable.back_pressed); break;
	    			default: break;
	        		}
	        	}	
	        		    
	            // handling be in the dialog
	            // don't mess with GUI from within a thread
	            Message msg = new Message();
	            msg.what = UPDATE_STUFF_ON_DIALOG;
	            handlerEvent.sendMessage(msg);  

	            //Do this again in 1.5 seconds           
	            handlerTimer.postDelayed(this, 1500);
	    }
	};
	
	private Handler handlerEvent = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	        case UPDATE_STUFF_ON_DIALOG: {
	        	mInputView.invalidateAllKeys();
	        }
	            break;          
	        default: {
	            super.handleMessage(msg);
	        }
	            break;          
	        }
	    }
	};
}