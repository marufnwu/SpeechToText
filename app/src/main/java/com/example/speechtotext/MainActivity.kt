package com.example.speechtotext

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val RECORD_AUDIO_PERMISSION_CODE: Int = 10001;
    lateinit var speechRecognizer: SpeechRecognizer
    lateinit var btnSpeak:Button
    lateinit var indicator:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSpeak = findViewById(R.id.btnSpeak)
        indicator = findViewById(R.id.indicator)
        speechRecognizer =  SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechToText"  , "onReadyForSpeech: ")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechToText"  , "onBeginningOfSpeech: ")

            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("SpeechToText"  , "onRmsChanged: "+rmsdB)
                val micAnimation = indicator.background as AnimationDrawable

                val Db = Math.abs(rmsdB)
                if(Db>5){
                    micAnimation.selectDrawable(1)
                }else{
                    micAnimation.selectDrawable(0)
                }


                //indicator.scaleX = Db

            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("SpeechToText"  , "onBufferReceived: ")

            }

            override fun onEndOfSpeech() {
                val micAnimation = indicator.background as AnimationDrawable
                micAnimation.stop()
                Log.d("SpeechToText"  , "onEndOfSpeech: ")

            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error: Microphone not working or unavailable."
                    SpeechRecognizer.ERROR_CLIENT -> "Client error: Invalid parameters or incorrect usage of SpeechRecognizer API."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions: RECORD_AUDIO permission not granted."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error: No internet connection or network issues."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout: Slow network response or server issues."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match: No recognition results found."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy: Unable to process the request due to another ongoing process."
                    SpeechRecognizer.ERROR_SERVER -> "Server error: Speech recognition server issues."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout: No speech input detected within the allowed time."
                    else -> "Unknown error occurred."
                }

                Log.d("SpeechToText", "onError: "+errorMessage)
            }

            override fun onResults(results: Bundle?) {
                val resultList = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!resultList.isNullOrEmpty()) {
                    val recognizedText = resultList[0]
                    // Do something with the recognizedText.

                    Log.d("SpeechToText", "onResults: "+recognizedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("SpeechToText"  , "onPartialResults: ")

            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SpeechToText"  , "onEvent: ")

            }

        })


        btnSpeak.setOnClickListener(View.OnClickListener {
          speak()
        })
    }
    private fun isRecordAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }
    private fun speak() {
        if(!isRecordAudioPermissionGranted()){
            requestRecordAudioPermission()
            return
        }


        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
        //intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, Locale("bn-BD"))
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        val micAnimation = indicator.background as AnimationDrawable
        micAnimation.start()

        speechRecognizer.startListening(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with SpeechRecognizer setup.
                speak()
            } else {
                // Permission denied. Handle the situation gracefully, e.g., show an explanation or disable speech recognition features.
            }
        }
    }
}