package movesay.hci.team8.movesay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import movesay.hci.team8.movesay.util.CharacterView;


public class GameActivity extends Activity {

	private SpeechRecognizer mSpeechRecognizer;
	private Intent mSpeechRecognizerIntent;

	private CharacterView mCharacterView;
	private static final String ICICLE_KEY = "CharacterView";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.character_layout);

		mCharacterView = (CharacterView) findViewById(R.id.character);
		mCharacterView.setTextView((TextView) findViewById(R.id.text));

		if (savedInstanceState == null) {
			// We were just launched -- set up a new game
			mCharacterView.setMode(CharacterView.READY);
		} else {
			// We are being restored
			Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
			if (map != null) {
				mCharacterView.restoreState(map);
			} else {
				mCharacterView.setMode(CharacterView.PAUSE);
			}
		}

		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
													RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		SpeechRecognitionListener listener = new SpeechRecognitionListener(); mSpeechRecognizer.setRecognitionListener(listener);
		if(checkVoiceRecognition()) {mSpeechRecognizer.startListening(mSpeechRecognizerIntent);}
    }

	boolean checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
			RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	/*
	 * For giving feedback to a user.
	 */
	void showToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Pause the game along with the activity
		mCharacterView.setMode(CharacterView.PAUSE);
		mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		//Store the game state
		outState.putBundle(ICICLE_KEY, mCharacterView.saveState());
	}

	class SpeechRecognitionListener implements RecognitionListener {
		private boolean lastCommandWasWalk = false;

		@Override
		public void onReadyForSpeech(Bundle params) {}

		@Override
		public void onBeginningOfSpeech() {}

		@Override
		public void onRmsChanged(float rmsdB) {}

		@Override
		public void onBufferReceived(byte[] buffer) {}

		@Override
		public void onEndOfSpeech() {}

		@Override
		public void onError(int error) {
			//Log.d("GameActivity", "Error!");
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		/**
		 * This function will try to recognize what is said as good as possible.
		 * Therefore the starting letter of the recognized word will be used to
		 * determine which command was said. As all command have another starting
		 * letter, this will work nicely.
		 *
		 */
		private boolean recognizeSpeech(String original) {
			Log.d("GameActivity", original);
			String result = "";
			if(lastCommandWasWalk) {
				result = "N" + original;
				if(mCharacterView.recognizeDirection(result)) {lastCommandWasWalk = false;}
			} else {
				String firstCharacter = String.valueOf(original.charAt(0));
				String secondCharacter = String.valueOf(original.charAt(1));
				if(firstCharacter.equals("u")) {result = "up";}
				else if(firstCharacter.equals("d")) {result = "down";}
				else if(firstCharacter.equals("t")) {result = "down";}
				else if(firstCharacter.equals("r")) {
					if(secondCharacter.equals("e")) {result = "preferences";}
					else {result = "right";}
				}
				else if(firstCharacter.equals("l")) {result = "left";}
				else if(firstCharacter.equals("s")) {result = "start";}
				else if(firstCharacter.equals("p")) {
					if(secondCharacter.equals("r")) {result = "preferences";}
					else {result = "pause";}
				}
				else if(firstCharacter.equals("b")) {result = "pause";}
				else if(firstCharacter.equals("w")) {
					result = "walk";
					if(mCharacterView.getControlChoice()) {lastCommandWasWalk = true;}
				}
				else if(firstCharacter.equals("q")) {result = "quit";}
				else if(firstCharacter.equals("c")) {result = "change";}
				mCharacterView.recognizeDirection(result);
			}
			if(result.equals("quit")) {quit(); return false;}
			return true;
		}

		@Override
		public void onResults(Bundle results) {
			String result;
			ArrayList<String> textMatchList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			if (!textMatchList.isEmpty()) {
				result = textMatchList.get(0);
				if(!recognizeSpeech(result)) {return;}
				showToastMessage(result);
			}
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			Log.d("partial", "Hi mom!");
			//mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			Log.d("event", String.valueOf(eventType));
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}
	}

	// Welll, it quits. Maybe somewhat less errory would be nice though.
	void quit() {
		mSpeechRecognizerIntent = null;
		mSpeechRecognizer.destroy();
		mCharacterView = null;
		finish();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mSpeechRecognizer != null) {mSpeechRecognizer.destroy();}
	}
}
