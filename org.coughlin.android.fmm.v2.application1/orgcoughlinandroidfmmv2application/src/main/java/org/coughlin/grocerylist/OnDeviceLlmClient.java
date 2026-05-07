package org.coughlin.grocerylist;

import android.content.Context;

/**
 * Stub for on-device LLM (Gemini Nano via Google AI Edge SDK).
 *
 * To enable in a future release:
 *   1. Add the AI Edge SDK dependency to build.gradle (verify the current
 *      artifact coordinates; the API moved during 2024-2025).
 *   2. In isAvailable(), check whether GenerativeModel can be initialized
 *      on the current device (Gemini Nano requires Pixel 8 Pro / Pixel 9 /
 *      Galaxy S24+ class hardware with AICore installed).
 *   3. In generate(), call GenerativeModel.generateContent(prompt) and
 *      return the text result. Wrap the async call in a blocking get()
 *      so the existing synchronous worker contract is preserved.
 */
public final class OnDeviceLlmClient {

    private OnDeviceLlmClient() {}

    public static boolean isAvailable(Context context) {
        return false;
    }

    public static String generate(Context context, String prompt) throws Exception {
        throw new Exception("On-device LLM is not available on this device.");
    }
}
