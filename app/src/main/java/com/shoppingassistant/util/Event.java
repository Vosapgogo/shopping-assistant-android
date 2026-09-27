package com.shoppingassistant.util;

/**
 * A value for LiveData that should be acted on once (a toast, a redirect), not again every time
 * a screen starts observing.
 */
public class Event<T> {

    private final T content;
    private boolean handled;

    public Event(T content) {
        this.content = content;
    }

    /** The content the first time it's asked for, null afterwards. */
    public synchronized T getContentIfNotHandled() {
        if (handled) {
            return null;
        }
        handled = true;
        return content;
    }
}
