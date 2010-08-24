package ca.idi.tekla.ime;

import java.util.ArrayList;
import java.util.List;

import ca.idi.tekla.R;
import ca.idi.tekla.ime.TeklaIMEHelper;
import ca.idi.tekla.sep.SwitchEventProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.method.MetaKeyKeyListener;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class TeklaInputMethod extends InputMethodService 
	implements KeyboardView.OnKeyboardActionListener {

	// TODO: SCAN_DELAY should be set from settings.
	// private static final int SCAN_DELAY = 1000; //in milliseconds
	// private Handler timerHandler = new Handler();

    private enum KeyboardType {
    	HARD_KEYS, QWERTY, SYMBOLS, SYMBOLS_SHIFT
    }
    private KeyboardType mKeyboardType;
    private int mImeOptions;
	private KeyboardView mKeyboardView; 
    private TeklaKeyboard mHardKeysKeyboard, mQwertyKeyboard,
		mSymbolsKeyboard, mSymbolsShiftKeyboard;
    private TeklaIMEHelper mTeklaIMEHelper;
    
    private enum ScanState {
    	SCANNING_ROW, SCANNING_COLUMN
    }
    private ScanState mScanState;
    private int mScanItemCount, mScanRowCount;
    private ArrayList<Integer> firstKeyPointers;

    // variables from the Soft Keyboard Sample 
    private int mLastDisplayWidth;
    private String mWordSeparators;
    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private CompletionInfo[] mCompletions;
    private TeklaCandidateView mCandidateView;
    private long mMetaState;
    
	/**
	* Main initialization of the input method component.  Be sure to call
	* to super class.
	*/
	@Override
	public void onCreate() {
		super.onCreate(); 
		
		// Use the following line to debug IME service.
		// android.os.Debug.waitForDebugger();

		mTeklaIMEHelper = new TeklaIMEHelper(this);
        mKeyboardType = KeyboardType.HARD_KEYS;

		mWordSeparators = getResources().getString(R.string.word_separators);     
		//Intents & Intent Filters
		IntentFilter sepServiceFilter = new IntentFilter(SwitchEventProvider.ACTION_SWITCH_EVENT_RECEIVED);
		registerReceiver(sepBroadcastReceiver, sepServiceFilter);
	}

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets
            // recreated, so we need to be able to re-build the keyboards if
            // the available space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mHardKeysKeyboard = new TeklaKeyboard(this, R.xml.hard_keys);
        mQwertyKeyboard = new TeklaKeyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new TeklaKeyboard(this, R.xml.symbols);
        mSymbolsShiftKeyboard = new TeklaKeyboard(this, R.xml.symbols_shift);
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
        showKeyboard(mKeyboardType, null);
        
        return mKeyboardView;
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
		
        if (mTeklaIMEHelper.retrievePersistentKeyboard())
        	showWindow(true);
        else
        	hideWindow();
        if (mTeklaIMEHelper.retrieveConnectShield())
        	startSwitchEventProvider();
        else
        	stopSwitchEventProvider();
		
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
                mKeyboardType = KeyboardType.SYMBOLS;
                break;
                
            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
            	mKeyboardType = KeyboardType.SYMBOLS;
                break;
                
            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
            	mKeyboardType = KeyboardType.QWERTY;
                mPredictionOn = true;
                
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
                // For all unknown input types default to
            	// the current keyboard and features
           		// mKeyboardType = KeyboardType.QWERTY;
        }
        mImeOptions = attribute.imeOptions;
	}

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // Apply the selected keyboard to the input view.
        showKeyboard(mKeyboardType, mImeOptions);
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

        // showKeyboard(KeyboardType.HARD_KEYS, null);
    }
    
    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override
    public View onCreateCandidatesView() {
        mCandidateView = new TeklaCandidateView(this);
        mCandidateView.setService(this);
        return null; // Do not create a candidate view
        //TODO: Implement functional candidate view?
        //return mCandidateView;
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
	* Use this (and onKeyUp below) to monitor hardware keyboard events being
	* delivered to the application. We get first crack at them, and can
	* either consume them or let them continue to the app.
	*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean CONSUME = true;
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_DPAD_UP:
	        	selectFocused();
	            return CONSUME;
	        case KeyEvent.KEYCODE_DPAD_DOWN:
	    		stepBack();
	            return CONSUME;
	        case KeyEvent.KEYCODE_DPAD_RIGHT:
	        	focusNext();
	            return CONSUME;
	        case KeyEvent.KEYCODE_DPAD_LEFT:
	        	focusPrev();
	            return CONSUME;
	        case KeyEvent.KEYCODE_BACK:
	            return !CONSUME;
	        case KeyEvent.KEYCODE_ENTER:
	            return !CONSUME;

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
	* From OnKeyboardActionListener implementation.
	* Called when the user presses a key. This is sent
	* before the onKey(int, int[]) is called.
	* For keys that repeat, this is only called once.
	*/
	@Override
	public void onPress(int primaryCode) {
	}

	/**
	* From OnKeyboardActionListener implementation.
	* Called when a key is pressed on the soft IME.
	*/
	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		processSelection(primaryCode, keyCodes);
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void swipeUp() {
		// TODO: Fix highlighting to enable this
		// selectFocused();
	}
	@Override
	public void swipeDown() {
		// TODO: Fix highlighting to enable this
		// stepBack();
	}

	@Override
	public void swipeRight() {
		// TODO: Fix highlighting to enable this
		// focusNext();
	}
	@Override
	public void swipeLeft() {
		// TODO: Fix highlighting to enable this
		// focusPrev();
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

	/**
	* This is called every time the soft IME window is hidden from the user.
	*/
	@Override
	public void onWindowHidden() {
		showKeyboard(KeyboardType.HARD_KEYS, null);
        if (mTeklaIMEHelper.retrievePersistentKeyboard())
        	showWindow(true);
        else
        	hideWindow();
	}

	/**
	* This is called to determine weather the IME window should be
	* displayed in fullscreen mode.
	*/
    @Override
    public boolean onEvaluateFullscreenMode() {
    	return false; // Never use fullscreen mode.
    }

	public void selectFocused() {
		List<Key> kl = mKeyboardView.getKeyboard().getKeys();
		int firstKeyIndex, sel;

		clearAllHighlights();
		switch (mScanState) {
			case SCANNING_ROW:
				mScanRowCount = mScanItemCount%firstKeyPointers.size();
				mScanItemCount = 0;
				mScanState = ScanState.SCANNING_COLUMN;
				updateHighlight();
				mTeklaIMEHelper.redrawSoftKeyboard(mKeyboardView);
				break;
			case SCANNING_COLUMN:
				int scanItemCountMax;
				firstKeyIndex = firstKeyPointers.get(mScanRowCount).intValue();
				// if we are in the last row...
				if(mScanRowCount + 1 == firstKeyPointers.size())
					scanItemCountMax = kl.size() - firstKeyIndex;
				else
					scanItemCountMax = firstKeyPointers.get(mScanRowCount+1).intValue() - firstKeyIndex;
				sel = firstKeyIndex + (mScanItemCount%scanItemCountMax);
				processSelection(kl.get(sel).codes[0], null);
				if (mKeyboardType == KeyboardType.HARD_KEYS)
					mScanState = ScanState.SCANNING_COLUMN;
					// Because it only has one row
				else {
					mScanState = ScanState.SCANNING_ROW;
					focusFirst();
				}
				break;
		}
	}
	
	public void stepBack() {
		if (mScanState == ScanState.SCANNING_COLUMN) {
			clearAllHighlights();
			mScanState = ScanState.SCANNING_ROW;
			mScanItemCount = mScanRowCount;
			updateHighlight();
			mTeklaIMEHelper.redrawSoftKeyboard(mKeyboardView);
		}
	}

	public void focusNext() {
		clearAllHighlights();
		++mScanItemCount;
		updateHighlight();
		mTeklaIMEHelper.redrawSoftKeyboard(mKeyboardView);
	};

	public void focusPrev() {
		clearAllHighlights();
		--mScanItemCount;
		updateHighlight();
		mTeklaIMEHelper.redrawSoftKeyboard(mKeyboardView);
	};

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
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

	// Switch Event Provider Intents will be processed here
	private BroadcastReceiver sepBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			pm.userActivity(SystemClock.uptimeMillis(), true);
			Bundle extras = intent.getExtras();
			switch(extras.getInt(SwitchEventProvider.EXTRA_SWITCH_EVENT)) {
				case SwitchEventProvider.SWITCH_FWD:
					selectFocused();
					break;
				case SwitchEventProvider.SWITCH_BACK:
					stepBack();
					break;
				case SwitchEventProvider.SWITCH_RIGHT:
					focusNext();
					break;
				case SwitchEventProvider.SWITCH_LEFT:
					focusPrev();
					break;
				}
		}
	};
	
	private void startSwitchEventProvider() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Intent sepIntent = new Intent(SwitchEventProvider.INTENT_START_SERVICE);
				sepIntent.putExtra(SwitchEventProvider.EXTRA_SHIELD_MAC, mTeklaIMEHelper.retrieveShieldMac());
				startSEPService(sepIntent);
			}
			
		});
		thread.start();
	}
    
	private void stopSwitchEventProvider() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Intent sepIntent = new Intent(SwitchEventProvider.INTENT_START_SERVICE);
				stopSEPService(sepIntent);
			}
			
		});
		thread.start();
	}
	
	private Boolean startSEPService(Intent sepIntent) {
		return startService(sepIntent) == null? false:true;
	}
	
	private Boolean stopSEPService(Intent sepIntent) {
		return stopService(sepIntent);
	}
	
	private void updateHighlight() {
		Keyboard kb = mKeyboardView.getKeyboard();
		List<Key> kl = kb.getKeys();
		Key k;
		int firstKeyIndex, lastKeyIndex;

		switch(mScanState) {
			case SCANNING_ROW:
				if (mScanItemCount < 0) mScanItemCount = firstKeyPointers.size() - 1;
				firstKeyIndex = firstKeyPointers.get(mScanItemCount%firstKeyPointers.size()).intValue();
				// if we are in the last row...
				if((mScanItemCount+1)%firstKeyPointers.size()==0)
					lastKeyIndex = kl.size();
				else
					lastKeyIndex = firstKeyPointers.get((mScanItemCount+1)%firstKeyPointers.size()).intValue();
				for(int i=firstKeyIndex; i<lastKeyIndex; ++i) {
					k = kl.get(i);
					k.pressed = true;
				}
				break;
			case SCANNING_COLUMN:
				int scanItemCountMax;
				firstKeyIndex = firstKeyPointers.get(mScanRowCount%firstKeyPointers.size()).intValue();
				// if we are in the last row...
				if((mScanRowCount+1)%firstKeyPointers.size()==0)
					scanItemCountMax = kl.size() - firstKeyIndex;
				else
					scanItemCountMax = firstKeyPointers.get((mScanRowCount+1)%firstKeyPointers.size()).intValue() - firstKeyIndex;
				if (mScanItemCount < 0) mScanItemCount = scanItemCountMax - 1;

				k = kl.get(firstKeyPointers.get(mScanRowCount).intValue()+(mScanItemCount%scanItemCountMax));
				k.pressed = true;
				break;				
			}
	}
		
	private void processSelection(int primaryCode, int[] keyCodes) {
		if((primaryCode>=19 && primaryCode<=23) || (primaryCode == KeyEvent.KEYCODE_BACK)) { // UI Navigation 
			keyDownUp(primaryCode);
		} else {
			if (isWordSeparator(primaryCode)) {
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
	        } else if (primaryCode == Keyboard.KEYCODE_CANCEL && mQwertyKeyboard != null) {
	            if (mKeyboardType == KeyboardType.HARD_KEYS) {
	            	showKeyboard(KeyboardType.QWERTY, null);
	            } else {
	            	showKeyboard(KeyboardType.HARD_KEYS, null);
	            }
	        } else if (primaryCode == TeklaKeyboardView.KEYCODE_OPTIONS) {
	            // Show a menu or somethin'
	        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
	                && mQwertyKeyboard != null) {
	            if (mKeyboardType == KeyboardType.SYMBOLS || mKeyboardType == KeyboardType.SYMBOLS_SHIFT) {
	            	showKeyboard(KeyboardType.QWERTY, null);
	            } else {
	            	showKeyboard(KeyboardType.SYMBOLS, null);
	            }
	        } else {
	            handleCharacter(primaryCode, keyCodes);
	        }
		}
	}

	private void showKeyboard(KeyboardType type, Integer imeOptions) {
		TeklaKeyboard curKeyboard = null;
		switch (type) {
			case HARD_KEYS:
				mScanState = ScanState.SCANNING_COLUMN;
				curKeyboard = mHardKeysKeyboard;
				break;
			case QWERTY:
	            mQwertyKeyboard.setShifted(false);
				mScanState = ScanState.SCANNING_ROW;
				curKeyboard = mQwertyKeyboard;
				break;
			case SYMBOLS:
	            mSymbolsShiftKeyboard.setShifted(false);
	            mSymbolsKeyboard.setShifted(false);
				mScanState = ScanState.SCANNING_ROW;
				curKeyboard = mSymbolsKeyboard;
				break;
			case SYMBOLS_SHIFT:
	            mSymbolsKeyboard.setShifted(true);
	            mSymbolsShiftKeyboard.setShifted(true);
				mScanState = ScanState.SCANNING_ROW;
				curKeyboard = mSymbolsShiftKeyboard;
				break;
		}
		if (imeOptions != null)
			curKeyboard.setImeOptions(getResources(), imeOptions);
		if (curKeyboard != null) {
			mKeyboardView.setKeyboard(curKeyboard);
			mKeyboardType = type;
			setFirstKeyPointers(type);
			updateHighlight();
			if (type != KeyboardType.HARD_KEYS)
				focusFirst();
		}
	}

	private void focusFirst() {
		mScanItemCount = 0;
		mScanRowCount = 0;
		updateHighlight();
		mTeklaIMEHelper.redrawSoftKeyboard(mKeyboardView);
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
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mKeyboardView.setShifted(mCapsLock || !mKeyboardView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
        	showKeyboard(KeyboardType.SYMBOLS_SHIFT, null);
        } else if (currentKeyboard == mSymbolsShiftKeyboard) {
        	showKeyboard(KeyboardType.SYMBOLS, null);
        }
    }
    
    private void setFirstKeyPointers(KeyboardType type) {
    	switch (type) {
	    	case HARD_KEYS:
	        	firstKeyPointers = new ArrayList<Integer>();
	        	firstKeyPointers.add(new Integer(0));
	    		break;
	    	case QWERTY:
	        	firstKeyPointers = new ArrayList<Integer>();
	        	firstKeyPointers.add(new Integer(0));
	        	firstKeyPointers.add(new Integer(10));
	        	firstKeyPointers.add(new Integer(19));
	        	firstKeyPointers.add(new Integer(28));
	    		break;
	    	case SYMBOLS:
	        	firstKeyPointers = new ArrayList<Integer>();
	        	firstKeyPointers.add(new Integer(0));
	        	firstKeyPointers.add(new Integer(10));
	        	firstKeyPointers.add(new Integer(20));
	        	firstKeyPointers.add(new Integer(29));
	    		break;
	    	case SYMBOLS_SHIFT:
	        	firstKeyPointers = new ArrayList<Integer>();
	        	firstKeyPointers.add(new Integer(0));
	        	firstKeyPointers.add(new Integer(10));
	        	firstKeyPointers.add(new Integer(20));
	        	firstKeyPointers.add(new Integer(29));
	    		break;
	    }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mQwertyKeyboard.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (mTeklaIMEHelper.isAlphabet(primaryCode) && mPredictionOn) {
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
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null 
                && mKeyboardView != null && mQwertyKeyboard == mKeyboardView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mKeyboardView.setShifted(mCapsLock || caps != 0);
        }
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
    
	private void clearAllHighlights() {
		Keyboard kb = mKeyboardView.getKeyboard();
		List<Key> kl = kb.getKeys();
		
		for (int i=0;i < kl.size();i++) {
			kl.get(i).pressed = false;
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
