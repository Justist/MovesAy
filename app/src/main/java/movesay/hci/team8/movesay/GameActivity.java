package movesay.hci.team8.movesay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import movesay.hci.team8.movesay.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class GameActivity extends Activity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
	private String textMatch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_game);
		if(checkVoiceRecognition()) {speak();}
    }

	public void speak() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Specify the calling package to identify your application
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
		// Given an hint to the recognizer about what the user is going to say
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		// Specify how many results you want to receive. The results will be
		// sorted where the first result is the one with higher confidence.
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE)
			//If Voice recognition is successful then it returns RESULT_OK
			if(resultCode == RESULT_OK && data != null) {
				ArrayList<String> textMatchList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if (!textMatchList.isEmpty()) {
					textMatch = textMatchList.get(0);
					showToastMessage("You said: "+ textMatch);
				}
			//Result codes for various errors.
			} else if(resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
				showToastMessage("Audio Error");
			} else if(resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
				showToastMessage("Client Error");
			} else if(resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
				showToastMessage("Network Error");
			} else if(resultCode == RecognizerIntent.RESULT_NO_MATCH) {
				showToastMessage("No Match");
			} else if(resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
				showToastMessage("Server Error");
			}
		super.onActivityResult(requestCode, resultCode, data);
		speak();
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
}
