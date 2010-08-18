package ca.idi.tekla.ime;

import java.util.ArrayList;
import java.util.List;

import ca.idi.tekla.R;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.os.Handler;
import android.os.Message;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class TeklaIME  extends InputMethodService 
	implements KeyboardView.OnKeyboardActionListener {

	// TODO: SCAN_DELAY should be set from settings.
	private static final int SCAN_DELAY = 1000; //in milliseconds
	private static final int REDRAW_KEYBOARD = 99999; //this is a true arbitrary number.
	private Handler timerHandler = new Handler();

	private KeyboardView mKeyboardView; 
    private TeklaKeyboard mSymbolsKeyboard;
    private TeklaKeyboard mSymbolsShiftedKeyboard;   
    private TeklaKeyboard mTeklaKeyboard;
    private TeklaKeyboard mCurKeyboard;    
    
    private enum ScanState {
    	IDLE, SCANNING_ROW, SCANNING_COLUMN
    }
    private ScanState mScanState;
    private int mScanCount, mCurrScanRow;
    private ArrayList<Integer> mFirstColKeyPtr;

    // variables from the Soft Keyboard Sample 
    private int mLastDisplayWidth;
    private String mWordSeparators;
    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private CompletionInfo[] mCompletions;
    private CandidateView mCandidateView;
    private long mMetaState;
    
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
        
    	mScanState = ScanState.IDLE;
    	setQwertyKeyPointers();
    	    	
        return mKeyboardView;
    }

    private void setQwertyKeyPointers() {
    	mFirstColKeyPtr = new ArrayList<Integer>(5);
    	mFirstColKeyPtr.add(new Integer(0));
    	mFirstColKeyPtr.add(new Integer(10));
    	mFirstColKeyPtr.add(new Integer(19));
    	mFirstColKeyPtr.add(new Integer(28));
    	mFirstColKeyPtr.add(new Integer(33));    	
    }

    private void setSymbolsKeyPointers() { 
    	mFirstColKeyPtr = new ArrayList<Integer>(5);
    	mFirstColKeyPtr.add(new Integer(0));
    	mFirstColKeyPtr.add(new Integer(10));
    	mFirstColKeyPtr.add(new Integer(20));
    	mFirstColKeyPtr.add(new Integer(29));
    	mFirstColKeyPtr.add(new Integer(34));   	
    }

    private void setShiftedSymbolsKeyPointers() {
    	mFirstColKeyPtr = new ArrayList<Integer>(5);
    	mFirstColKeyPtr.add(new Integer(0));
    	mFirstColKeyPtr.add(new Integer(10));
    	mFirstColKeyPtr.add(new Integer(20));
    	mFirstColKeyPtr.add(new Integer(29));
    	mFirstColKeyPtr.add(new Integer(34));
    	
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
        mTeklaKeyboard = new TeklaKeyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new TeklaKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new TeklaKeyboard(this, R.xml.symbols_shift);

    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
    
    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mKeyboardView == null) {
            return;
        }
        
        Keyboard currentKeyboard = mKeyboardView.getKeyboard();
        if (mTeklaKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mKeyboardView.setShifted(mCapsLock || !mKeyboardView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            mKeyboardView.setKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
            setShiftedSymbolsKeyPointers();
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            mKeyboardView.setKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
            setSymbolsKeyPointers();
        }
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mTeklaKeyboard.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
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
        updateCandidates();

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }
        
        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                setSymbolsKeyPointers();
                break;
                
            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                setSymbolsKeyPointers();
                break;
                
            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mTeklaKeyboard;
                mPredictionOn = true;
                setQwertyKeyPointers();
                
                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }
                
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }
                
                if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }
                
                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;
                
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mTeklaKeyboard;
                updateShiftKeyState(attribute);
        }
        
		mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
	}

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        
        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();
        
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
        mCurKeyboard = mTeklaKeyboard;
        if (mKeyboardView != null) {
        	mKeyboardView.closing();
        }
    }
    
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        mKeyboardView.setKeyboard(mCurKeyboard);
        mKeyboardView.closing();
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }
    
    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        
        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }
        
        onKey(c, null);
        
        return true;
    }
    
    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
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
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null 
                && mKeyboardView != null && mTeklaKeyboard == mKeyboardView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mKeyboardView.setShifted(mCapsLock || caps != 0);
        }
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

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mKeyboardView.closing();
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
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
			for(int i=0; i<mFirstColKeyPtr.get(1).intValue(); ++i) {
				k = kl.get(i);
				k.pressed = true;
			}
			mScanState = ScanState.SCANNING_ROW;
			mScanCount = 0;
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
			break;
		case SCANNING_ROW:
			timerHandler.removeCallbacks(updateKeyDrawables);
			startIndex = mFirstColKeyPtr.get(mScanCount%mFirstColKeyPtr.size()).intValue();
			if((mScanCount+1)%mFirstColKeyPtr.size()==0) endIndex = startIndex + 6;
			else endIndex = mFirstColKeyPtr.get((mScanCount+1)%mFirstColKeyPtr.size()).intValue();
			for(int i=startIndex+1; i<endIndex; ++i) {
				k = kl.get(i);
				k.pressed = false;
			}
			mScanState = ScanState.SCANNING_COLUMN;
			mCurrScanRow = mScanCount%mFirstColKeyPtr.size();
			mScanCount = 0;
			timerHandler.postDelayed(updateKeyDrawables , SCAN_DELAY); //set delay for the initial post
			break;
		case SCANNING_COLUMN:
			timerHandler.removeCallbacks(updateKeyDrawables);
			mScanState = ScanState.IDLE;
			startIndex = mFirstColKeyPtr.get(mCurrScanRow).intValue();
			if(mCurrScanRow==mFirstColKeyPtr.size()-1) colCount = 6;
			else colCount = mFirstColKeyPtr.get(mCurrScanRow+1).intValue() - startIndex;
			sel = startIndex+(mScanCount%colCount);
			k = kl.get(sel);
			k.pressed = false;
			if(mCurrScanRow==mFirstColKeyPtr.size()-1) { // UI Navigation 
				switch (mScanCount%6) {
				case 0: keyDownUp(KeyEvent.KEYCODE_DPAD_UP); break;
				case 1: keyDownUp(KeyEvent.KEYCODE_DPAD_DOWN); break;
				case 2: keyDownUp(KeyEvent.KEYCODE_DPAD_LEFT); break;
				case 3: keyDownUp(KeyEvent.KEYCODE_DPAD_RIGHT); break;
				case 4: keyDownUp(KeyEvent.KEYCODE_DPAD_CENTER); break;
				case 5: keyDownUp(KeyEvent.KEYCODE_BACK); break;
				default: break;
				}
			} else {
				if (isWordSeparator(k.codes[0])) {
		            // Handle separator
		            if (mComposing.length() > 0) {
		                commitTyped(getCurrentInputConnection());
		            }
		            sendKey(k.codes[0]);
		            updateShiftKeyState(getCurrentInputEditorInfo());
		        } else if (k.codes[0] == Keyboard.KEYCODE_DELETE) {
		            handleBackspace();
		        } else if (k.codes[0] == Keyboard.KEYCODE_SHIFT) {
		            handleShift();
		        } else if (k.codes[0] == Keyboard.KEYCODE_CANCEL) {
		            handleClose();
		            return;
		        } else if (k.codes[0] == TeklaKeyboardView.KEYCODE_OPTIONS) {
		            // Show a menu or somethin'
		        } else if (k.codes[0] == Keyboard.KEYCODE_MODE_CHANGE
		                && mTeklaKeyboard != null) {
		            Keyboard current = mKeyboardView.getKeyboard();
		            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
		                current = mTeklaKeyboard;
		                setQwertyKeyPointers();
		            } else {
		                current = mSymbolsKeyboard;
		                setSymbolsKeyPointers();
		            }
		            mKeyboardView.setKeyboard(current);
		            if (current == mSymbolsKeyboard) {
		                current.setShifted(false);
		            }
		        } else {
		            handleCharacter(k.codes[0], keyCodes);
		        }
		      	
			}
			
			break;
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
		InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
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

    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
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
				startIndex = mFirstColKeyPtr.get(mScanCount%mFirstColKeyPtr.size()).intValue();
				if((mScanCount+1)%mFirstColKeyPtr.size()==0) endIndex = startIndex + 6;
				else endIndex = mFirstColKeyPtr.get((mScanCount+1)%mFirstColKeyPtr.size()).intValue();
				for(int i=startIndex; i<endIndex; ++i) {
					k = kl.get(i);
					k.pressed = false;
				}
				break;
			case SCANNING_COLUMN:
				startIndex = mFirstColKeyPtr.get(mCurrScanRow%mFirstColKeyPtr.size()).intValue();
				if((mCurrScanRow+1)%mFirstColKeyPtr.size()==0) colCount = 6;
				else colCount = mFirstColKeyPtr.get((mCurrScanRow+1)%mFirstColKeyPtr.size()).intValue() - startIndex;
				k = kl.get(mFirstColKeyPtr.get(mCurrScanRow).intValue()+(mScanCount%colCount));
				k.pressed = false;
				break;
			}
			
			++mScanCount;
			
			switch(mScanState) {
			case SCANNING_ROW:
				if(mScanCount/mFirstColKeyPtr.size()==3 &&
						mScanCount%mFirstColKeyPtr.size()==0) {
					mScanState = ScanState.IDLE;
					break;
				}
				startIndex = mFirstColKeyPtr.get(mScanCount%mFirstColKeyPtr.size()).intValue();
				if((mScanCount+1)%mFirstColKeyPtr.size()==0) endIndex = startIndex + 6;
				else endIndex = mFirstColKeyPtr.get((mScanCount+1)%mFirstColKeyPtr.size()).intValue();
				for(int i=startIndex; i<endIndex; ++i) {
					k = kl.get(i);
					k.pressed = true;
				}
				break;
			case SCANNING_COLUMN:
				startIndex = mFirstColKeyPtr.get(mCurrScanRow%mFirstColKeyPtr.size()).intValue();
				if((mCurrScanRow+1)%mFirstColKeyPtr.size()==0) colCount = 6;
				else colCount = mFirstColKeyPtr.get((mCurrScanRow+1)%mFirstColKeyPtr.size()).intValue() - startIndex;
				if(mScanCount/colCount==3 && mScanCount%colCount==0) {
					mScanState = ScanState.IDLE;
					break;
				}
				k = kl.get(mFirstColKeyPtr.get(mCurrScanRow).intValue()+(mScanCount%colCount));
				k.pressed = true;
				break;				
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
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
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
    
}
