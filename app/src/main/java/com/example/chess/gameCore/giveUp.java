package com.example.chess.gameCore;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.chess.R;

public class giveUp {
    public static Dialog show(
            Context context,
            Runnable onExit
    ) {
        // Создаем диалог
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.giveup);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Настраиваем окно
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Находим View
        Button gubtnExit = dialog.findViewById(R.id.gubtnExit);
        Button guCancel = dialog.findViewById(R.id.guCancel);

        // Обработчик кнопки
        gubtnExit.setOnClickListener(v -> {
            dialog.dismiss();
            if (onExit != null) {
                onExit.run();
            }
        });

        guCancel.setOnClickListener(v -> {
            dialog.dismiss();

        });

        dialog.show();
        return dialog;
    }
}
