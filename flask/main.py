from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///blackjack.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
class completedGame(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(150), unique=True, nullable=False)
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

@app.route('/api/users', methods=['GET'])
def get_users():
    with app.app_context():
        users = User.query.all()
        return jsonify([{'id': u.id, 'username': u.username} for u in users])

@app.route('/api/games', methods=['POST'])
def add_game():
    with app.app_context():
        data = request.get_json()
        
        new_game = completedGame(data.get("player"),data.get("score"))

        db.session.add(new_game)
        db.session.commit()
        return jsonify({'game_id': new_game.id}), 201

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

if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(debug=True)  