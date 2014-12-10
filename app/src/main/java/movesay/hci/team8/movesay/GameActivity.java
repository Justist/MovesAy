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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import movesay.hci.team8.movesay.util.SnakeView;


public class GameActivity extends Activity {
	public TextView textMatch;

	private SpeechRecognizer mSpeechRecognizer;
	private Intent mSpeechRecognizerIntent;

	private SnakeView mSnakeView;
	private static String ICICLE_KEY = "snakeview";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.snake_layout);
		//setContentView(R.layout.activity_game);

		textMatch = (TextView) findViewById(R.id.result);

		mSnakeView = (SnakeView) findViewById(R.id.snake);
		mSnakeView.setTextView((TextView) findViewById(R.id.text));

		if (savedInstanceState == null) {
			// We were just launched -- set up a new game
			mSnakeView.setMode(SnakeView.READY);
		} else {
			// We are being restored
			Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
			if (map != null) {
				mSnakeView.restoreState(map);
			} else {
				mSnakeView.setMode(SnakeView.PAUSE);
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

	public boolean checkVoiceRecognition() {
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

	public void showToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Pause the game along with the activity
		mSnakeView.setMode(SnakeView.PAUSE);
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		//Store the game state
		outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
	}

	protected class SpeechRecognitionListener implements RecognitionListener {
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
			//textMatch.setText("Nope, I did not get that...");
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onResults(Bundle results) {
			ArrayList<String> textMatchList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			if (!textMatchList.isEmpty()) {
				mSnakeView.recognizeDirection(textMatchList.get(0).toLowerCase());
				//showToastMessage(textMatchList.get(0).toLowerCase());
			}
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onPartialResults(Bundle partialResults) {}

		@Override
		public void onEvent(int eventType, Bundle params) {}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mSpeechRecognizer != null) {mSpeechRecognizer.destroy();}
	}
}
