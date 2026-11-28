package org.coughlin.grocerylist;

import java.util.List;
import java.util.Locale;

/**
 * Builds the request that asks the user's chosen AI to write a menu or recipe
 * prompt from what they said into the mic, the field's current text, and any notes
 * they shared in from Google Keep. Also cleans up the AI's reply.
 * Pure Java with no Android dependencies, so it is easy to unit test.
 */
public final class PromptBuilder {

    public enum Kind { MENU, RECIPE }

    /** Upper bound on note text sent to the AI, to keep the request small. */
    static final int MAX_NOTES_CHARS = 12000;

    private PromptBuilder() { }

    public static String build(Kind kind, String spoken, String currentPrompt,
                               List<ReferenceNote> notes) {
        StringBuilder sb = new StringBuilder();
        sb.append("You write the preferences text for a family meal-planning AI.\n\n");

        if (kind == Kind.MENU) {
            sb.append("The text you write is added to the end of a request that already lists ")
              .append("the dates to plan and the exact JSON format to return. So write ONLY the ")
              .append("family's meal preferences: which meals or proteins, how often, dietary ")
              .append("rules, side dishes, and ingredients they already have and want to use up. ")
              .append("Do NOT mention JSON, output formats, or specific dates. ")
              .append("If the user wants to skip a meal, say it plainly, e.g. \"No lunches this week.\"\n")
              .append("Example of the style wanted: \"")
              .append(AppSettings.DEFAULT_MENU_PROMPT).append("\"\n\n");
        } else {
            sb.append("The text you write is added to the end of a request that asks for recipes ")
              .append("(ingredients and steps) for planned meals and already specifies the JSON ")
              .append("format. So write ONLY the recipe requirements: style, number of ingredients, ")
              .append("number and format of steps, ingredients to prefer or avoid. ")
              .append("Do NOT mention JSON or output formats.\n")
              .append("Example of the style wanted: \"")
              .append(AppSettings.DEFAULT_RECIPE_PROMPT).append("\"\n\n");
        }

        String current = currentPrompt == null ? "" : currentPrompt.trim();
        if (!current.isEmpty()) {
            sb.append("CURRENT TEXT (change it as the request says; keep parts the request ")
              .append("does not change, unless the request asks to start over):\n")
              .append(current).append("\n\n");
        }

        sb.append("WHAT THE USER SAID:\n").append(spoken == null ? "" : spoken.trim()).append("\n\n");

        if (notes != null && !notes.isEmpty()) {
            sb.append("SAVED NOTES (shared from the user's notes app). Decide which notes, if any, ")
              .append("matter for the request. A note may be an example prompt (copy its style ")
              .append("and rules) or a list of groceries the family has (prefer meals that use ")
              .append("those items). Ignore notes that do not apply.\n");
            int budget = MAX_NOTES_CHARS;
            for (ReferenceNote n : notes) {
                if (budget <= 0) break;
                String text = n.text.length() > budget ? n.text.substring(0, budget) : n.text;
                budget -= text.length();
                sb.append("--- Note: ").append(n.displayTitle()).append(" ---\n")
                  .append(text).append("\n");
            }
            sb.append("--- End of notes ---\n\n");
        }

        sb.append("Reply with ONLY the new text, under 120 words, plain sentences, ")
          .append("no heading, no markdown, no quotes, no explanation.");
        return sb.toString();
    }

    /** Strips code fences, quotes, and "Here is…" lead-ins from the AI's reply. */
    public static String cleanResponse(String response) {
        if (response == null) return "";
        String s = response.trim();
        s = s.replaceAll("(?s)^```[a-zA-Z]*\\s*", "").replaceAll("(?s)\\s*```$", "").trim();
        // Drop a lead-in line such as "Here is the updated prompt:".
        String[] lines = s.split("\\R", 2);
        if (lines.length == 2) {
            String first = lines[0].trim().toLowerCase(Locale.ROOT);
            if ((first.startsWith("here is") || first.startsWith("here's")
                    || first.startsWith("sure")) && first.endsWith(":")) {
                s = lines[1].trim();
            }
        }
        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).trim();
        }
        return s;
    }
}
