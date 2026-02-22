package com.trainee.reciptai.Service;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;

@Service
public class SpeechService {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String speechRegion;

    public String captureItemByVoice() throws InterruptedException, ExecutionException {
        SpeechConfig config = SpeechConfig.fromSubscription(speechKey, speechRegion);
        AudioConfig audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        SpeechRecognizer recognizer = new SpeechRecognizer(config, audioConfig);

        System.out.println("Listening... Speak your shopping item clearly.");

        // This waits for you to finish speaking one sentence
        SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();

        if (result.getReason() == ResultReason.RecognizedSpeech) {
            return result.getText().replace(".", ""); // Remove the full stop at the end
        } else {
            return "Error: Could not understand speech.";
        }
    }
}