package movesay.hci.team8.movesay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends Activity {
	private String textMatch;

	private SpeechRecognizer mSpeechRecognizer;
	private Intent mSpeechRecognizerIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);

		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
													RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication().getPackageName());
		mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		SpeechRecognitionListener listener = new SpeechRecognitionListener(); mSpeechRecognizer.setRecognitionListener(listener);
		if(checkVoiceRecognition()) {mSpeechRecognizer.startListening(mSpeechRecognizerIntent);}
    }

	void showToastMessage(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	public boolean checkVoiceRecognition() {
		// Check if voice recognition is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
			RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			Toast.makeText(this, "Voice recognizer not present", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
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
			//showToastMessage("error" + error);
			mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
		}

		@Override
		public void onResults(Bundle results) {
			ArrayList<String> textMatchList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			if (!textMatchList.isEmpty()) {
				textMatch = textMatchList.get(0);
				showToastMessage("You said: "+ textMatch);
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
