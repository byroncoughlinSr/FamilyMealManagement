package org.coughlin.grocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ProviderSetupActivity extends AppCompatActivity {

    private static final class Choice {
        final String providerKey;
        final int nameRes;
        final int descRes;
        Choice(String key, int name, int desc) {
            this.providerKey = key;
            this.nameRes = name;
            this.descRes = desc;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_setup);

        LinearLayout container = findViewById(R.id.providerListContainer);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Choice c : buildChoices()) {
            View card = inflater.inflate(R.layout.item_provider_choice, container, false);
            ((TextView) card.findViewById(R.id.providerName)).setText(c.nameRes);
            ((TextView) card.findViewById(R.id.providerDesc)).setText(c.descRes);
            card.setOnClickListener(v -> {
                AppSettings.setLlmProvider(this, c.providerKey);
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
            });
            container.addView(card);
        }
    }

    private List<Choice> buildChoices() {
        List<Choice> choices = new ArrayList<>();
        if (OnDeviceLlmClient.isAvailable(this)) {
            choices.add(new Choice(AppSettings.PROVIDER_ON_DEVICE,
                    R.string.provider_on_device_name, R.string.provider_on_device_desc));
        }
        choices.add(new Choice(AppSettings.PROVIDER_OLLAMA,
                R.string.provider_ollama_name, R.string.provider_ollama_desc));
        choices.add(new Choice(AppSettings.PROVIDER_CLAUDE,
                R.string.provider_claude_name, R.string.provider_claude_desc));
        choices.add(new Choice(AppSettings.PROVIDER_OPENAI,
                R.string.provider_openai_name, R.string.provider_openai_desc));
        choices.add(new Choice(AppSettings.PROVIDER_GEMINI,
                R.string.provider_gemini_name, R.string.provider_gemini_desc));
        choices.add(new Choice(AppSettings.PROVIDER_GROK,
                R.string.provider_grok_name, R.string.provider_grok_desc));
        choices.add(new Choice(AppSettings.PROVIDER_GROQ,
                R.string.provider_groq_name, R.string.provider_groq_desc));
        return choices;
    }
}
