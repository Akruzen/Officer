package com.akruzen.officer.views.dialog;

import android.content.DialogInterface;

import androidx.annotation.Nullable;

public interface IMaterialDialogActionsCallback {
    default void onPositiveClick(@Nullable DialogInterface dialogInterface) {
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
    }
    default void onNegativeClick(@Nullable DialogInterface dialogInterface) {
        if (dialogInterface != null) {
            dialogInterface.dismiss();
        }
    }
}
