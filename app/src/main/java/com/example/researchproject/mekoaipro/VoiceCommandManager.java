package com.example.researchproject.mekoaipro;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;


import com.example.researchproject.History.OrderHistoryActivity;
import com.example.researchproject.Notification.NotificationActivity;
import com.example.researchproject.ThanksActivity;
import com.example.researchproject.guide.UserGuideActivity;


import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;


import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;
import ai.picovoice.porcupine.PorcupineException;


import java.util.ArrayList;
import java.util.Locale;
public class VoiceCommandManager {
    public interface VoiceCommandListener {
        void onNavigateToHome();
        void onNavigateToMessageAI();
        void onNavigateToCart();
        void onNavigateToNews();
        void onNavigateToSettings();
        void onUnknownCommand(String command);
    }
    private Context context;
    private VoiceCommandListener listener;
    private SpeechRecognizer speechRecognizer;
    private PorcupineManager porcupineManager;
    public VoiceCommandManager(Context context, VoiceCommandListener listener) {
        this.context = context;
        this.listener = listener;
        setupPorcupine();
        setupSpeechRecognizer();
    }
    public void setupPorcupine() {
        try {
            PorcupineManagerCallback callback = new PorcupineManagerCallback() {
                @Override
                public void invoke(int keywordIndex) {
                    if (porcupineManager != null) {
                        porcupineManager.start(); // Đảm bảo Porcupine vẫn đang chạy
                    }
                    startListeningWithSpeechRecognizer();
                }
            };


            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey("LKNNJYnY2WEYC5A8hXuVLUeskttDVyNmaSrUq0yovaq0hkgumPnDGw==")
                    .setKeywordPath("hey_meko_android.ppn") // đặt trong assets hoặc raw
                    .build(context, callback); // Truyền đúng tham số


            porcupineManager.start();
        } catch (PorcupineException e) {
            Toast.makeText(context, "Lỗi Porcupine: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { }
            @Override public void onBeginningOfSpeech() { }
            @Override public void onRmsChanged(float rmsdB) { }
            @Override public void onBufferReceived(byte[] buffer) { }
            @Override
            public void onEndOfSpeech() {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startListeningWithSpeechRecognizer();
                }, 2000);
            }
            @Override
            public void onError(int error) {
                Toast.makeText(context, "Lỗi nhận diện: " + error, Toast.LENGTH_SHORT).show();

                // Chờ 2 giây rồi khởi động lại nhận diện giọng nói
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    startListeningWithSpeechRecognizer();
                }, 2000);
            }


            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String command = matches.get(0).toLowerCase();
                    handleCommand(command);
                }
            }


            @Override public void onPartialResults(Bundle partialResults) { }
            @Override public void onEvent(int eventType, Bundle params) { }
        });
    }


    public void startListeningWithSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói lệnh của bạn...");
        speechRecognizer.startListening(intent);
    }

    public void handleCommand(String command) {
        if (command.contains("mở trang chủ")) {
            listener.onNavigateToHome();
        } else if (command.contains("trò chuyện với tôi")) {
            listener.onNavigateToMessageAI();
        } else if (command.contains("mở giỏ hàng")) {
            listener.onNavigateToCart();
        } else if (command.contains("mở trang đăng tin")) {
            listener.onNavigateToNews();
        } else if (command.contains("mở thông tin cá nhân")) {
            listener.onNavigateToSettings();
        } else if (command.contains("mở trang hướng dẫn sử dụng")) {
            openActivity(UserGuideActivity.class); // Mở trang hướng dẫn sử dụng
        } else if (command.contains("mở thông báo")) {
            openActivity(NotificationActivity.class); // Mở trang thông báo
        } else if (command.contains("ní ơi")) {
            openActivity(MekoAIPro.class); // Mở trang MekoAIPro
        } else if (command.contains("mở lịch sử đơn hàng")) {
            openActivity(OrderHistoryActivity.class); // Mở lịch sử đơn hàng
        } else if (command.contains("mở cảm ơn")) {
        openActivity(ThanksActivity.class);}
        else {
            listener.onUnknownCommand(command);
        }
    }
    public void destroy() {
        if (porcupineManager != null) {
            try {
                porcupineManager.stop();
                porcupineManager.delete();
            } catch (PorcupineException e) {
                e.printStackTrace();
            }
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(context, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}



