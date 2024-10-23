package com.sal.leseniyashuleyasabato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class dialogwindow {

    public static void showErrorDialog(Context context, String title, String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(errorMessage)
                .setCancelable(false) // User cannot dismiss the dialog by tapping outside
                .setPositiveButton("sawa", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss(); // Close the dialog when OK is pressed
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}