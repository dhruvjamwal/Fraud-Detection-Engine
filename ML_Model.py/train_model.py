import pandas as pd
from sklearn.linear_model import LogisticRegression
import joblib

data = {
    "amount": [100, 20, 50000, 200000, 50, 150000, 300, 90000, 10, 80000],
    "velocity": [1, 1, 10, 15, 1, 12, 1, 10, 1, 8],  # Transactions per minute
    "night_txn": [0, 0, 0, 1, 0, 1, 0, 1, 0, 0],     # 1 if between 10PM - 6AM
    "fraud": [0, 0, 0, 1, 0, 1, 0, 1, 0, 1]          # 0 = Safe, 1 = Fraud
}

df = pd.DataFrame(data)
# Features: Amount, Velocity, Night Transaction
X = df[["amount", "velocity", "night_txn"]]
y = df["fraud"]

model = LogisticRegression()
model.fit(X, y)
joblib.dump(model, "fraud_model.pkl")
print("✅ ML model trained and saved as 'fraud_model.pkl'")
