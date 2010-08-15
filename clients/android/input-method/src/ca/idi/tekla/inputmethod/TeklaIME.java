package ca.idi.tekla.inputmethod;

import java.util.ArrayList;
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

    private int mLastDisplayWidth;
	private KeyboardView mKeyboardView;    
    private TeklaKeyboard mTeklaKeyboard;
    private TeklaKeyboard mCurKeyboard;
    private InputMethodManager imManager;    
    
    private enum ScanState {
    	IDLE, SCANNING_ROW, SCANNING_COLUMN
    }
    private ScanState mScanState;
    private int mScanCount, mCurrScanRow;
    private ArrayList<Integer> mFirstColumnKeyPointer;
    
    private String mWordSeparators;
    private StringBuilder mComposing = new StringBuilder();
    
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override
    public void onCreate() {
    	super.onCreate(); 
        mWordSeparators = getResources().getString(R.string.word_separators);     
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
    	mScanState = ScanState.IDLE;
    	mFirstColumnKeyPointer = new ArrayList<Integer>(5);
    	mFirstColumnKeyPointer.add(new Integer(0));
    	mFirstColumnKeyPointer.add(new Integer(10));
    	mFirstColumnKeyPointer.add(new Integer(19));
    	mFirstColumnKeyPointer.add(new Integer(28));
    	mFirstColumnKeyPointer.add(new Integer(33));
    	
    	
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
        //mTeklaKeyboard = new TeklaKeyboard(this, R.xml.ui_navigation);
        mTeklaKeyboard = new TeklaKeyboard(this, R.xml.qwerty);

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
		
		// Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        
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
        //Toast.makeText(this, "Window Hidden", Toast.LENGTH_SHORT).show();
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

    private String getWordSeparators() {
        return mWordSeparators;
    }
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

	/**
	* From OnKeyboardActionListener implementation. Called when a key is
	* pressed on the soft IME.
	*/
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		Keyboard kb = mKeyboardView.getKeyboard();
		List<Key> kl = kb.getKeys();
		Key k;
		int startIndex, endIndex, colCount, sel;
		switch (mScanState) {
		case IDLE:
			for(int i=0; i<mFirstColumnKeyPointer.get(1).intValue(); ++i) {
				k = kl.get(i);
				k.pressed = true;
			}
			mScanState = ScanState.SCANNING_ROW;
			mScanCount = 0;
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
			break;
		case SCANNING_ROW:
			timerHandler.removeCallbacks(updateKeyDrawables);
			startIndex = mFirstColumnKeyPointer.get(mScanCount%mFirstColumnKeyPointer.size()).intValue();
			if((mScanCount+1)%mFirstColumnKeyPointer.size()==0) endIndex = startIndex + 6;
			else endIndex = mFirstColumnKeyPointer.get((mScanCount+1)%mFirstColumnKeyPointer.size()).intValue();
			for(int i=startIndex+1; i<endIndex; ++i) {
				k = kl.get(i);
				k.pressed = false;
			}
			mScanState = ScanState.SCANNING_COLUMN;
			mCurrScanRow = mScanCount%mFirstColumnKeyPointer.size();
			mScanCount = 0;
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
			break;
		case SCANNING_COLUMN:
			timerHandler.removeCallbacks(updateKeyDrawables);
			mScanState = ScanState.IDLE;
			startIndex = mFirstColumnKeyPointer.get(mCurrScanRow).intValue();
			if(mCurrScanRow==mFirstColumnKeyPointer.size()-1) colCount = 6;
			else colCount = mFirstColumnKeyPointer.get(mCurrScanRow+1).intValue() - startIndex;
			sel = startIndex+(mScanCount%colCount);
			k = kl.get(sel);
			k.pressed = false;
			//keyDownUp(k.codes[0]);
			if(mCurrScanRow==mFirstColumnKeyPointer.size()-1) { // UI Navigation 
				switch (mScanCount%6) {
				case 0:
					keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
					break;
				case 1:
					keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case 2:
					keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case 3:
					keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
				case 4:
					keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);
					break;
				case 5:
					keyDownUp(KeyEvent.KEYCODE_BACK);
					break;
				default:
					break;
				}
			} else {
				sendKey(k.codes[0]);
				/*
				if (isWordSeparator(k.codes[0])) {
		            // Handle separator
		            if (mComposing.length() > 0) {
		                commitTyped(getCurrentInputConnection());
		            }
		            sendKey(primaryCode);
		            updateShiftKeyState(getCurrentInputEditorInfo());
		        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
		            handleBackspace();
		        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
		            handleShift();
		        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
		            handleClose();
		            return;
		        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
		            // Show a menu or somethin'
		        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
		                && mInputView != null) {
		            Keyboard current = mInputView.getKeyboard();
		            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
		                current = mQwertyKeyboard;
		            } else {
		                current = mSymbolsKeyboard;
		            }
		            mInputView.setKeyboard(current);
		            if (current == mSymbolsKeyboard) {
		                current.setShifted(false);
		            }
		        } else {
		            handleCharacter(primaryCode, keyCodes);
		        }
		      	*/
			}
			
			break;
		}
		mKeyboardView.invalidateAllKeys(); // Redraw keyboard
		
		/*
		if(mScanCount==-1) {
			mScanCount = 0;
			Key k = kl.get(0);
			k.pressed = true;
			
			timerHandler.removeCallbacks(updateKeyDrawables );
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
		} else {
			Key k = kl.get(mScanCount%6);
			k.pressed = false;
			switch (mScanCount%6) {
				case 0:
					keyDownUp(KeyEvent.KEYCODE_DPAD_UP);
					break;
				case 1:
					keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case 2:
					keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case 3:
					keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
				case 4:
					keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER);
					break;
				case 5:
					keyDownUp(KeyEvent.KEYCODE_BACK);
					break;
				default:
					break;
			}
			mScanCount = -1;
		}
		mKeyboardView.invalidateAllKeys(); // Redraw keyboard
		*/
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

			Keyboard kb = mKeyboardView.getKeyboard();
			List<Key> kl = kb.getKeys();
			Key k;
			int startIndex, endIndex, colCount;
			
			switch(mScanState) {
			case IDLE:
				return;
			case SCANNING_ROW:
				startIndex = mFirstColumnKeyPointer.get(mScanCount%mFirstColumnKeyPointer.size()).intValue();
				if((mScanCount+1)%mFirstColumnKeyPointer.size()==0) endIndex = startIndex + 6;
				else endIndex = mFirstColumnKeyPointer.get((mScanCount+1)%mFirstColumnKeyPointer.size()).intValue();
				for(int i=startIndex; i<endIndex; ++i) {
					k = kl.get(i);
					k.pressed = false;
				}
				break;
			case SCANNING_COLUMN:
				startIndex = mFirstColumnKeyPointer.get(mCurrScanRow%mFirstColumnKeyPointer.size()).intValue();
				if((mCurrScanRow+1)%mFirstColumnKeyPointer.size()==0) colCount = 6;
				else colCount = mFirstColumnKeyPointer.get((mCurrScanRow+1)%mFirstColumnKeyPointer.size()).intValue() - startIndex;
				k = kl.get(mFirstColumnKeyPointer.get(mCurrScanRow).intValue()+(mScanCount%colCount));
				k.pressed = false;
				break;
			}
			
			++mScanCount;
			
			switch(mScanState) {
			case SCANNING_ROW:
				if(mScanCount/mFirstColumnKeyPointer.size()==3) return;
				startIndex = mFirstColumnKeyPointer.get(mScanCount%mFirstColumnKeyPointer.size()).intValue();
				if((mScanCount+1)%mFirstColumnKeyPointer.size()==0) endIndex = startIndex + 6;
				else endIndex = mFirstColumnKeyPointer.get((mScanCount+1)%mFirstColumnKeyPointer.size()).intValue();
				for(int i=startIndex; i<endIndex; ++i) {
					k = kl.get(i);
					k.pressed = true;
				}
				break;
			case SCANNING_COLUMN:
				startIndex = mFirstColumnKeyPointer.get(mCurrScanRow%mFirstColumnKeyPointer.size()).intValue();
				if((mCurrScanRow+1)%mFirstColumnKeyPointer.size()==0) colCount = 6;
				else colCount = mFirstColumnKeyPointer.get((mCurrScanRow+1)%mFirstColumnKeyPointer.size()).intValue() - startIndex;
				if(mScanCount/colCount==3) return;
				k = kl.get(mFirstColumnKeyPointer.get(mCurrScanRow).intValue()+(mScanCount%colCount));
				k.pressed = true;
				break;				
			}
			
			/*
			if(mScanCount==-1) return;
			   
			Keyboard kb = mKeyboardView.getKeyboard();
			List<Key> kl = kb.getKeys();
			Key k = kl.get(mScanCount%6);
			k.pressed = false;
			
			++mScanCount;
			if(mScanCount==18) mScanCount = -1;
			else {
				k = kl.get(mScanCount%6);
				k.pressed = true;
			}	
			*/
			
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
