package com.example.chess;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chess.api.Requests;
import com.example.chess.api.endPoints;
import com.example.chess.data.loadUser;
import com.example.chess.gameCore.GameOverDialog;
import com.example.chess.gameCore.Pause;
import com.example.chess.gameCore.giveUp;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.app.Dialog;
import androidx.appcompat.app.AlertDialog;

public class GameActivity extends AppCompatActivity {
    private Dialog pauseDialog = null;
    private Dialog giveup = null;
    private Dialog gameOverDiaolg = null;
    private boolean gameStop = false;
    private ImageView gameTable;
    private ImageButton flagButton;
    private Bitmap boardBitmap;
    private TextView name_p1_g;
    private TextView name_p2_g;
    private int cellWidth, cellHeight;
    private int boardWidth, boardHeight;

    private ChessPiece[][] pieces = new ChessPiece[8][8];
    private ChessPiece selectedPiece = null;
    private int selectedRow = -1, selectedCol = -1;
    private boolean isWhiteTurn = true;

    // WebSocket fields
    private WebSocket webSocket;
    private OkHttpClient client;
    private String roomId;
    private Requests requests;
    private boolean isWhitePlayer = true; // Будет определено при подключении
    private boolean myTurn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requests = new Requests(this);
        requests.PingPong(new Requests.ApiCallback() {
            @Override
            public void onSuccess(String response) {}

            @Override
            public void onError(String error) {}
        });
        setContentView(R.layout.game);

        name_p1_g = findViewById(R.id.name_p1_g);
        name_p2_g = findViewById(R.id.name_p2_g);

        flagButton = findViewById(R.id.flagButton);

        gameTable = findViewById(R.id.gameTable);
        roomId = getIntent().getStringExtra("room_id");


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
            }
        });

        gameTable.post(new Runnable() {
            @Override
            public void run() {
                drawGrid();
                initPieces();
                drawAllPieces();
                setupTouchListener();

                client = new OkHttpClient();
                connectToWebSocket();

            }
        });

        flagButton.setOnClickListener(v ->showGiveUpDialog());
    }

    private void connectToWebSocket() {
        loadUser loadUser = new loadUser();
        loadUser.UserData userData = loadUser.loadUserData(this);
        String token = (userData != null) ? userData.getToken() : "";

        endPoints endpoints = new endPoints();
        String url = endpoints.getWS_URL() + endpoints.getGAME_SESSION() + roomId + "/?token=" + token;

        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {

                Log.d("GameSession", "Connected to game session");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d("GameSession", "Received: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.optString("type");
                    JSONObject desk = null;
                    String status;
                    String winner;
                    String message;
                    int whitepiecesCount;
                    int blackpiecesCount;
                    switch (type) {

                        case "connected":
                            isWhitePlayer = json.optBoolean("is_white", true);

                            if (json.has("desck") && !json.isNull("desck")) {
                                desk = json.getJSONObject("desck");
                            }
                            String user1 = json.optString("user1");
                            String user2 = json.optString("user2");
                            runOnUiThread(() -> {
                                if (isWhitePlayer){
                                    name_p1_g.setText(user1);
                                    name_p2_g.setText(user2);
                                } else {
                                    name_p2_g.setText(user1);
                                    name_p1_g.setText(user2);
                                }
                            });
                            final JSONObject finalDesk = desk;  // для использования в runOnUiThread
                            runOnUiThread(() -> {
                                String color = isWhitePlayer ? "белых" : "черных";
                                Toast.makeText(GameActivity.this, "Вы играете за " + color, Toast.LENGTH_SHORT).show();
                                if (finalDesk == null){
                                    drawAllPieces();
                                } else {
                                    updateBoardFromJson(finalDesk);
                                }
                            });
                            break;
                        case "start_game":
                            gameStop = false;
                            runOnUiThread(() -> {
                                Toast.makeText(GameActivity.this, "Игра началась!", Toast.LENGTH_SHORT).show();
                            });
                            // Сервер может прислать кто мы. Пока предположим Player 1 = White.
                            // Для простоты, в вашей реализации Session.py, user_1 - создатель.
                            break;
                        case "give_up":
                            gameStop = true;
                            status = json.optString("status");
                            winner = json.optString("winner");
                            message = json.optString("reason");
                            whitepiecesCount = json.optInt("white_pieces");
                            blackpiecesCount = json.optInt("black_pieces");
                            runOnUiThread(() -> {
                                showGameOverDialog(status, winner, whitepiecesCount, blackpiecesCount, message);
                            });
                            break;
                        case "opponent_move":
                            status = json.optString("status");
                            winner = json.optString("winner");
                            message = json.optString("reason");
                            whitepiecesCount = json.optInt("white_pieces");
                            blackpiecesCount = json.optInt("black_pieces");
                            if (json.has("desck") && !json.isNull("desck")) {
                                desk = json.getJSONObject("desck");
                            }
                            switch (status) {
                                case "checkmate":
                                    gameStop = true;
                                    runOnUiThread(() -> {
                                        showGameOverDialog(status, winner, whitepiecesCount, blackpiecesCount, message);
                                    });
                                    updateBoardFromJson(desk);
                                    break;
                                case "draw":
                                    gameStop = true;
                                    runOnUiThread(() -> {
                                        showGameOverDialog(status, winner, whitepiecesCount, blackpiecesCount, message);
                                    });
                                    updateBoardFromJson(desk);
                                    break;
                                case "stalemate":
                                    gameStop = true;
                                    runOnUiThread(() -> {
                                        showGameOverDialog(status, winner, whitepiecesCount, blackpiecesCount, message);
                                    });
                                    updateBoardFromJson(desk);
                                    break;
                                case "check":
                                    gameStop = false;
                                    updateBoardFromJson(desk);
                                    runOnUiThread(() -> {
                                        Toast.makeText(GameActivity.this, message, Toast.LENGTH_SHORT).show();
                                    });
                                    break;
                                case "playing":
                                    gameStop = false;
                                    updateBoardFromJson(desk);
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + status);
                            }
                            break;
                        case "waiting":
                            runOnUiThread(() -> {
                                Toast.makeText(GameActivity.this, json.optString("message"), Toast.LENGTH_SHORT).show();
                            });
                            break;
                        case "opponent_disconnected":
                            String messDisconnected = json.optString("message");
                            gameStop = true;
                            if (gameOverDiaolg == null || !gameOverDiaolg.isShowing()) {
                                runOnUiThread(() -> {
                                    showPauseDialog(messDisconnected);
                                });
                            }
                            break;
                        case "opponent_reconnected":
                            runOnUiThread(() -> {
                                if (pauseDialog != null || pauseDialog.isShowing()) {
                                    pauseDialog.dismiss();
                                    pauseDialog = null;
                                }
                                gameStop = false;
                                Toast.makeText(GameActivity.this, "Соперник вернулся!", Toast.LENGTH_SHORT).show();
                            });
                            break;
                        case "error":
                            runOnUiThread(() -> {
                                Toast.makeText(GameActivity.this, json.optString("message"), Toast.LENGTH_LONG).show();
                            });
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("GameSession", "Failure", t);
            }
        });
    }

    private void updateBoardFromJson(JSONObject desk) {
        try {
            JSONArray grid = desk.getJSONArray("grid");
            isWhiteTurn = desk.getBoolean("isWhiteTurn");

            for (int r = 0; r < 8; r++) {
                JSONArray rowArray = grid.getJSONArray(r);
                for (int c = 0; c < 8; c++) {
                    JSONObject pJson = rowArray.optJSONObject(c);
                    if (pJson == null || pJson.isNull("type")) {
                        pieces[r][c] = null;
                    } else {
                        pieces[r][c] = new ChessPiece(
                                pJson.getString("type"),
                                pJson.getBoolean("isWhite"),
                                r, c
                        );
                    }
                }
            }
            runOnUiThread(this::drawAllPieces);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject boardToJson() {
        try {
            JSONObject desk = new JSONObject();
            JSONArray grid = new JSONArray();
            for (int r = 0; r < 8; r++) {
                JSONArray rowArray = new JSONArray();
                for (int c = 0; c < 8; c++) {
                    if (pieces[r][c] == null) {
                        rowArray.put(JSONObject.NULL);
                    } else {
                        JSONObject pJson = new JSONObject();
                        pJson.put("type", pieces[r][c].type);
                        pJson.put("isWhite", pieces[r][c].isWhite);
                        rowArray.put(pJson);
                    }
                }
                grid.put(rowArray);
            }
            desk.put("grid", grid);

            return desk;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendBoardToServer() {
        try {
            JSONObject desk = boardToJson();
            if (desk != null && webSocket != null) {
                desk.put("type", "save");
                desk.put("isWhiteTurn", !isWhiteTurn);
                webSocket.send(desk.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    private void drawGrid() {
        Drawable drawable = gameTable.getDrawable();
        if (drawable == null) return;
        Bitmap originalBitmap = ((BitmapDrawable) drawable).getBitmap();

        boardBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(boardBitmap);

        boardWidth = boardBitmap.getWidth();
        boardHeight = boardBitmap.getHeight();

        cellWidth = boardWidth / 8;
        cellHeight = boardHeight / 8;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2f);

        for (int i = 1; i <= 7; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, boardHeight, paint);
            canvas.drawLine(0, i * cellHeight, boardWidth, i * cellHeight, paint);
        }

        gameTable.setImageBitmap(boardBitmap);
    }

    private void initPieces() {
        // Стандартная расстановка
        for (int i = 0; i < 8; i++) {
            pieces[1][i] = new ChessPiece("pawn", false, 1, i);
            pieces[6][i] = new ChessPiece("pawn", true, 6, i);
        }
        pieces[0][0] = new ChessPiece("rook", false, 0, 0);
        pieces[0][7] = new ChessPiece("rook", false, 0, 7);
        pieces[7][0] = new ChessPiece("rook", true, 7, 0);
        pieces[7][7] = new ChessPiece("rook", true, 7, 7);
        pieces[0][1] = new ChessPiece("knight", false, 0, 1);
        pieces[0][6] = new ChessPiece("knight", false, 0, 6);
        pieces[7][1] = new ChessPiece("knight", true, 7, 1);
        pieces[7][6] = new ChessPiece("knight", true, 7, 6);
        pieces[0][2] = new ChessPiece("bishop", false, 0, 2);
        pieces[0][5] = new ChessPiece("bishop", false, 0, 5);
        pieces[7][2] = new ChessPiece("bishop", true, 7, 2);
        pieces[7][5] = new ChessPiece("bishop", true, 7, 5);
        pieces[0][3] = new ChessPiece("queen", false, 0, 3);
        pieces[7][3] = new ChessPiece("queen", true, 7, 3);
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
        // Отражение для черного игрока
        int displayRow = isWhitePlayer ? row : 7 - row;
        int displayCol = isWhitePlayer ? col : 7 - col;

        int x = displayCol * cellWidth;
        int y = displayRow * cellHeight;

        Paint paint = new Paint();
        paint.setTextSize(cellHeight * 0.6f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(piece.isWhite ? Color.WHITE : Color.BLACK);

        canvas.drawText(getPieceSymbol(piece), x + cellWidth / 2, y + cellHeight * 0.7f, paint);
    }

    private String getPieceSymbol(ChessPiece piece) {
        switch (piece.type) {
            case "king": return piece.isWhite ? "♔" : "♚";
            case "queen": return piece.isWhite ? "♕" : "♛";
            case "rook": return piece.isWhite ? "♖" : "♜";
            case "bishop": return piece.isWhite ? "♗" : "♝";
            case "knight": return piece.isWhite ? "♘" : "♞";
            case "pawn": return piece.isWhite ? "♙" : "♟";
            default: return "?";
        }
    }

    private void setupTouchListener() {
        gameTable.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    int displayCol = (int) (x * 8 / v.getWidth());
                    int displayRow = (int) (y * 8 / v.getHeight());

                    // Перевод в реальные координаты массива
                    int row = isWhitePlayer ? displayRow : 7 - displayRow;
                    int col = isWhitePlayer ? displayCol : 7 - displayCol;

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
        if (gameStop) {
            return;
        }
        // Проверка очереди хода
        if (isWhiteTurn != isWhitePlayer) {
            Toast.makeText(this, "Сейчас ход соперника", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPiece == null) {
            ChessPiece piece = pieces[row][col];
            if (piece != null && piece.isWhite == isWhitePlayer) {
                selectedPiece = piece;
                selectedRow = row;
                selectedCol = col;
                highlightSelectedCell(row, col);
                Toast.makeText(this, "Выбрана " + piece.type, Toast.LENGTH_SHORT).show();
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                movePiece(selectedRow, selectedCol, row, col);
                sendBoardToServer(); // Отправляем состояние после хода
                selectedPiece = null;
                selectedRow = -1;
                selectedCol = -1;
                isWhiteTurn = !isWhiteTurn;
                drawAllPieces();
            } else {
                selectedPiece = null;
                drawAllPieces();
            }
        }
    }

    private void highlightSelectedCell(int row, int col) {
        Bitmap tempBitmap = boardBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(tempBitmap);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (pieces[r][c] != null) drawPiece(canvas, pieces[r][c], r, c);
            }
        }

        List<int[]> moves = getPossibleMoves(row, col);
        Paint movePaint = new Paint();
        movePaint.setColor(Color.argb(150, 0, 255, 0));

        for (int[] move : moves) {
            int r = move[0], c = move[1];
            int displayR = isWhitePlayer ? r : 7 - r;
            int displayC = isWhitePlayer ? c : 7 - c;
            canvas.drawRect(displayC * cellWidth, displayR * cellHeight,
                    (displayC + 1) * cellWidth, (displayR + 1) * cellHeight, movePaint);
        }

        Paint highlight = new Paint();
        highlight.setColor(Color.argb(100, 255, 255, 0));
        int dr = isWhitePlayer ? row : 7 - row;
        int dc = isWhitePlayer ? col : 7 - col;
        canvas.drawRect(dc * cellWidth, dr * cellHeight, (dc + 1) * cellWidth, (dr + 1) * cellHeight, highlight);

        gameTable.setImageBitmap(tempBitmap);
    }

    private List<int[]> getPossibleMoves(int row, int col) {
        ChessPiece piece = pieces[row][col];
        if (piece == null) return new ArrayList<>();
        List<int[]> moves = new ArrayList<>();
        switch (piece.type) {
            case "pawn": getPawnMoves(row, col, piece.isWhite, moves); break;
            case "rook": getRookMoves(row, col, piece.isWhite, moves); break;
            case "knight": getKnightMoves(row, col, piece.isWhite, moves); break;
            case "bishop": getBishopMoves(row, col, piece.isWhite, moves); break;
            case "queen": getQueenMoves(row, col, piece.isWhite, moves); break;
            case "king": getKingMoves(row, col, piece.isWhite, moves); break;
        }
        return moves;
    }

    private void getPawnMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int dir = isWhite ? -1 : 1;
        int nextR = row + dir;
        if (nextR >= 0 && nextR < 8 && pieces[nextR][col] == null) {
            moves.add(new int[]{nextR, col});
            int startR = isWhite ? 6 : 1;
            if (row == startR && pieces[row + 2 * dir][col] == null) moves.add(new int[]{row + 2 * dir, col});
        }
        for (int dc : new int[]{-1, 1}) {
            int nc = col + dc;
            if (nextR >= 0 && nextR < 8 && nc >= 0 && nc < 8) {
                ChessPiece t = pieces[nextR][nc];
                if (t != null && t.isWhite != isWhite) moves.add(new int[]{nextR, nc});
            }
        }
    }

    private void getLinearMoves(int row, int col, boolean isWhite, List<int[]> moves, int[][] dirs) {
        for (int[] d : dirs) {
            int r = row + d[0], c = col + d[1];
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                if (pieces[r][c] == null) moves.add(new int[]{r, c});
                else {
                    if (pieces[r][c].isWhite != isWhite) moves.add(new int[]{r, c});
                    break;
                }
                r += d[0]; c += d[1];
            }
        }
    }

    private void getRookMoves(int r, int c, boolean w, List<int[]> m) { getLinearMoves(r, c, w, m, new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}); }
    private void getBishopMoves(int r, int c, boolean w, List<int[]> m) { getLinearMoves(r, c, w, m, new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}); }
    private void getQueenMoves(int r, int c, boolean w, List<int[]> m) { getLinearMoves(r, c, w, m, new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}}); }
    private void getKnightMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        int[][] kMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
        for (int[] m : kMoves) {
            int r = row + m[0], c = col + m[1];
            if (r >= 0 && r < 8 && c >= 0 && c < 8 && (pieces[r][c] == null || pieces[r][c].isWhite != isWhite)) moves.add(new int[]{r, c});
        }
    }
    private void getKingMoves(int row, int col, boolean isWhite, List<int[]> moves) {
        for (int dr = -1; dr <= 1; dr++) for (int dc = -1; dc <= 1; dc++) {
            if (dr == 0 && dc == 0) continue;
            int r = row + dr, c = col + dc;
            if (r >= 0 && r < 8 && c >= 0 && c < 8 && (pieces[r][c] == null || pieces[r][c].isWhite != isWhite)) moves.add(new int[]{r, c});
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        List<int[]> moves = getPossibleMoves(fromRow, fromCol);
        for (int[] m : moves) if (m[0] == toRow && m[1] == toCol) return true;
        return false;
    }

    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        pieces[toRow][toCol] = pieces[fromRow][fromCol];
        pieces[fromRow][fromCol] = null;
        checkPawnPromotion(toRow, toCol);
    }

    private void checkPawnPromotion(int row, int col){
        ChessPiece piece = pieces[row][col];
        if (piece == null || !piece.type.equals("pawn")) return;
        if ((piece.isWhite && row == 0) || (!piece.isWhite && row == 7)){
            showPromotionDialog(row, col);
        }
    }
    private void showPromotionDialog(int row, int col){
        String[] options = {"♕ Ферзь", "♖ Ладья", "♗ Слон", "♘ Конь"};
        String[] types = {"queen", "rook", "bishop", "knight"};
        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Превращение пешки")
                    .setItems(options, (dialog, which) ->
                    {
                        promotePawn(row, col, types[which]);
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void promotePawn(int row, int col, String newType) {
        ChessPiece piece = pieces[row][col];
        if (piece != null) {
            piece.type = newType;
            drawAllPieces();

            String name = "";
            switch (newType) {
                case "queen": name = "ферзя"; break;
                case "rook": name = "ладья"; break;
                case "bishop": name = "слона"; break;
                case "knight": name = "коня"; break;
            }
            Toast.makeText(this, "Пешка превратилась в " + name + "!", Toast.LENGTH_SHORT).show();
            sendBoardToServer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) webSocket.close(1000, "Activity closed");
    }

    class ChessPiece {
        String type;
        boolean isWhite;
        int row, col;
        ChessPiece(String type, boolean isWhite, int row, int col) {
            this.type = type; this.isWhite = isWhite; this.row = row; this.col = col;
        }
    }

    private void showGameOverDialog(String status, String winner, int whitePieces, int blackPieces, String message) {
        String title = "Игра окончена!";
        String icon = "🏆";
        String stats = "Белых: " + whitePieces + " | Черных: " + blackPieces;

        switch (status) {
            case "checkmate":
                if ("white".equals(winner)) {
                    icon = "🏆";
                } else if ("black".equals(winner)) {
                    icon = "🏆";
                }
                break;
            case "draw":
                icon = "🤝";
                break;
            case "stalemate":
                icon = "🤝";
                break;
            default:
                return;
        }

        gameOverDiaolg = GameOverDialog.show(
                this,
                title,
                icon,
                message,
                stats,
                () -> {
                    // Что делать при нажатии "Выйти"
                    finish();
                }
        );
    }

    private void showPauseDialog(String message) {
        runOnUiThread(() -> {
            if (pauseDialog != null && pauseDialog.isShowing()) {
                pauseDialog.dismiss();
                pauseDialog = null;
            }
            pauseDialog = Pause.show(
                    this,
                    message,
                    () -> {
                        // Что делать при нажатии "Выйти"
                        finish();
                    }
            );
        });
    }

    private void showGiveUpDialog() {
        runOnUiThread(() -> {
            if (giveup != null && giveup.isShowing()) {
                giveup.dismiss();
                giveup = null;
            }
            giveup = giveUp.show(
                    this,
                    () -> {
                        try {
                            JSONObject json = new JSONObject();
                            json.put("type", "give_up");
                            webSocket.send(json.toString());
                            new android.os.Handler().postDelayed(() -> {
                                finish();
                            }, 500);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
            );
        });
    }

}
