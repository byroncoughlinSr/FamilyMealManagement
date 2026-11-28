package org.coughlin.grocerylist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Receives text shared from Google Keep (or any app) through the Android share
 * sheet and saves it as a note the AI prompt builder in Settings can draw on.
 * Has no UI of its own: it saves, shows a toast, and opens Settings.
 */
public class SharedNoteReceiverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            CharSequence subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT);
            ReferenceNote note = ReferenceNoteStore.add(this,
                    subject == null ? null : subject.toString(),
                    text == null ? null : text.toString());
            if (note == null) {
                Toast.makeText(this, R.string.shared_note_empty, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.shared_note_saved, note.displayTitle()),
                        Toast.LENGTH_LONG).show();
                Intent settings = new Intent(this, SettingsActivity.class);
                settings.putExtra(SettingsActivity.EXTRA_SCROLL_TO_PROMPTS, true);
                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(settings);
            }
        }
        finish();
    }
}
