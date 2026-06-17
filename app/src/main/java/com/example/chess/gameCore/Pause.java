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

public class Pause {
    public static void show(
            Context context,
            String message,
            Runnable onExit
    ) {
        // Создаем диалог
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.pause);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Настраиваем окно
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.CENTER;
            window.setAttributes(params);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Находим View
        TextView tvTitle = dialog.findViewById(R.id.pauseTitle);
        TextView tvMessage = dialog.findViewById(R.id.pauseMessage);
        Button btnExit = dialog.findViewById(R.id.btnExit);

        // Устанавливаем данные
        tvMessage.setText(message);

        // Обработчик кнопки
        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            if (onExit != null) {
                onExit.run();
            }
        });

        dialog.show();
    }
}
