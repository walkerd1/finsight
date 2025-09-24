#!/usr/bin/env python3
import argparse, json, csv
from datetime import datetime, date
from http.server import HTTPServer, BaseHTTPRequestHandler

def load_state(state_path):
    try:
        with open(state_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception:
        return {}

def load_time_log(log_path):
    total_today = 0
    total_7d = 0
    today = date.today().isoformat()
    cutoff = datetime.utcnow().date().toordinal() - 6
    try:
        with open(log_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for r in reader:
                d = r.get('date','')
                mins = float(r.get('minutes','0') or 0)
                try:
                    d_ord = datetime.fromisoformat(d).date().toordinal()
                except Exception:
                    continue
                if d == today:
                    total_today += mins
                if d_ord >= cutoff:
                    total_7d += mins
    except Exception:
        pass
    return total_today, total_7d

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path != '/metrics':
            self.send_response(404); self.end_headers(); return
        st = load_state(self.server.state_path)
        today_mins, week_mins = load_time_log(self.server.time_log_path)
        cash = float(st.get('cash_on_hand_usd', 0))
        monthly_burn = float(st.get('monthly_burn_usd', 0.01))
        income_monthly = float(st.get('expected_income_monthly_usd', 0))
        net_burn = max(monthly_burn - income_monthly, 0.01)
        daily_burn = net_burn / 30.0
        runway_days = cash / daily_burn if daily_burn > 0 else float('inf')
        lines = []
        def m(name, value, help_text):
            lines.append(f"# HELP {name} {help_text}")
            lines.append(f"# TYPE {name} gauge")
            val = "NaN" if value is None else (value if isinstance(value, (int,float)) else 0)
            lines.append(f"{name} {val}")
        m("finsight_cash_on_hand_usd", cash, "Cash on hand in USD")
        m("finsight_burn_rate_monthly_usd", monthly_burn, "Estimated monthly burn in USD")
        m("finsight_expected_income_monthly_usd", income_monthly, "Expected monthly income in USD")
        m("finsight_burn_rate_daily_usd", daily_burn, "Estimated daily burn in USD")
        m("finsight_runway_days", runway_days, "Runway in days based on cash_on_hand and net burn")
        m("finsight_dev_minutes_today", today_mins, "Minutes spent on FinSight today")
        m("finsight_dev_minutes_7d", week_mins, "Minutes spent on FinSight in the last 7 days")
        m("finsight_office_coffee_cups_today", 0, "Cups of coffee consumed today")
        m("finsight_office_coffee_beans_lb", 0, "Coffee beans on hand (lb)")
        m("finsight_hardware_cost_usd", 0, "Total cost of active hardware in USD")
        body = "\n".join(lines).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'text/plain; version=0.0.4')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)

def main():
    ap = argparse.ArgumentParser(description="FinSight LifeOps Metrics Exporter")
    ap.add_argument("--state", required=True, help="Path to lifeops_state.json")
    ap.add_argument("--time-log", required=True, help="Path to time_log.csv")
    ap.add_argument("--port", type=int, default=9105, help="Port to serve /metrics")
    args = ap.parse_args()
    httpd = HTTPServer(("0.0.0.0", args.port), Handler)
    httpd.state_path = args.state
    httpd.time_log_path = args.time_log
    print(f"Serving /metrics on :{args.port}")
    httpd.serve_forever()

if __name__ == "__main__":
    main()
