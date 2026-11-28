package org.coughlin.grocerylist;

/**
 * A note the user shared into the app (for example from Google Keep) for the AI
 * prompt builder to draw on. Plain Java so {@link PromptBuilder} stays Android-free.
 */
public final class ReferenceNote {
    public final String id;
    public final String title;
    public final String text;
    public final long savedAt;

    public ReferenceNote(String id, String title, String text, long savedAt) {
        this.id = id;
        this.title = title == null ? "" : title;
        this.text = text == null ? "" : text;
        this.savedAt = savedAt;
    }

    /** Title to show the user; falls back to the first line of the note. */
    public String displayTitle() {
        if (!title.trim().isEmpty()) return title.trim();
        String firstLine = text.trim().split("\\R", 2)[0].trim();
        if (firstLine.length() > 30) firstLine = firstLine.substring(0, 30) + "…";
        return firstLine.isEmpty() ? "Untitled note" : firstLine;
    }
}
