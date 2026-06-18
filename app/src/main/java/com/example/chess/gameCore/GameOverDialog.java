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

public class GameOverDialog {

    public static void show(
            Context context,
            String title,
            String icon,
            String message,
            String stats,
            Runnable onExit
    ) {
        // Создаем диалог
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false); // Не закрывать по нажатию вне окна
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
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvIcon = dialog.findViewById(R.id.tvIcon);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        TextView tvStats = dialog.findViewById(R.id.tvStats);
        Button btnExit = dialog.findViewById(R.id.btnExit);

        // Устанавливаем данные
        tvTitle.setText(title);
        tvIcon.setText(icon);
        tvMessage.setText(message);
        tvStats.setText(stats);

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
