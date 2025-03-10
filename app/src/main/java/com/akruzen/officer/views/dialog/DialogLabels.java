package com.akruzen.officer.views.dialog;

import androidx.annotation.Nullable;

public final class DialogLabels {
    private String title = "title";
    private String message = "message";
    private String positiveText = null;
    private String negativeText = null;
    private IMaterialDialogActionsCallback callback;
    public DialogLabels setTitle(String title) {
        this.title = title;
        return this;
    }

    public DialogLabels setMessage(String message) {
        this.message = message;
        return this;
    }

    public DialogLabels setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public DialogLabels setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public DialogLabels setCallback(IMaterialDialogActionsCallback callback) {
        this.callback = callback;
        return this;
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    @Nullable
    public String getPositiveText() { return positiveText; }
    @Nullable
    public String getNegativeText() { return negativeText; }
    @Nullable
    public IMaterialDialogActionsCallback getCallback() { return callback; }
}
