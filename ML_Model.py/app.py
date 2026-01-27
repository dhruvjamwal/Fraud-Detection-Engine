from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
import joblib
import datetime
import random
import os

app = Flask(__name__, template_folder="templates")
CORS(app) 
try:
    model = joblib.load("fraud_model.pkl")
except:
    print("⚠️  Model not found! Please run train_model.py first.")
    model = None

transactions = []
transaction_counter = 1000

MERCHANTS = ["Amazon", "Netflix", "Uber", "Apple Store", "Binance", "Walmart", "Steam", "Starbucks"]
LOCATIONS = ["US", "IN", "CN", "RU", "BR", "DE", "FR"]
FRAUD_REASONS = ["Rule 1: High Value", "Rule 3: Night Pattern", "Rule 4: Velocity Check", "Rule 9: Crypto Block"]

def get_features(amount):
    """Generates features for the ML model based on input amount."""
    hour = datetime.datetime.now().hour
    velocity = random.randint(1, 20) if amount > 50000 else random.randint(1, 5)
    night_txn = 1 if hour < 6 or hour > 22 else 0
    return [amount, velocity, night_txn], velocity

@app.route('/')
def index():
    return "Fraud Detection API is running on Port 8080."
@app.route('/api/transactions/history', methods=['GET'])
def get_history():
    """Returns the list of recent transactions for the dashboard table."""
    return jsonify(transactions[::-1][:50])

@app.route('/api/transactions/stats', methods=['GET'])
def get_stats():
    """Returns KPI stats (Total Volume, Clean, Fraud)."""
    total = len(transactions)
    fraud_count = len([t for t in transactions if t['fraud']])
    clean_count = total - fraud_count
    return jsonify({
        "total_txns": total,
        "total_fraud": fraud_count,
        "total_clean": clean_count
    })

@app.route('/api/transactions/reset', methods=['DELETE'])
def reset_db():
    """Clears the database (Reset button)."""
    global transactions
    transactions = []
    return jsonify({"message": "Database reset"})

@app.route('/api/transactions/simulate', methods=['POST'])
def simulate():
    """Generates fake traffic based on the 'type' param from your HTML."""
    global transaction_counter
    sim_type = request.args.get('type', 'standard')
    
    if sim_type == 'high_value' or sim_type == 'email_test':
        amount = random.randint(60000, 500000)
        merchant = "Rolex Store" if random.random() > 0.5 else "Crypto.com"
    elif sim_type == 'velocity':
        amount = random.randint(100, 5000)
        merchant = "Uber" # High velocity usually small txns
    else:
        amount = random.randint(50, 5000)
        merchant = random.choice(MERCHANTS)

    # ML Prediction
    features, velocity = get_features(amount)
    
    is_fraud = False
    risk_score = 10
    reason = None

    if model:
        prediction = model.predict([features])[0]
        risk_score = int(model.predict_proba([features])[0][1] * 100)
        
        if prediction == 1:
            is_fraud = True
            reason = random.choice(FRAUD_REASONS)
            if amount > 50000: reason = "Rule 1: High Value"
    
    txn = {
        "id": transaction_counter,
        "timestamp": datetime.datetime.now().isoformat(),
        "merchant": merchant,
        "amount": amount,
        "ipAddress": f"192.168.1.{random.randint(10, 99)}",
        "location": random.choice(LOCATIONS),
        "deviceId": f"DEV-{random.randint(1000,9999)}",
        "fraud": is_fraud,
        "isFraud": is_fraud, # redundancy for compatibility
        "fraudReason": reason if is_fraud else None,
        "riskScore": risk_score
    }
    
    transactions.append(txn)
    transaction_counter += 1
    
    return jsonify({"message": "Simulation added", "txn": txn})

@app.route('/api/transactions', methods=['POST'])
def manual_check():
    """Handles the Manual Neural Inspector input."""
    data = request.json
    amount = data.get('amount', 0)
    merchant = data.get('merchant', 'Unknown')
    
    features, _ = get_features(amount)
    
    is_fraud = False
    reason = "Safe"
    
    if model:
        prediction = model.predict([features])[0]
        if prediction == 1:
            is_fraud = True
            reason = "ML Model Flagged High Risk"
            if amount > 100000: reason = "Rule 1: Limit Exceeded"
            if merchant.lower() in ['crypto', 'casino']: reason = "Rule 9: Restricted Category"

    return jsonify({
        "fraud": is_fraud,
        "isFraud": is_fraud,
        "fraudReason": reason,
        "riskScore": 95 if is_fraud else 5
    })

if __name__ == "__main__":
    app.run(port=8080, debug=True)
