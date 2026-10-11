package com.jai.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int SPEECH_REQUEST = 1001;
    private TextToSpeech tts;
    private EditText commandInput;
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tts = new TextToSpeech(this, result -> {
            if (result != TextToSpeech.ERROR) tts.setLanguage(Locale.getDefault());
        });
        buildScreen();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 40, 28, 24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(0xFF101426);

        TextView title = new TextView(this);
        title.setText("JAI AI");
        title.setTextSize(34);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 12, 0, 4);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView subtitle = new TextView(this);
        subtitle.setText("Your personal voice assistant");
        subtitle.setTextSize(15);
        subtitle.setTextColor(0xFFB9C4E8);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 0, 0, 28);
        root.addView(subtitle);

        commandInput = new EditText(this);
        commandInput.setSingleLine(false);
        commandInput.setMinLines(2);
        commandInput.setHint("Type a command… e.g. open YouTube");
        commandInput.setTextColor(0xFFFFFFFF);
        commandInput.setHintTextColor(0xFF9BA7C8);
        commandInput.setPadding(18, 14, 18, 14);
        commandInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6C86FF));
        root.addView(commandInput, new LinearLayout.LayoutParams(-1, -2));

        Button run = makeButton("Run command");
        root.addView(run, buttonParams());
        run.setOnClickListener(v -> handleCommand(commandInput.getText().toString()));

        Button voice = makeButton("🎙 Speak a command");
        root.addView(voice, buttonParams());
        voice.setOnClickListener(v -> startVoice());

        Button settings = makeButton("Open phone settings");
        root.addView(settings, buttonParams());
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS)); }
            catch (Exception e) { speak("I could not open settings."); }
        });

        status = new TextView(this);
        status.setText("Ready. Try “open YouTube” or “open settings”.");
        status.setTextColor(0xFFE2E8FF);
        status.setTextSize(15);
        status.setPadding(0, 22, 0, 0);
        root.addView(status);

        setContentView(root);
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        return b;
    }
    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = 12;
        return p;
    }

    private void startVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command");
        try { startActivityForResult(intent, SPEECH_REQUEST); }
        catch (Exception e) { status.setText("Voice input is not available on this phone."); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                commandInput.setText(results.get(0));
                handleCommand(results.get(0));
            }
        }
    }

    private void handleCommand(String raw) {
        String cmd = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (cmd.isEmpty()) { status.setText("Please type or speak a command."); return; }
        if (cmd.contains("youtube")) {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            if (launch != null) startActivity(launch);
            else startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.youtube.com")));
            status.setText("Opening YouTube…");
            speak("Opening YouTube");
        } else if (cmd.contains("settings") || cmd.contains("setting")) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            status.setText("Opening phone settings…");
            speak("Opening settings");
        } else if (cmd.contains("hello") || cmd.contains("hi jai")) {
            status.setText("Hello! I am JAI AI. Try “open YouTube” or “open settings”.");
            speak("Hello! I am JAI AI. What can I help you with?");
        } else {
            status.setText("I can do basic commands right now: open YouTube, open settings, or say hello. AI chat will need an online AI service setup.");
            speak("I can open YouTube or settings. AI chat needs an online service setup.");
        }
    }

    private void speak(String text) {
        if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jai_ai_utterance");
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}
