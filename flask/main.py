from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import json 

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///blackjack.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

class completedGame(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=False, nullable=False)
    score = db.Column(db.Integer)
    
    def __init__(self, username, score):
        self.username = username
        self.score = score
        
    def __repr__(self):
        return f"<CompletedGame {self.id} for {self.username} with score {self.score}>"

class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=True, nullable=False)
    games = db.relationship('Game', backref='user', lazy=True)

    def __repr__(self):
        return f"<User {self.username}>"

class Game(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    start_time = db.Column(db.DateTime, default=datetime.utcnow)
    end_time = db.Column(db.DateTime)
    final_result = db.Column(db.String(50))
    turns = db.relationship('Turn', backref='game', lazy=True, order_by='Turn.turn_number')

    def __repr__(self):
        return f"<Game {self.id} for User {self.user_id}>"

class Turn(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    game_id = db.Column(db.Integer, db.ForeignKey('game.id'), nullable=False)
    turn_number = db.Column(db.Integer, nullable=False)
    player_action = db.Column(db.String(50))
    player_hand = db.Column(db.String(200))
    dealer_hand = db.Column(db.String(200))
    turn_result = db.Column(db.String(100))
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<Turn {self.turn_number} in Game {self.game_id}>"

class MultiplayerGame(db.Model):
    id = db.Column(db.String, primary_key=True)  
    start_time = db.Column(db.DateTime, default=datetime.utcnow)
    dealer_hand = db.Column(db.String) 
    current_turn = db.Column(db.String)
    is_active = db.Column(db.Boolean, default=True)
    winner = db.Column(db.String, nullable=True) 

    players = db.relationship('MultiplayerPlayer', backref='game', lazy=True)
    turns = db.relationship('MultiplayerTurn', backref='multiplayer_game', lazy=True, order_by='MultiplayerTurn.turn_number')


class MultiplayerPlayer(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    game_id = db.Column(db.String, db.ForeignKey('multiplayer_game.id'), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id')) 
    username = db.Column(db.String(150), nullable=False) 
    hand = db.Column(db.String)  
    score = db.Column(db.Integer, default=0) 
    status = db.Column(db.String(50), default='playing')  
    is_host = db.Column(db.Boolean, default=False)

    def __repr__(self):
        return f"<MultiplayerPlayer {self.username} in Game {self.game_id}>"

class MultiplayerTurn(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    multiplayer_game_id = db.Column(db.String, db.ForeignKey('multiplayer_game.id'), nullable=False)
    turn_number = db.Column(db.Integer, nullable=False)
    acting_player_username = db.Column(db.String(150), nullable=False) 
    action_type = db.Column(db.String(50), nullable=False) 
    player_hands_json = db.Column(db.Text) 
    dealer_hand_json = db.Column(db.Text) 
    game_state_message = db.Column(db.String(255)) 
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return f"<MultiplayerTurn {self.turn_number} for {self.acting_player_username} in MultiplayerGame {self.multiplayer_game_id}>"


with app.app_context():
    db.create_all()


def get_or_create_local_user(username="Local Player"):
    with app.app_context():
        user = User.query.filter_by(username=username).first()
        if not user:
            user = User(username=username)
            db.session.add(user)
            db.session.commit()
        return user

def sync_to_firebase(game_id):
    pass 


@app.route('/api/users', methods=['GET'])
def get_users():
    with app.app_context():
        users = User.query.all()
        return jsonify([{'id': u.id, 'username': u.username} for u in users])
    
@app.route('/api/games', methods=['POST'])
def handle_game_creation():
    data = request.get_json()
    
    if request.args.get('type') == 'start_singleplayer':
        username = data.get('username')

        if not username:
            return jsonify({"error": "Missing username for singleplayer game start"}), 400

        user = User.query.filter_by(username=username).first()
        if not user:
            user = User(username=username)
            db.session.add(user)
            db.session.commit()
        
        new_game = Game(user_id=user.id)
        new_game.end_time = None
        new_game.final_result = None
        db.session.add(new_game)
        db.session.commit()
        return jsonify({"message": "New singleplayer game started", "game_id": new_game.id}), 201
    
    else:
        username_for_completed_game = data.get("username") 
        score = data.get("score")
        
        if not username_for_completed_game or score is None:
            return jsonify({'error': 'Missing username or score data for completed game record'}), 400

        user = User.query.filter_by(username=username_for_completed_game).first()
        if not user:
            user = User(username=username_for_completed_game)
            db.session.add(user)
            db.session.commit() 

        new_completed_game = completedGame(username=username_for_completed_game, score=score) 
        db.session.add(new_completed_game)
        db.session.commit()
        return jsonify({'message': 'Completed game added successfully', 'game_id': new_completed_game.id}), 201

@app.route('/api/games/<int:game_id>/turns', methods=['POST'])
def save_turn(game_id):
    data = request.get_json()
    if not data or 'turn_number' not in data or 'player_action' not in data:
        return jsonify({'error': 'Missing turn data'}), 400

    with app.app_context():
        game = Game.query.get_or_404(game_id)
        new_turn = Turn(
            game_id=game.id,
            turn_number=data.get('turn_number'),
            player_action=data.get('player_action'),
            player_hand=data.get('player_hand'),
            dealer_hand=data.get('dealer_hand'),
            turn_result=data.get('turn_result')
        )
        db.session.add(new_turn)
        db.session.commit()
        return jsonify({'message': 'Turn saved'}), 201

@app.route('/api/games/<int:game_id>', methods=['PUT'])
def update_game_result(game_id):
    data = request.get_json()
    final_result = data.get('final_result')
    end_time = datetime.utcnow()

    with app.app_context():
        game = Game.query.get_or_404(game_id)
        game.end_time = end_time
        game.final_result = final_result
        db.session.commit()
        return jsonify({'message': f'Game {game_id} updated'}), 200

@app.route('/api/users/<int:user_id>/games', methods=['GET'])
def get_user_games(user_id):
    with app.app_context():
        user = User.query.get_or_404(user_id)
        games = Game.query.filter_by(user_id=user.id).all()
        return jsonify([{'id': g.id, 'start_time': g.start_time, 'end_time': g.end_time, 'final_result': g.final_result} for g in games])

@app.route('/api/games/<int:game_id>/turns', methods=['GET'])
def get_game_turns(game_id):
    with app.app_context():
        game = Game.query.get_or_404(game_id)
        turns = Turn.query.filter_by(game_id=game.id).order_by(Turn.turn_number).all()
        return jsonify([{'turn_number': t.turn_number, 'player_action': t.player_action, 'player_hand': t.player_hand, 'dealer_hand': t.dealer_hand, 'turn_result': t.turn_result, 'timestamp': t.timestamp} for t in turns])

@app.route('/api/multiplayer_games', methods=['POST'])
def create_multiplayer_game():
    data = request.get_json()
    game_id = data.get('game_id')
    host_username = data.get('host_username')
    
    if not game_id or not host_username:
        return jsonify({'error': 'Missing game_id or host_username'}), 400

    with app.app_context():
        existing_game = MultiplayerGame.query.get(game_id)
        if existing_game:
            return jsonify({'error': 'Game ID already exists'}), 409

        host_user = User.query.filter_by(username=host_username).first()
        if not host_user:
            host_user = User(username=host_username)
            db.session.add(host_user)
            db.session.commit()

        new_game = MultiplayerGame(id=game_id, current_turn=host_username, is_active=True, dealer_hand=json.dumps([]))
        db.session.add(new_game)
        db.session.flush() 

        host_player = MultiplayerPlayer(game_id=game_id, user_id=host_user.id, username=host_username, is_host=True, hand=json.dumps([]), score=0, status='playing')
        db.session.add(host_player)
        db.session.commit()
        return jsonify({'message': 'Multiplayer game created', 'game_id': game_id}), 201

@app.route('/api/multiplayer_games/<string:game_id>/join', methods=['POST'])
def join_multiplayer_game(game_id):
    data = request.get_json()
    joiner_username = data.get('joiner_username')

    if not joiner_username:
        return jsonify({'error': 'Missing joiner_username'}), 400

    with app.app_context():
        game = MultiplayerGame.query.get(game_id)
        if not game:
            return jsonify({'error': 'Game not found'}), 404
        if not game.is_active:
            return jsonify({'error': 'Game is not active'}), 400

        existing_player = MultiplayerPlayer.query.filter_by(game_id=game_id, username=joiner_username).first()
        if existing_player:
            return jsonify({'message': 'Player already in game'}), 200 

        joiner_user = User.query.filter_by(username=joiner_username).first()
        if not joiner_user:
            joiner_user = User(username=joiner_username)
            db.session.add(joiner_user)
            db.session.commit() 

        non_host_player_count = MultiplayerPlayer.query.filter_by(game_id=game_id, is_host=False).count()
        if non_host_player_count >= 1: 
            return jsonify({'error': 'Game is full'}), 400

        new_player = MultiplayerPlayer(game_id=game_id, user_id=joiner_user.id, username=joiner_username, is_host=False, hand=json.dumps([]), score=0, status='playing')
        db.session.add(new_player)
        db.session.commit()
        return jsonify({'message': 'Player joined game', 'game_id': game_id}), 200

@app.route('/api/multiplayer_games/<string:game_id>/turns', methods=['POST'])
def save_multiplayer_turn(game_id):
    data = request.get_json()
    
    required_fields = ['turn_number', 'acting_player_username', 'action_type', 
                       'player_hands_json', 'dealer_hand_json', 'game_state_message']
    if not all(field in data for field in required_fields):
        return jsonify({'error': 'Missing essential turn data fields'}), 400

    with app.app_context():
        game = MultiplayerGame.query.get(game_id)
        if not game:
            return jsonify({'error': 'Multiplayer game not found'}), 404

        if 'current_turn' in data:
            game.current_turn = data['current_turn']
        if 'winner' in data:
            game.winner = data['winner']
            game.is_active = False 

        game.dealer_hand = data['dealer_hand_json']

        player_hands_data = json.loads(data['player_hands_json'])
        for username, player_data in player_hands_data.items():
            player = MultiplayerPlayer.query.filter_by(game_id=game_id, username=username).first()
            if player:
                player.hand = json.dumps(player_data['hand'])
                player.score = player_data['score']
                player.status = player_data.get('status', 'playing') 
        db.session.add(game) 
        db.session.commit() 

        new_turn = MultiplayerTurn(
            multiplayer_game_id=game_id,
            turn_number=data['turn_number'],
            acting_player_username=data['acting_player_username'],
            action_type=data['action_type'],
            player_hands_json=data['player_hands_json'],
            dealer_hand_json=data['dealer_hand_json'],
            game_state_message=data['game_state_message']
        )
        db.session.add(new_turn)
        db.session.commit()
        
        return jsonify({'message': 'Multiplayer turn saved'}), 201

@app.route('/api/multiplayer_games/<string:game_id>/history', methods=['GET'])
def get_multiplayer_game_history(game_id):
    with app.app_context():
        game = MultiplayerGame.query.get(game_id)
        if not game:
            return jsonify({'error': 'Multiplayer game not found'}), 404
        
        turns = MultiplayerTurn.query.filter_by(multiplayer_game_id=game_id).order_by(MultiplayerTurn.turn_number).all()
        
        history = []
        for turn in turns:
            history.append({
                'turn_number': turn.turn_number,
                'acting_player': turn.acting_player_username,
                'action_type': turn.action_type,
                'player_hands': json.loads(turn.player_hands_json),
                'dealer_hand': json.loads(turn.dealer_hand_json),
                'message': turn.game_state_message,
                'timestamp': turn.timestamp.isoformat()
            })
        return jsonify(history)

@app.route('/api/multiplayer_games/<string:game_id>', methods=['GET'])
def get_multiplayer_game_state(game_id):
    with app.app_context():
        game = MultiplayerGame.query.get(game_id)
        if not game:
            return jsonify({'error': 'Multiplayer game not found'}), 404
        
        players = MultiplayerPlayer.query.filter_by(game_id=game_id).all()
        players_data = []
        for p in players:
            players_data.append({
                'username': p.username,
                'hand': json.loads(p.hand) if p.hand else [],
                'score': p.score,
                'status': p.status,
                'is_host': p.is_host
            })
        
        return jsonify({
            'game_id': game.id,
            'start_time': game.start_time.isoformat(),
            'dealer_hand': json.loads(game.dealer_hand) if game.dealer_hand else [],
            'current_turn': game.current_turn,
            'is_active': game.is_active,
            'winner': game.winner,
            'players': players_data
        })


if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True, port=5000)