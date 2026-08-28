package com.example.advanceassistant;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.ai.edge.litertlm.Backend;
import com.google.ai.edge.litertlm.Contents;
import com.google.ai.edge.litertlm.Conversation;
import com.google.ai.edge.litertlm.ConversationConfig;
import com.google.ai.edge.litertlm.Engine;
import com.google.ai.edge.litertlm.EngineConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelReasoningEngine {

    private static final String TAG = "ModelReasoningEngine";

    private static final String MODEL_FILE =
            "gemma-4-E2B-it.litertlm";

    /*
     * This is the TOTAL context budget:
     *
     * input tokens + output tokens
     *
     * Do not confuse this with maxOutputToken.
     */
    private static final int MAX_CONTEXT_TOKENS = 2048;

    /*
     * Notification responses are very small.
     */
    private static final int MAX_OUTPUT_TOKENS = 128;

    private final Context context;

    private Engine engine;

    private final ExecutorService inferenceExecutor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private volatile boolean initialized = false;
    private volatile boolean closed = false;

    public ModelReasoningEngine(Context context) {
        this.context =
                context.getApplicationContext();
    }

    public void initialize(
            InitializationCallback callback
    ) {

        inferenceExecutor.execute(() -> {

            long startTime =
                    System.currentTimeMillis();

            try {

                if (closed) {
                    throw new IllegalStateException(
                            "ModelReasoningEngine is closed"
                    );
                }

                String modelPath =
                        getModelPath();

                Log.d(
                        TAG,
                        "Model path: " + modelPath
                );

                EngineConfig engineConfig =
                        new EngineConfig(
                                modelPath,
                                new Backend.CPU(),
                                null,
                                null,
                                MAX_CONTEXT_TOKENS,
                                null,
                                null
                        );

                engine =
                        new Engine(engineConfig);

                engine.initialize();

                initialized = true;

                long elapsed =
                        System.currentTimeMillis()
                                - startTime;

                Log.d(
                        TAG,
                        "Gemma engine loaded in "
                                + elapsed
                                + " ms"
                );

                mainHandler.post(
                        callback::onInitialized
                );

            } catch (Exception e) {

                initialized = false;

                Log.e(
                        TAG,
                        "Failed to load Gemma",
                        e
                );

                mainHandler.post(
                        () -> callback.onError(e)
                );
            }
        });
    }

    public void generate(
            String prompt,
            GenerationCallback callback
    ) {

        if (prompt == null ||
                prompt.trim().isEmpty()) {

            mainHandler.post(
                    () -> callback.onError(
                            new IllegalArgumentException(
                                    "Prompt cannot be empty"
                            )
                    )
            );

            return;
        }

        if (!initialized || engine == null) {

            mainHandler.post(
                    () -> callback.onError(
                            new IllegalStateException(
                                    "Gemma engine not initialized"
                            )
                    )
            );

            return;
        }

        if (closed) {

            mainHandler.post(
                    () -> callback.onError(
                            new IllegalStateException(
                                    "Gemma engine is closed"
                            )
                    )
            );

            return;
        }

        inferenceExecutor.execute(() -> {

            long startTime =
                    System.currentTimeMillis();

            Conversation conversation = null;

            try {

                Log.d(
                        TAG,
                        "Starting Gemma inference..."
                );

                Log.d(
                        TAG,
                        "Prompt length: "
                                + prompt.length()
                                + " characters"
                );

                /*
                 * IMPORTANT:
                 *
                 * Create a NEW conversation for every
                 * notification.
                 *
                 * Notification analysis must be stateless.
                 */
                ConversationConfig config =
                        new ConversationConfig(
                                null,
                                Collections.emptyList(),
                                Collections.emptyList(),
                                null,
                                false,
                                Collections.emptyList(),
                                Collections.emptyMap(),
                                null,
                                false,
                                MAX_OUTPUT_TOKENS,
                                null,
                                false
                        );

                conversation =
                        engine.createConversation(config);

                String response =
                        conversation
                                .sendMessage(prompt)
                                .toString();

                long elapsed =
                        System.currentTimeMillis()
                                - startTime;

                Log.d(
                        TAG,
                        "Inference completed in "
                                + elapsed
                                + " ms"
                );

                Log.d(
                        TAG,
                        "Gemma response: "
                                + response
                );

                final String finalResponse =
                        response;

                mainHandler.post(
                        () -> callback.onResult(
                                finalResponse
                        )
                );

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Gemma inference failed",
                        e
                );

                mainHandler.post(
                        () -> callback.onError(e)
                );

            } finally {

                /*
                 * Destroy the notification-specific
                 * conversation.
                 */
                if (conversation != null) {

                    try {
                        conversation.close();

                        Log.d(
                                TAG,
                                "Notification conversation closed"
                        );

                    } catch (Exception e) {

                        Log.w(
                                TAG,
                                "Failed to close conversation",
                                e
                        );
                    }
                }
            }
        });
    }

    private String getModelPath()
            throws IOException {

        File modelFile =
                new File(
                        context.getFilesDir(),
                        MODEL_FILE
                );

        if (!modelFile.exists()) {

            Log.d(
                    TAG,
                    "Copying Gemma model from assets..."
            );

            try (
                    InputStream inputStream =
                            context.getAssets()
                                    .open(MODEL_FILE);

                    FileOutputStream outputStream =
                            new FileOutputStream(
                                    modelFile
                            )
            ) {

                byte[] buffer =
                        new byte[8192];

                int length;

                while (
                        (length =
                                inputStream.read(buffer))
                                > 0
                ) {

                    outputStream.write(
                            buffer,
                            0,
                            length
                    );
                }

                outputStream.flush();
            }

            Log.d(
                    TAG,
                    "Gemma model copied to: "
                            + modelFile.getAbsolutePath()
            );
        }

        return modelFile.getAbsolutePath();
    }

    public void close() {

        if (closed) {
            return;
        }

        closed = true;
        initialized = false;

        inferenceExecutor.execute(() -> {

            if (engine != null) {

                try {

                    engine.close();

                    Log.d(
                            TAG,
                            "Gemma engine closed"
                    );

                } catch (Exception e) {

                    Log.e(
                            TAG,
                            "Error closing Gemma engine",
                            e
                    );
                }

                engine = null;
            }
        });

        inferenceExecutor.shutdown();
    }

    public interface InitializationCallback {

        void onInitialized();

        void onError(Exception exception);
    }

    public interface GenerationCallback {

        void onResult(String response);

        void onError(Exception exception);
    }
}