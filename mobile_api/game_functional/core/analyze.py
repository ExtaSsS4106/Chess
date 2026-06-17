import json
from typing import List, Dict, Optional, Tuple

class ChessAnalyzer:
    def __init__(self, board_data: dict):
        """
        Инициализация анализатора
        board_data: словарь с полями 'grid' и 'isWhiteTurn'
        """
        self.grid = board_data['grid']
        self.is_white_turn = board_data.get('isWhiteTurn', True)
        self.board_size = 8
        
        # Находим позиции королей
        self.white_king_pos = self.find_king(True)
        self.black_king_pos = self.find_king(False)
        
        # Подсчитываем фигуры
        self.white_pieces = self.count_pieces(True)
        self.black_pieces = self.count_pieces(False)
    
    def find_king(self, is_white: bool) -> Optional[Tuple[int, int]]:
        """Находит позицию короля указанного цвета"""
        for row in range(self.board_size):
            for col in range(self.board_size):
                piece = self.grid[row][col]
                if piece and piece.get('type') == 'king' and piece.get('isWhite') == is_white:
                    return (row, col)
        return None
    
    def count_pieces(self, is_white: bool) -> int:
        """Подсчитывает количество фигур указанного цвета"""
        count = 0
        for row in range(self.board_size):
            for col in range(self.board_size):
                piece = self.grid[row][col]
                if piece and piece.get('isWhite') == is_white:
                    count += 1
        return count
    
    def get_king(self, is_white: bool):
        """Возвращает объект короля"""
        pos = self.find_king(is_white)
        if pos:
            row, col = pos
            return self.grid[row][col]
        return None
    
    def analyze(self) -> dict:
        """
        Основной метод анализа позиции
        Возвращает словарь с результатом
        """
        result = {
            'status': 'playing',
            'winner': None,
            'reason': '',
            'white_pieces': self.white_pieces,
            'black_pieces': self.black_pieces,
            'is_white_turn': self.is_white_turn,
            'king_in_check': None,
        }
        
        # 1. Проверка: съеден ли король?
        if not self.white_king_pos:
            result['status'] = 'checkmate'
            result['winner'] = 'black'
            result['reason'] = 'Белый король съеден'
            result['king_in_check'] = 'white'
            return result
        
        if not self.black_king_pos:
            result['status'] = 'checkmate'
            result['winner'] = 'white'
            result['reason'] = 'Черный король съеден'
            result['king_in_check'] = 'black'
            return result
        
        # 2. Проверка: остались только короли (ничья)
        if self.white_pieces == 1 and self.black_pieces == 1:
            result['status'] = 'draw'
            result['winner'] = None
            result['reason'] = 'Остались только короли'
            result['king_in_check'] = None
            return result
        
        # 3. Проверка: недостаточно фигур для мата
        if self.is_insufficient_material():
            result['status'] = 'draw'
            result['winner'] = None
            result['reason'] = 'Недостаточно фигур для мата'
            result['king_in_check'] = None
            return result
        
        # 4. Проверка: есть ли у текущего игрока ходы?
        current_color = self.is_white_turn
        
        # ОПРЕДЕЛЯЕМ, КАКОЙ КОРОЛЬ ПОД ШАХОМ
        king_in_check = self.get_king_in_check()
        result['king_in_check'] = king_in_check
        
        has_legal_moves = self.has_legal_moves(current_color)
        
        if not has_legal_moves:
            # Проверяем, находится ли король под шахом
            if self.is_king_in_check(current_color):
                result['status'] = 'checkmate'
                result['winner'] = 'black' if current_color else 'white'
                result['reason'] = f'Шах и мат! {"Белые" if current_color else "Черные"} проиграли'
            else:
                result['status'] = 'stalemate'
                result['winner'] = None
                result['reason'] = 'Пат! Ничья'
            return result
        
        # 5. Если все проверки пройдены - игра продолжается
        if king_in_check:
            result['status'] = 'check'
            result['reason'] = f'Шах! Король {"белых" if king_in_check == "white" else "черных"} под ударом!'
        else:
            result['status'] = 'playing'
            result['reason'] = 'Игра продолжается'
        
        return result
    def get_king_in_check(self) -> Optional[str]:
        """
        Определяет, какой король находится под шахом.
        Возвращает: 'white', 'black', или None
        """
        white_in_check = self.is_king_in_check(True)
        black_in_check = self.is_king_in_check(False)
        
        if white_in_check and black_in_check:
            # Оба короля под шахом (редкая ситуация, обычно ошибка)
            return 'both'
        elif white_in_check:
            return 'white'
        elif black_in_check:
            return 'black'
        else:
            return None
    
    
    def is_insufficient_material(self) -> bool:
        """Проверяет, достаточно ли фигур для мата"""
        white_pieces_list = self.get_pieces_list(True)
        black_pieces_list = self.get_pieces_list(False)
        
        # Король + слон против короля
        if len(white_pieces_list) == 2 and len(black_pieces_list) == 1:
            types = [p['type'] for p in white_pieces_list if p['type'] != 'king']
            if len(types) == 1 and types[0] in ['bishop', 'knight']:
                return True
        
        if len(black_pieces_list) == 2 and len(white_pieces_list) == 1:
            types = [p['type'] for p in black_pieces_list if p['type'] != 'king']
            if len(types) == 1 and types[0] in ['bishop', 'knight']:
                return True
        
        # Король + слон + слон против короля (если слоны на одной цвете)
        if len(white_pieces_list) == 3 and len(black_pieces_list) == 1:
            bishops = [p for p in white_pieces_list if p['type'] == 'bishop']
            if len(bishops) == 2:
                # Проверяем, что слоны на разных цветах (упрощенно)
                # Если на одной цвете - мат невозможен
                return True
        
        return False
    
    def get_pieces_list(self, is_white: bool) -> List[dict]:
        """Возвращает список всех фигур указанного цвета"""
        pieces = []
        for row in range(self.board_size):
            for col in range(self.board_size):
                piece = self.grid[row][col]
                if piece and piece.get('isWhite') == is_white:
                    piece_copy = piece.copy()
                    piece_copy['row'] = row
                    piece_copy['col'] = col
                    pieces.append(piece_copy)
        return pieces
    
    def has_legal_moves(self, is_white: bool) -> bool:
        """Проверяет, есть ли у игрока легальные ходы"""
        for row in range(self.board_size):
            for col in range(self.board_size):
                piece = self.grid[row][col]
                if piece and piece.get('isWhite') == is_white:
                    moves = self.get_pseudo_legal_moves(row, col)
                    for move_row, move_col in moves:
                        if self.is_move_legal(row, col, move_row, move_col, is_white):
                            return True
        return False
    
    def is_king_in_check(self, is_white: bool) -> bool:
        """Проверяет, находится ли король под шахом"""
        king_pos = self.find_king(is_white)
        if not king_pos:
            return True
        
        king_row, king_col = king_pos
        opponent_color = not is_white
        
        # Проверяем, может ли какая-либо фигура противника атаковать короля
        for row in range(self.board_size):
            for col in range(self.board_size):
                piece = self.grid[row][col]
                if piece and piece.get('isWhite') == opponent_color:
                    moves = self.get_pseudo_legal_moves(row, col)
                    for move_row, move_col in moves:
                        if move_row == king_row and move_col == king_col:
                            return True
        return False
    
    def get_pseudo_legal_moves(self, row: int, col: int) -> List[Tuple[int, int]]:
        """Получает псевдолегальные ходы для фигуры (без проверки на шах)"""
        piece = self.grid[row][col]
        if not piece:
            return []
        
        moves = []
        piece_type = piece['type']
        is_white = piece['isWhite']
        
        if piece_type == 'pawn':
            moves.extend(self.get_pawn_moves(row, col, is_white))
        elif piece_type == 'rook':
            moves.extend(self.get_rook_moves(row, col, is_white))
        elif piece_type == 'knight':
            moves.extend(self.get_knight_moves(row, col, is_white))
        elif piece_type == 'bishop':
            moves.extend(self.get_bishop_moves(row, col, is_white))
        elif piece_type == 'queen':
            moves.extend(self.get_queen_moves(row, col, is_white))
        elif piece_type == 'king':
            moves.extend(self.get_king_moves(row, col, is_white))
        
        return moves
    
    def is_move_legal(self, from_row: int, from_col: int, to_row: int, to_col: int, is_white: bool) -> bool:
        """Проверяет, легален ли ход (не оставляет ли короля под шахом)"""
        # Сохраняем состояние
        moved_piece = self.grid[from_row][from_col]
        target_piece = self.grid[to_row][to_col]
        
        # Делаем пробный ход
        self.grid[to_row][to_col] = moved_piece
        self.grid[from_row][from_col] = None
        
        # Проверяем, не под шахом ли король
        in_check = self.is_king_in_check(is_white)
        
        # Откатываем ход
        self.grid[from_row][from_col] = moved_piece
        self.grid[to_row][to_col] = target_piece
        
        return not in_check
    
    # ============ МЕТОДЫ ДЛЯ КАЖДОЙ ФИГУРЫ ============
    
    def get_pawn_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        moves = []
        direction = -1 if is_white else 1
        start_row = 6 if is_white else 1
        
        # Ход вперед на 1 клетку
        new_row = row + direction
        if 0 <= new_row < 8 and self.grid[new_row][col] is None:
            moves.append((new_row, col))
            # Ход на 2 клетки с начальной позиции
            if row == start_row and self.grid[row + 2 * direction][col] is None:
                moves.append((row + 2 * direction, col))
        
        # Атака по диагонали
        for dc in [-1, 1]:
            new_col = col + dc
            if 0 <= new_row < 8 and 0 <= new_col < 8:
                target = self.grid[new_row][new_col]
                if target and target.get('isWhite') != is_white:
                    moves.append((new_row, new_col))
        
        return moves
    
    def get_rook_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        moves = []
        directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]
        for dr, dc in directions:
            r, c = row + dr, col + dc
            while 0 <= r < 8 and 0 <= c < 8:
                target = self.grid[r][c]
                if target is None:
                    moves.append((r, c))
                else:
                    if target.get('isWhite') != is_white:
                        moves.append((r, c))
                    break
                r += dr
                c += dc
        return moves
    
    def get_knight_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        moves = []
        knight_moves = [(-2, -1), (-2, 1), (-1, -2), (-1, 2), (1, -2), (1, 2), (2, -1), (2, 1)]
        for dr, dc in knight_moves:
            r, c = row + dr, col + dc
            if 0 <= r < 8 and 0 <= c < 8:
                target = self.grid[r][c]
                if target is None or target.get('isWhite') != is_white:
                    moves.append((r, c))
        return moves
    
    def get_bishop_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        moves = []
        directions = [(-1, -1), (-1, 1), (1, -1), (1, 1)]
        for dr, dc in directions:
            r, c = row + dr, col + dc
            while 0 <= r < 8 and 0 <= c < 8:
                target = self.grid[r][c]
                if target is None:
                    moves.append((r, c))
                else:
                    if target.get('isWhite') != is_white:
                        moves.append((r, c))
                    break
                r += dr
                c += dc
        return moves
    
    def get_queen_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        return self.get_rook_moves(row, col, is_white) + self.get_bishop_moves(row, col, is_white)
    
    def get_king_moves(self, row: int, col: int, is_white: bool) -> List[Tuple[int, int]]:
        moves = []
        for dr in [-1, 0, 1]:
            for dc in [-1, 0, 1]:
                if dr == 0 and dc == 0:
                    continue
                r, c = row + dr, col + dc
                if 0 <= r < 8 and 0 <= c < 8:
                    target = self.grid[r][c]
                    if target is None or target.get('isWhite') != is_white:
                        moves.append((r, c))
        return moves


# ============ ИСПОЛЬЗОВАНИЕ ============

"""def analyze_chess_position(board_json: str):
    data = json.loads(board_json)
    analyzer = ChessAnalyzer(data)
    result = analyzer.analyze()
    
    # Форматируем результат
    status_messages = {
        'playing': '⏳ Игра продолжается',
        'check': '⚠️ Шах!',
        'checkmate': '🏆 Мат!',
        'stalemate': '🤝 Пат!',
        'draw': '🤝 Ничья'
    }
    
    print("=" * 50)
    print("📊 АНАЛИЗ ШАХМАТНОЙ ПОЗИЦИИ")
    print("=" * 50)
    print(f"📌 Статус: {status_messages.get(result['status'], result['status'])}")
    print(f"👤 Ход: {'Белых' if result['is_white_turn'] else 'Черных'}")
    print(f"♟ Белых фигур: {result['white_pieces']}")
    print(f"♟ Черных фигур: {result['black_pieces']}")
    
    if result['winner']:
        print(f"🏆 Победитель: {'Белые' if result['winner'] == 'white' else 'Черные'}")
    
    if result['reason']:
        print(f"📝 Причина: {result['reason']}")
    
    if result.get('in_check'):
        print("⚠️ Король под шахом!")
    
    print("=" * 50)
    
    return result


# ============ ПРИМЕР ИСПОЛЬЗОВАНИЯ ============

if __name__ == "__main__":
    # Твой JSON
    board_json = '''{
      "grid": [
        [
          {"type": "rook", "isWhite": false},
          {"type": "knight", "isWhite": false},
          {"type": "bishop", "isWhite": false},
          {"type": "queen", "isWhite": false},
          {"type": "king", "isWhite": false},
          null,
          null,
          {"type": "rook", "isWhite": false}
        ],
        [
          null,
          {"type": "pawn", "isWhite": false},
          {"type": "pawn", "isWhite": false},
          {"type": "pawn", "isWhite": false},
          null,
          {"type": "pawn", "isWhite": false},
          {"type": "pawn", "isWhite": false},
          {"type": "pawn", "isWhite": false}
        ],
        [
          null,
          null,
          null,
          null,
          null,
          {"type": "knight", "isWhite": false},
          null,
          null
        ],
        [
          {"type": "pawn", "isWhite": false},
          null,
          null,
          null,
          {"type": "pawn", "isWhite": false},
          null,
          null,
          null
        ],
        [
          null,
          null,
          {"type": "pawn", "isWhite": true},
          {"type": "pawn", "isWhite": true},
          null,
          null,
          null,
          null
        ],
        [
          {"type": "bishop", "isWhite": false},
          null,
          {"type": "knight", "isWhite": true},
          null,
          null,
          null,
          null,
          null
        ],
        [
          null,
          {"type": "pawn", "isWhite": true},
          null,
          null,
          {"type": "pawn", "isWhite": true},
          {"type": "pawn", "isWhite": true},
          {"type": "pawn", "isWhite": true},
          {"type": "pawn", "isWhite": true}
        ],
        [
          {"type": "rook", "isWhite": true},
          null,
          {"type": "bishop", "isWhite": true},
          {"type": "queen", "isWhite": true},
          {"type": "king", "isWhite": true},
          {"type": "bishop", "isWhite": true},
          {"type": "knight", "isWhite": true},
          {"type": "rook", "isWhite": true}
        ]
      ],
      "isWhiteTurn": true
    }'''
    
    result = analyze_chess_position(board_json)"""