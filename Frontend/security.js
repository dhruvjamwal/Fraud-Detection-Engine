//Live Feed widget
const { useState, useEffect } = React;

const SecurityTicker = () => {
    const [alerts, setAlerts] = useState([
        { id: 1, text: "System Initialized", time: "Just now", type: "info" }
    ]);
    useEffect(() => {
        const messages = [
            "Intercepted SQL Injection from IP 192.168.0.10",
            "New device detected: iPhone 15 Pro (admin)",
            "Firewall updated: Port 8080 secured",
            "Brute force attempt blocked on /login",
            "Database backup completed successfully",
            "Suspicious activity detected in Region: East-US",
            "API Rate Limit exceeded for Key: 88X-Y21"
        ];

        const interval = setInterval(() => {
            const randomMsg = messages[Math.floor(Math.random() * messages.length)];
            const isDanger = randomMsg.includes("blocked") || randomMsg.includes("Injection") || randomMsg.includes("Suspicious");
            
            const newAlert = {
                id: Date.now(),
                text: randomMsg,
                time: new Date().toLocaleTimeString(),
                type: isDanger ? "danger" : "success"
            };

            setAlerts(prev => [newAlert, ...prev].slice(0, 5)); 
        }, 3500);

        return () => clearInterval(interval);
    }, []);

    const styles = {
        container: {
            height: "100%",
            display: "flex",
            flexDirection: "column",
            fontFamily: "'Inter', sans-serif",
            overflow: "hidden"
        },
        header: {
            padding: "15px",
            borderBottom: "1px solid #27272a",
            background: "#1c1c1c",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center"
        },
        title: { fontWeight: "600", color: "#e4e4e7", fontSize: "0.9rem" },
        badge: { 
            background: "#3b82f6", 
            color: "white", 
            padding: "2px 6px", 
            borderRadius: "4px", 
            fontSize: "0.7rem", 
            fontWeight: "bold",
            animation: "pulse 2s infinite"
        },
        list: { listStyle: "none", padding: "0", margin: "0", flex: 1, overflowY: "auto" },
        item: { 
            padding: "12px 15px", 
            borderBottom: "1px solid #27272a", 
            fontSize: "0.8rem", 
            display: "flex", 
            flexDirection: "column", 
            gap: "4px",
            transition: "background 0.2s"
        },
        danger: { borderLeft: "3px solid #ef4444", background: "rgba(239, 68, 68, 0.05)" },
        success: { borderLeft: "3px solid #10b981", background: "transparent" },
        text: { color: "#e4e4e7" },
        time: { color: "#71717a", fontSize: "0.7rem" }
    };

    return (
        <div style={styles.container}>
            <div style={styles.header}>
                <span style={styles.title}>Live Intel Feed</span>
                <span style={styles.badge}>REACT ACTIVE</span>
            </div>
            <ul style={styles.list}>
                {alerts.map(alert => (
                    <li key={alert.id} style={{ ...styles.item, ...(alert.type === 'danger' ? styles.danger : styles.success) }}>
                        <span style={styles.text}>{alert.text}</span>
                        <span style={styles.time}>{alert.time}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
};

const rootElement = document.getElementById('react-root');
if (rootElement) {
    const root = ReactDOM.createRoot(rootElement);
    root.render(<SecurityTicker />);
}
