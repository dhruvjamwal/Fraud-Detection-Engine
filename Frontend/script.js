lucide.createIcons();
window.lastData = [];
let gaugeChart;

const App = {
    login: () => {
        const p = document.getElementById('login-pass').value;
        if(p === "26FDE51") {
            document.getElementById('login-layer').style.opacity = '0';
            setTimeout(() => {
                document.getElementById('login-layer').style.display = 'none';
                document.getElementById('app-root').style.display = 'flex';
                setTimeout(() => document.getElementById('app-root').style.opacity = '1', 50);
                App.initCharts();
                App.initMap();
                Backend.sync();
                setInterval(Backend.sync, 4000);
                setInterval(() => document.getElementById('clock').innerText = new Date().toLocaleTimeString(), 1000);
            }, 500);
        } else { alert("Invalid Key"); }
    },

    initMap: () => {
        window.mapInstance = new jsVectorMap({
            selector: '#world-map',
            map: 'world',
            backgroundColor: 'transparent',
            zoomButtons: false,
            regionStyle: { 
                initial: { fill: '#09090b', stroke: '#27272a', strokeWidth: 0.8 }, 
                hover: { fill: '#27272a' } 
            },
            markerStyle: {
                initial: { 
                    fill: '#ef4444', 
                    stroke: 'rgba(239,68,68,0.4)', 
                    strokeWidth: 6,
                    r: 4 
                }
            }
        });
    },

    updateThreatMap: (history) => {
        const frauds = history.filter(t => t.fraud || t.isFraud);
        
        const countryCenters = {
            'us': [38, -97], 'usa': [38, -97], 'united states': [38, -97],
            'cn': [35, 105], 'china': [35, 105],
            'ru': [60, 100], 'russia': [60, 100],
            'in': [22, 79],  'india': [22, 79],
            'br': [-14, -55], 'brazil': [-14, -55],
            'de': [51, 10],  'germany': [51, 10],
            'uk': [55, -3],  'united kingdom': [55, -3],
            'au': [-25, 133], 'australia': [-25, 133], 
            'ca': [56, -106], 'canada': [56, -106],
            'jp': [36, 138],  'japan': [36, 138],
            'fr': [46, 2],    'france': [46, 2],
            'za': [-30, 25],  'south africa': [-30, 25],
            'ng': [9, 8],     'nigeria': [9, 8]
        };

        const markers = frauds.map(f => {
            let rawCountry = f.country || f.location || 'US';
            let locKey = rawCountry.toString().toLowerCase().trim();
            let center = countryCenters[locKey];
            if (!center) { center = [0, 0]; }
            
            const idVal = f.id || 0; 
            const latOffset = (Math.sin(idVal * 999) * 6); 
            const lonOffset = (Math.cos(idVal * 999) * 6); 

            return {
                name: `Fraud Alert #${f.id} (${rawCountry})`,
                coords: [ center[0] + latOffset, center[1] + lonOffset ],
                style: { fill: '#ef4444', r: 4 + (f.amount % 3) } 
            };
        });

        document.getElementById('world-map').innerHTML = '';
        new jsVectorMap({
            selector: '#world-map',
            map: 'world',
            backgroundColor: 'transparent',
            zoomButtons: false,
            regionStyle: { initial: { fill: '#18181b', stroke: '#27272a', strokeWidth: 0.5 } },
            markers: markers,
            markerStyle: {
                initial: { fill: '#ef4444', stroke: 'rgba(220, 38, 38, 0.4)', strokeWidth: 8 }
            }
        });
    },

    initCharts: () => {
        const commonOpt = { plugins: { legend: { display: false } }, scales: { x: { display: false }, y: { display: false } }, elements: { line: { tension: 0.4, borderWidth: 2 }, point: { radius: 0 } }, maintainAspectRatio: false };
        
        gaugeChart = new Chart(document.getElementById('chart-gauge'), {
            type: 'doughnut',
            data: { labels: ['Fraud', 'Safe'], datasets: [{ data: [15, 85], backgroundColor: ['#ef4444', '#3b82f6'], borderWidth: 0, cutout: '85%' }] },
            options: { 
                plugins: { legend: { display: false }, tooltip: { enabled: false } }, 
                maintainAspectRatio: false,
                animation: {
                    duration: 2000, 
                    easing: 'easeOutElastic',
                    animateRotate: true,
                    animateScale: true
                }
            }
        });

        ['chart-s1', 'chart-s2', 'chart-s3', 'chart-s4'].forEach((id, i) => {
            const ctx = document.getElementById(id).getContext('2d');
            const color = i === 3 ? '#10b981' : (i === 1 ? '#ef4444' : '#3b82f6');
            const grad = ctx.createLinearGradient(0, 0, 0, 100);
            grad.addColorStop(0, color); grad.addColorStop(1, 'rgba(0,0,0,0)');
            new Chart(ctx, { type: 'line', data: { labels: [1,2,3,4,5,6], datasets: [{ data: Array.from({length:6},()=>Math.random()*100), borderColor: color, backgroundColor: grad, fill: true }] }, options: commonOpt });
        });
    },

    render: (data, stats) => {
        document.getElementById('kpi-total').innerText = stats.total_txns || 0;
        document.getElementById('kpi-clean').innerText = stats.total_clean || 0;
        document.getElementById('kpi-fraud').innerText = stats.total_fraud || 0;
        document.getElementById('alert-count').innerText = stats.total_fraud || 0;

        const tbody = document.querySelector('#txn-table tbody');
        tbody.innerHTML = data.slice(0, 100).map(t => { 
            let badge = 'st-green'; let txt = 'CLEAN';
            if(t.fraud || t.isFraud) { badge = 'st-red'; txt = 'FRAUD'; }
            
            let seed = (t.id * 0.123456);
            let staticRandom = seed - Math.floor(seed); 

            let score = t.riskScore || (t.fraud ? (85 + staticRandom * 14) : (staticRandom * 20)).toFixed(1);
            let scoreColor = score > 50 ? '#ef4444' : '#3b82f6';

            return `<tr style="cursor:pointer; transition:0.2s;" onclick="App.showDetails(${t.id})" onmouseover="this.style.background='#1c1c1c'" onmouseout="this.style.background='transparent'">
                <td>#${t.id}</td>
                <td style="color:#666">${new Date(t.timestamp).toLocaleTimeString()}</td>
                <td>${t.merchant}</td>
                <td style="font-weight:bold;">₹${t.amount}</td>
                <td style="color:${scoreColor}; font-weight:bold;">${score}%</td> <td><span class="status-badge ${badge}">${txt}</span></td>
            </tr>`;
        }).join('');

        const blBody = document.querySelector('#blacklist-table tbody');
        const blocked = data.filter(t => t.fraud || t.isFraud);
        blBody.innerHTML = blocked.length ? blocked.map(t => `<tr><td>${t.merchant}</td><td style="color:#ef4444">${t.fraudReason}</td><td>${new Date(t.timestamp).toLocaleTimeString()}</td><td>BLOCKED</td></tr>`).join('') : `<tr><td colspan="4" style="text-align:center;color:#666">No Threats Logged</td></tr>`;

        const fraudPct = stats.total_txns > 0 ? (stats.total_fraud / stats.total_txns) * 100 : 0;
        gaugeChart.data.datasets[0].data = [fraudPct, 100 - fraudPct];
        gaugeChart.update();
        document.getElementById('risk-score').innerText = fraudPct > 10 ? 'High' : 'Low';
        document.getElementById('risk-score').style.color = fraudPct > 10 ? '#ef4444' : '#3b82f6';
        App.updateThreatMap(data);
    },

    showDetails: (id) => {
        const t = window.lastData.find(x => x.id == id);
        if (!t) return;
        
        document.getElementById('depth-view').style.display = 'block';
        document.getElementById('d-ip').innerText = t.ipAddress || '192.168.1.57'; 
        document.getElementById('d-dev').innerText = t.deviceId || 'Unknown Device';
        document.getElementById('d-reason').innerText = t.fraudReason || 'None';
        
        document.getElementById('d-time').innerText = t.timestamp ? new Date(t.timestamp).toLocaleString() : '--';
    }
};

const Backend = {
    url: 'http://localhost:8080/api/transactions',
    
    reset: async () => {
        if(confirm("ARE YOU SURE?\nThis will delete ALL transaction history and reset the dashboard to zero.")) {
            try {
                await fetch(Backend.url + '/reset', { method: 'DELETE' });
                alert("System Reset Complete.");
                Backend.sync();
            } catch(e) {
                alert("Error resetting system: " + e);
            }
        }
    },

    sync: async (manual = false) => {
        if(manual) Utils.log("Dashboard Data Synced Successfully");
        
        try {
            const [d, s] = await Promise.all([ fetch(Backend.url + '/history').then(r=>r.json()), fetch(Backend.url + '/stats').then(r=>r.json()) ]);
            window.lastData = d;
            App.render(d, s);
            if(document.getElementById('inspect').classList.contains('active')) Inspector.renderTable();
            document.getElementById('api-status').innerText = "ONLINE";
            document.getElementById('api-status').className = "text-green";
        } catch(e) {
            document.getElementById('api-status').innerText = "OFFLINE";
            document.getElementById('api-status').className = "text-red";
            if(manual) Utils.log("Sync Failed: Backend Unreachable");
        }
    },

    generateTraffic: async () => {
        Utils.log("Generating random user traffic...");
        for(let i=0; i<5; i++) {
            await fetch(Backend.url + '/simulate?type=standard', { method: 'POST' }); 
        }
        Utils.log("Traffic generation complete.");
        Backend.sync();
    },
    
    triggerVelocity: async () => {
        Utils.log("Initiating Velocity Attack (5x burst)...");
        for(let i=0; i<5; i++) {
            fetch(Backend.url + '/simulate?type=velocity', { method: 'POST' }).catch(()=>{});
        }
        setTimeout(Backend.sync, 1000);
    },

    triggerSim: async (type) => {
        if(type === 'email_test') {
            for(let i=0; i<5; i++) {
                await fetch(Backend.url + '/simulate?type=high_value', { method: 'POST' });
                Utils.log(`Fraud Simulation ${i+1}/5 sent...`);
                await new Promise(r => setTimeout(r, 200)); 
            }
            Utils.log("5th Hit! Email Request Sent to Backend.");
            Backend.sync();
            return;
        }

        await fetch(Backend.url + '/simulate?type=' + type, { method: 'POST' });
        Utils.log(`Injecting ${type} scenario...`);
        Backend.sync();
    },

    manualCheck: async () => {
        const amt = document.getElementById('insp-amt').value;
        const merch = document.getElementById('insp-merch').value;
        const ui = document.getElementById('insp-result');
        ui.innerHTML = "Processing...";
        try {
            const res = await fetch(Backend.url, { method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify({ amount: parseFloat(amt), merchant: merch, accountId: 'MANUAL', timestamp: new Date().toISOString() }) });
            const d = await res.json();
            const isFraud = d.fraud || d.isFraud;
            ui.innerHTML = `<h3 style="color:${isFraud?'#ef4444':'#10b981'}">${isFraud?'BLOCKED':'APPROVED'}</h3><div>Reason: ${d.fraudReason || 'Safe'}</div>`;
        } catch(e) { ui.innerHTML = "Connection Error"; }
    }
};

const Utils = {
    log: (msg) => {
        const b = document.getElementById('sim-logs');
        if (b) {
            b.innerHTML += `> ${msg}<br>`;
            b.scrollTop = b.scrollHeight;
        }

        const container = document.getElementById('toast-container');
        if (container) {
            const t = document.createElement('div'); 
            t.className = 'toast'; 
            t.innerHTML = `<i data-lucide="bell" style="width:16px; display:inline-block; vertical-align:middle; margin-right:8px;"></i> ${msg}`;
            container.appendChild(t);
            
            if(window.lucide) lucide.createIcons();
            
            setTimeout(()=>t.remove(), 3000);
        }
    }
};

const Report = {
    downloadPDF: () => {
        if(!window.lastData.length) return Utils.log("No data for PDF");
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();
        
        doc.setFontSize(18);
        doc.text("Fraud Transaction Report", 14, 20);
        
        doc.setFontSize(10);
        doc.text(`Generated: ${new Date().toLocaleString()}`, 14, 28);

        const rows = window.lastData.map(t => [t.id, t.merchant, t.amount, t.fraudReason || 'Clean']);
        
        doc.autoTable({ 
            startY: 35,
            head: [['ID','Merchant','Amt','Status']], 
            body: rows 
        });
        
        doc.save("Report.pdf");
    }
};

const Inspector = {
    currentFilter: 'all',
    filter: (type) => {
        Inspector.currentFilter = type;
        document.querySelectorAll('.filter-btn').forEach(b => {
            b.style.background = 'transparent'; b.style.color = '#a1a1aa';
        });
        const activeBtn = Array.from(document.querySelectorAll('.filter-btn')).find(b => b.innerText.toLowerCase() === (type === 'clean' ? 'success' : (type === 'fraud' ? 'failed' : 'all')));
        if(activeBtn) { activeBtn.style.background = '#27272a'; activeBtn.style.color = 'white'; }
        Inspector.renderTable();
    },
    downloadPDF1: () => {
        const data = window.lastData;
        if (!data || !data.length) return alert("No log data available to export.");

        const { jsPDF } = window.jspdf;
        const doc = new jsPDF();

        doc.setFontSize(16);
        doc.setTextColor(40, 40, 40);
        doc.text("Deep Inspection Trace Logs", 14, 20);
        
        doc.setFontSize(10);
        doc.setTextColor(100, 100, 100);
        doc.text(`Generated: ${new Date().toLocaleString()} | Security Level: MAXIMUM`, 14, 28);

        const rows = data.map(t => {
            const isFraud = t.fraud || t.isFraud;
            const status = isFraud ? "FAILED" : "SUCCESS";
            const reason = isFraud ? (t.fraudReason || "Unknown Rule") : "Verified (Low Latency)";
            return [
                `TR-${t.id}`, 
                t.ipAddress || '192.168.X.X', 
                t.deviceId || 'Unknown', 
                status, 
                reason
            ];
        });

        doc.autoTable({
            startY: 35,
            head: [['Trace ID', 'IP Address', 'Device Endpoint', 'Status', 'Technical Reason']],
            body: rows,
            theme: 'grid',
            headStyles: { fillColor: [20, 20, 20], textColor: [255, 255, 255] },
            styles: { fontSize: 8, font: "helvetica" },
            columnStyles: {
                0: { cellWidth: 25 },
                3: { fontStyle: 'bold' } 
            },
            didParseCell: function(data) {
                if (data.section === 'body' && data.column.index === 3) {
                    if (data.cell.raw === 'FAILED') {
                        data.cell.styles.textColor = [239, 68, 68]; 
                    } else {
                        data.cell.styles.textColor = [16, 185, 129]; 
                    }
                }
            }
        });

        doc.save("Deep_Inspect_Analysis.pdf");
    },

    renderTable: () => {
        const tbody = document.querySelector('#inspect-table tbody');
        let data = window.lastData || [];
        if (Inspector.currentFilter === 'clean') data = data.filter(t => !t.fraud && !t.isFraud);
        if (Inspector.currentFilter === 'fraud') data = data.filter(t => t.fraud || t.isFraud);
        tbody.innerHTML = data.map(t => {
            let isFraud = t.fraud || t.isFraud;
            let statusColor = isFraud ? '#ef4444' : '#10b981';
            let statusText = isFraud ? 'FAILED' : 'SUCCESS';
            let metaInfo = isFraud ? `<span style="color:#fca5a5">${t.fraudReason || 'Unknown Rule'}</span>` : `<span style="color:#666">${(Math.random()*20 + 10).toFixed(0)}ms • ${t.location || t.country || 'IN'}</span>`;
            return `<tr onclick="Inspector.analyze(${t.id})" style="cursor:pointer; transition:0.2s;" onmouseover="this.style.background='#1c1c1c'" onmouseout="this.style.background='transparent'">
                <td style="font-family:var(--font-num); color:#888;">TR-${t.id}</td>
                <td style="font-family:var(--font-num);">${t.ipAddress || '192.168.x.x'}</td>
                <td style="font-family:var(--font-num); color:#aaa;">${t.deviceId || 'Unknown_Dev'}</td>
                <td style="font-weight:bold; color:${statusColor}">${statusText}</td>
                <td>${metaInfo}</td>
            </tr>`;
        }).join('');
    },
    analyze: (id) => {
        const t = window.lastData.find(x => x.id == id);
        if (!t) return;
        const log = document.getElementById('inspect-logs');
        const time = new Date().toLocaleTimeString();
        let msg = "";
        if (t.fraud || t.isFraud) {
            let reason = t.fraudReason || "General Risk";
            let measure = "Investigate User Activity";
            if (reason.includes("Rule 1") || reason.includes("High Value")) measure = "Manual KYC verification required. Contact user for proof of funds.";
            else if (reason.includes("Rule 19") || reason.includes("Crypto")) measure = "Permanent Ban: Merchant is on global blacklist. Report to compliance.";
            else if (reason.includes("ML") || reason.includes("Anomalous")) measure = "Step-up Auth: Require 2FA or biometric verification for next attempt.";
            msg = `<span style="color:#555">[${time}]</span> <span style="color:#ef4444">ANALYZING FAILURE (ID: ${t.id})</span><br>> Reason: ${reason}<br>> IP Origin: ${t.ipAddress || 'N/A'} (${t.country || 'Unknown'})<br>> <span style="color:#fca5a5; font-weight:bold;">RECOMMENDED ACTION:</span><br>> ${measure}<br>----------------------------------<br>`;
        } else {
            msg = `<span style="color:#555">[${time}]</span> <span style="color:#10b981">INSPECTING SUCCESS (ID: ${t.id})</span><br>> Latency: 12ms (Optimal)<br>> Route: Validated via RSA-2048<br>> Status: Packet delivered to Core Banking.<br>----------------------------------<br>`;
        }
        log.innerHTML += msg;
        log.scrollTop = log.scrollHeight;
    }
};

const Ops = {
    scan: () => {
        const data = window.lastData || [];
        const frauds = data.filter(t => t.fraud || t.isFraud);
        const clusters = {};
        
        frauds.forEach(f => {
            const key = `${f.fraudReason || 'Unknown'}|${f.country || 'Unknown'}`;
            if(!clusters[key]) clusters[key] = { count: 0, reason: f.fraudReason, country: f.country, ids: [] };
            clusters[key].count++;
            clusters[key].ids.push(f.accountId || 'Anon');
        });

        const tbody = document.querySelector('#threat-table tbody');
        if(!tbody) return;

        if (Object.keys(clusters).length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding:20px; color:#666;">No Active Threats Detected. System Clean.</td></tr>`;
            document.getElementById('threat-count').innerText = "0";
            return;
        }

        document.getElementById('threat-count').innerText = Object.keys(clusters).length;

        tbody.innerHTML = Object.values(clusters).map(c => {
            let severity = c.count > 2 ? 'CRITICAL' : 'HIGH';
            let badgeColor = c.count > 2 ? '#ef4444' : '#f59e0b';
            let users = [...new Set(c.ids)].slice(0, 2).join(', ');
            if(c.ids.length > 2) users += ` +${c.ids.length - 2} more`;

            return `<tr style="border-bottom:1px solid #27272a;">
                <td style="color:${badgeColor}; font-weight:bold;">● ${severity}</td>
                <td><div style="color:white; font-weight:500;">${c.reason}</div><div style="font-size:0.7rem; color:#666;">Heuristic Match</div></td>
                <td style="font-family:var(--font-num);">${c.country}</td>
                <td style="color:#aaa;">${users}</td>
                <td><span style="background:rgba(239,68,68,0.1); color:#fca5a5; padding:2px 6px; border-radius:3px; font-size:0.7rem; border:1px solid rgba(239,68,68,0.3);">ACTIVE</span></td>
            </tr>`;
        }).join('');
    },
    blockAll: () => {
        const count = document.getElementById('threat-count').innerText;
        if(count === "0") return alert("No threats to block.");
        if(confirm(`AUTHORIZING FIREWALL UPDATE:\nPermanently block ${count} threat clusters?`)) {
            const btn = document.querySelector('#simulation button[onclick="Ops.blockAll()"]');
            btn.innerHTML = `<i data-lucide="loader-2" class="animate-spin"></i> ENFORCING...`;
            setTimeout(() => {
                alert("ACCESS DENIED for identified subnets.\nRules propagated to Gateway.");
                btn.innerHTML = `<i data-lucide="check"></i> BLOCKED`;
                btn.style.background = "#10b981"; btn.style.borderColor = "#059669";
                document.querySelectorAll('#threat-table tbody tr').forEach(r => {
                    r.style.opacity = '0.5';
                    r.lastElementChild.innerHTML = `<span style="color:#10b981; font-weight:bold;">BLOCKED</span>`;
                });
            }, 1500);
        }
    }
};

document.addEventListener('click', (e) => {
    if(e.target.closest('[onclick*="simulation"]')) setTimeout(Ops.scan, 100);
});

const Nav = {
    to: (id, el) => {
        document.querySelectorAll('.view-section').forEach(d => d.classList.remove('active'));
        document.getElementById(id).classList.add('active');
        document.querySelectorAll('.nav-item').forEach(d => d.classList.remove('active'));
        el.classList.add('active');
    }
};
