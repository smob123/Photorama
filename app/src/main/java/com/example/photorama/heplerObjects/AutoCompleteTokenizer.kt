package com.example.photorama.heplerObjects

import android.widget.MultiAutoCompleteTextView

/**
 * @author Sultan
 * Custom tokenizer for the auto complete text view, that handles when suggestions should be shown.
 */

class AutoCompleteTokenizer : MultiAutoCompleteTextView.Tokenizer {

    override fun findTokenStart(p0: CharSequence?, cursorPosition: Int): Int {
        if (p0 != null) {
            // check if the cursor's position is at least 2
            if (cursorPosition < 2) {
                return 0
            }

            // loop from the cursor's position backwards to find the index
            // of any of the mention symbols and return it
            for (i in (cursorPosition - 1) downTo 0) {
                if (p0[i] == '@' || p0[i] == '#') {
                    return i
                }
            }
        }

        return cursorPosition
    }

    override fun findTokenEnd(p0: CharSequence?, cursorPosition: Int): Int {
        return cursorPosition
    }

    override fun terminateToken(selection: CharSequence?): CharSequence {
        if (selection != null) {
            // return the user's selection, with a space after it
            return "$selection "
        }

        return ""
    }
}
