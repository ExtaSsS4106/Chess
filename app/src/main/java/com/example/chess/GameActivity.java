package com.example.chess;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private ImageView gameTable;
    private Bitmap boardBitmap;
    private int cellWidth, cellHeight;
    private int boardWidth, boardHeight;

    // Шахматные фигуры
    private ChessPiece[][] pieces = new ChessPiece[8][8];
    private ChessPiece selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isWhiteTurn = true; // Белые ходят первыми

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        gameTable = findViewById(R.id.gameTable);

        gameTable.post(new Runnable() {
            @Override
            public void run() {
                drawGrid();
                initPieces();
                drawAllPieces();
                setupTouchListener();
            }
        });
    }

    private void drawGrid() {
        android.graphics.drawable.Drawable drawable = gameTable.getDrawable();
        Bitmap originalBitmap = ((android.graphics.drawable.BitmapDrawable) drawable).getBitmap();

        boardBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(boardBitmap);

        boardWidth = boardBitmap.getWidth();
        boardHeight = boardBitmap.getHeight();

        cellWidth = boardWidth / 8;
        cellHeight = boardHeight / 8;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2f);

        // Рисуем сетку
        for (int i = 1; i <= 7; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, boardHeight, paint);
            canvas.drawLine(0, i * cellHeight, boardWidth, i * cellHeight, paint);
        }

        gameTable.setImageBitmap(boardBitmap);
    }

    private void initPieces() {
        // Пешки (черные - вверху, белые - внизу)
        for (int i = 0; i < 8; i++) {
            pieces[1][i] = new ChessPiece("pawn", false, 1, i); // Черные пешки
            pieces[6][i] = new ChessPiece("pawn", true, 6, i);  // Белые пешки
        }

        // Ладьи
        pieces[0][0] = new ChessPiece("rook", false, 0, 0);
        pieces[0][7] = new ChessPiece("rook", false, 0, 7);
        pieces[7][0] = new ChessPiece("rook", true, 7, 0);
        pieces[7][7] = new ChessPiece("rook", true, 7, 7);

        // Кони
        pieces[0][1] = new ChessPiece("knight", false, 0, 1);
        pieces[0][6] = new ChessPiece("knight", false, 0, 6);
        pieces[7][1] = new ChessPiece("knight", true, 7, 1);
        pieces[7][6] = new ChessPiece("knight", true, 7, 6);

        // Слоны
        pieces[0][2] = new ChessPiece("bishop", false, 0, 2);
        pieces[0][5] = new ChessPiece("bishop", false, 0, 5);
        pieces[7][2] = new ChessPiece("bishop", true, 7, 2);
        pieces[7][5] = new ChessPiece("bishop", true, 7, 5);

        // Ферзи
        pieces[0][3] = new ChessPiece("queen", false, 0, 3);
        pieces[7][3] = new ChessPiece("queen", true, 7, 3);

        // Короли
        pieces[0][4] = new ChessPiece("king", false, 0, 4);
        pieces[7][4] = new ChessPiece("king", true, 7, 4);
    }

    private void drawAllPieces() {
        Bitmap tempBitmap = boardBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (pieces[row][col] != null) {
                    drawPiece(canvas, pieces[row][col], row, col);
                }
            }
        }

        gameTable.setImageBitmap(tempBitmap);
    }

    private void drawPiece(Canvas canvas, ChessPiece piece, int row, int col) {
        int x = col * cellWidth;
        int y = row * cellHeight;

        Paint paint = new Paint();
        paint.setTextSize(cellHeight * 0.6f);
        paint.setTextAlign(Paint.Align.CENTER);

        // Используем Unicode символы для фигур
        String symbol = getPieceSymbol(piece);
        paint.setColor(piece.isWhite ? Color.WHITE : Color.BLACK);

        canvas.drawText(symbol, x + cellWidth / 2, y + cellHeight * 0.7f, paint);
    }

    private String getPieceSymbol(ChessPiece piece) {
        if (piece.type.equals("king")) return piece.isWhite ? "♔" : "♚";
        if (piece.type.equals("queen")) return piece.isWhite ? "♕" : "♛";
        if (piece.type.equals("rook")) return piece.isWhite ? "♖" : "♜";
        if (piece.type.equals("bishop")) return piece.isWhite ? "♗" : "♝";
        if (piece.type.equals("knight")) return piece.isWhite ? "♘" : "♞";
        if (piece.type.equals("pawn")) return piece.isWhite ? "♙" : "♟";
        return "?";
    }

    private void setupTouchListener() {
        gameTable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();

                    int col = x / cellWidth;
                    int row = y / cellHeight;

                    if (row >= 0 && row < 8 && col >= 0 && col < 8) {
                        handleCellClick(row, col);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void handleCellClick(int row, int col) {
        if (selectedPiece == null) {
            // Выбираем фигуру
            ChessPiece piece = pieces[row][col];
            if (piece != null && piece.isWhite == isWhiteTurn) {
                selectedPiece = piece;
                selectedRow = row;
                selectedCol = col;
                highlightSelectedCell(row, col);
                showPossibleMoves(row, col);
                Toast.makeText(this, "Выбрана " + piece.type, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Не ваша фигура!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Пытаемся переместить фигуру
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                movePiece(selectedRow, selectedCol, row, col);
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
                isWhiteTurn = !isWhiteTurn;
                drawAllPieces();

                // Проверка на мат (упрощенная)
                if (isCheckmate()) {
                    String winner = isWhiteTurn ? "Черные" : "Белые";
                    Toast.makeText(this, "Мат! Победили " + winner, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Невозможный ход!", Toast.LENGTH_SHORT).show();
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
                drawAllPieces();
            }
        }
    }

    private void highlightSelectedCell(int row, int col) {
        Bitmap tempBitmap = boardBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);

        // Рисуем все фигуры
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (pieces[r][c] != null) {
                    drawPiece(canvas, pieces[r][c], r, c);
                }
            }
        }

        // Рисуем выделение
        Paint highlight = new Paint();
        highlight.setColor(Color.argb(100, 255, 255, 0));
        canvas.drawRect(col * cellWidth, row * cellHeight,
                (col + 1) * cellWidth, (row + 1) * cellHeight, highlight);

        gameTable.setImageBitmap(tempBitmap);
    }

    private void showPossibleMoves(int row, int col) {
        Bitmap tempBitmap = boardBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);

        // Рисуем все фигуры
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (pieces[r][c] != null) {
                    drawPiece(canvas, pieces[r][c], r, c);
                }
            }
        }

        // Показываем возможные ходы
        List<int[]> moves = getPossibleMoves(row, col);
        Paint movePaint = new Paint();
        movePaint.setColor(Color.argb(150, 0, 255, 0));

        for (int[] move : moves) {
            int r = move[0], c = move[1];
            canvas.drawRect(c * cellWidth, r * cellHeight,
                    (c + 1) * cellWidth, (r + 1) * cellHeight, movePaint);
        }

        // Выделяем выбранную клетку
        Paint highlight = new Paint();
        highlight.setColor(Color.argb(100, 255, 255, 0));
        canvas.drawRect(col * cellWidth, row * cellHeight,
                (col + 1) * cellWidth, (row + 1) * cellHeight, highlight);

        gameTable.setImageBitmap(tempBitmap);
    }

    private List<int[]> getPossibleMoves(int row, int col) {
        ChessPiece piece = pieces[row][col];
        if (piece == null) return new ArrayList<>();

        List<int[]> moves = new ArrayList<>();

        switch (piece.type) {
            case "pawn":
                getPawnMoves(row, col, piece.isWhite, moves);
                break;
            case "rook":
                getRookMoves(row, col, piece.isWhite, moves);
                break;
            case "knight":
                getKnightMoves(row, col, piece.isWhite, moves);
                break;
            case "bishop":
                getBishopMoves(row, col, piece.isWhite, moves);
                break;
            case "queen":
                getQueenMoves(row, col, piece.isWhite, moves);
                break;
            case "king":
                getKingMoves(row, col, piece.isWhite, moves);
                break;
        }

        return moves;
    }

    private void getPawnMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int direction = isWhite ? -1 : 1;
        int startRow = isWhite ? 6 : 1;

        // Ход вперед на одну клетку
        int newRow = row + direction;
        if (newRow >= 0 && newRow < 8 && pieces[newRow][col] == null) {
            moves.add(new int[]{newRow, col});

            // Ход на две клетки с начальной позиции
            if (row == startRow && pieces[row + 2 * direction][col] == null) {
                moves.add(new int[]{row + 2 * direction, col});
            }
        }

        // Атака по диагонали
        for (int dc : new int[]{-1, 1}) {
            int newCol = col + dc;
            if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
                ChessPiece target = pieces[newRow][newCol];
                if (target != null && target.isWhite != isWhite) {
                    moves.add(new int[]{newRow, newCol});
                }
            }
        }
    }

    private void getRookMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        // Вверх, вниз, влево, вправо
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        getLinearMoves(row, col, isWhite, moves, directions);
    }

    private void getBishopMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        // По диагоналям
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        getLinearMoves(row, col, isWhite, moves, directions);
    }

    private void getQueenMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        // Все направления
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        getLinearMoves(row, col, isWhite, moves, directions);
    }

    private void getLinearMoves(int row, int col, boolean isWhite, List<int[]> moves, int[][] directions) {
        for (int[] dir : directions) {
            int r = row + dir[0];
            int c = col + dir[1];

            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                ChessPiece target = pieces[r][c];
                if (target == null) {
                    moves.add(new int[]{r, c});
                } else {
                    if (target.isWhite != isWhite) {
                        moves.add(new int[]{r, c});
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
    }

    private void getKnightMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : knightMoves) {
            int r = row + move[0];
            int c = col + move[1];

            if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                ChessPiece target = pieces[r][c];
                if (target == null || target.isWhite != isWhite) {
                    moves.add(new int[]{r, c});
                }
            }
        }
    }

    private void getKingMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;

                int r = row + dr;
                int c = col + dc;

                if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                    ChessPiece target = pieces[r][c];
                    if (target == null || target.isWhite != isWhite) {
                        moves.add(new int[]{r, c});
                    }
                }
            }
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = pieces[fromRow][fromCol];
        if (piece == null) return false;

        // Проверяем, что целевая клетка не занята своей фигурой
        ChessPiece target = pieces[toRow][toCol];
        if (target != null && target.isWhite == piece.isWhite) return false;

        // Получаем возможные ходы
        List<int[]> moves = getPossibleMoves(fromRow, fromCol);

        // Проверяем, есть ли целевая клетка в списке возможных ходов
        for (int[] move : moves) {
            if (move[0] == toRow && move[1] == toCol) {
                return true;
            }
        }

        return false;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = pieces[fromRow][fromCol];
        pieces[toRow][toCol] = piece;
        pieces[fromRow][fromCol] = null;

        // Обновляем позицию фигуры
        piece.row = toRow;
        piece.col = toCol;

        // Превращение пешки (упрощенно)
        if (piece.type.equals("pawn") && (toRow == 0 || toRow == 7)) {
            piece.type = "queen";
            Toast.makeText(this, "Пешка превратилась в ферзя!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCheckmate() {
        // Упрощенная проверка мата (просто проверяем есть ли ходы у текущего игрока)
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = pieces[row][col];
                if (piece != null && piece.isWhite == isWhiteTurn) {
                    List<int[]> moves = getPossibleMoves(row, col);
                    if (!moves.isEmpty()) {
                        return false; // Есть хотя бы один ход
                    }
                }
            }
        }
        return true; // Нет доступных ходов - мат
    }

    // Класс шахматной фигуры
    class ChessPiece {
        String type; // king, queen, rook, bishop, knight, pawn
        boolean isWhite;
        int row, col;

        ChessPiece(String type, boolean isWhite, int row, int col) {
            this.type = type;
            this.isWhite = isWhite;
            this.row = row;
            this.col = col;
        }
    }
}