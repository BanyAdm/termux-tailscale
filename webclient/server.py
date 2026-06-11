import http.server
import json
import os
import subprocess
import urllib.request

PORT = 5252
WEB_DIR = os.path.dirname(os.path.abspath(__file__))
API_KEY_FILE = os.path.expanduser("~/.config/tailscale/api_key")

def get_ip():
    try:
        out = subprocess.check_output("ifconfig 2>/dev/null", shell=True).decode()
        in_tun = False
        for line in out.splitlines():
            if line.startswith("tun"):
                in_tun = True
            if in_tun and "inet " in line:
                parts = line.split()
                for i, p in enumerate(parts):
                    if p == "inet":
                        return parts[i+1]
    except:
        pass
    return None

def get_hostname():
    ip = get_ip()
    if ip:
        for d in get_devices():
            ips = d.get("addresses", [])
            dip = ips[0].split("/")[0] if ips else ""
            if dip == ip:
                return d.get("hostname", "android")
    try:
        return subprocess.check_output("hostname", shell=True).decode().strip()
    except:
        return "android"

def is_connected():
    return get_ip() is not None

def get_api_key():
    if os.path.exists(API_KEY_FILE):
        return open(API_KEY_FILE).read().strip()
    return None

def get_devices():
    key = get_api_key()
    if not key:
        return []
    try:
        req = urllib.request.Request(
            "https://api.tailscale.com/api/v2/tailnet/-/devices",
            headers={"Authorization": f"Bearer {key}"}
        )
        with urllib.request.urlopen(req, timeout=5) as r:
            return json.loads(r.read()).get("devices", [])
    except:
        return []

def get_tailnet_name():
    devices = get_devices()
    for d in devices:
        user = d.get("user", "")
        if user:
            return user
    return "tailnet"

def get_current_user():
    devices = get_devices()
    if devices:
        user = devices[0].get("user", "unknown")
        return {"DisplayName": user, "loginName": user}
    return {"DisplayName": "unknown", "loginName": "unknown"}

def build_data():
    ip = get_ip()
    hostname = get_hostname()
    connected = ip is not None
    devices = get_devices()
    tailnet = get_tailnet_name()
    user = get_current_user()

    my_device = None
    peers = []
    for d in devices:
        ips = d.get("addresses", [])
        dip = ips[0].split("/")[0] if ips else ""
        if dip == ip:
            my_device = d
        else:
            peers.append(d)
    my_ipv6 = ""
    if my_device:
        addrs = my_device.get("addresses", [])
        for a in addrs:
            aip = a.split("/")[0]
            if ":" in aip:
                my_ipv6 = aip
                break
    key_expiry = ""
    key_expiry_disabled = True
    if my_device:
        key_expiry_disabled = my_device.get("keyExpiryDisabled", False)
        key_expiry = my_device.get("expires", "")

    return {
        "Status": "Running" if connected else "Stopped",
        "DeviceName": hostname,
        "TailnetName": get_current_user().get("loginName", tailnet),
        "DomainName": tailnet,
        "IP": ip or "",
        "IPv4": ip or "",
        "IPv6": my_ipv6,
        "IsOperator": True,
        "ClientVersion": {"RunningLatest": True},
        "URLQueryParams": "",
        "ManagedBy": "",
        "ManagedByOrganization": "",
        "CurrentUser": user,
        "Profile": user,
        "IsTagged": False,
        "Tags": [],
        "ControlAdminURL": "https://login.tailscale.com/admin",
        "OS": "android",
        "NodeID": my_device.get("nodeId", "") if my_device else "",
        "KeyExpiry": "" if key_expiry_disabled else key_expiry,
        "KeyExpiryDisabled": key_expiry_disabled,
        "ID": my_device.get("id", "") if my_device else "",
        "IPNVersion": my_device.get("clientVersion", "unknown") if my_device else "unknown",
        "TUNMode": connected,
        "IsSynology": False,
        "DSMVersion": "",
        "LicensesURL": "https://tailscale.com/licenses",
        "Self": {
            "ID": my_device.get("id", "") if my_device else "",
            "HostName": hostname,
            "DNSName": hostname,
            "TailscaleIPs": [ip] if ip else [],
            "ControlAdminURL": "https://login.tailscale.com/admin",
            "Online": connected,
            "ExitNode": False,
            "ExitNodeOption": False,
            "advertise-exit-node": False,
            "AdvertisingExitNode": False,
            "AdvertisingExitNodeApproved": False,
            "UsingExitNode": None,
            "IsSynology": False,
            "ClientVersion": {"RunningLatest": True},
            "DSMVersion": "",
            "LicensesURL": "https://tailscale.com/licenses"
        },
        "Features": {
            "advertise-exit-node": True,
            "use-exit-node": True,
            "advertise-routes": False,
            "ssh": False,
            "auto-update": False
        },
        "ExitNodeStatus": None,
        "Health": [],
        "TKAEnabled": False,
        "AdvertisedRoutes": None,
        "SubnetRoutes": None,
        "AppConnector": {"Advertised": False},
        "RunExitNode": False,
        "SSHIsEnabled": False,
        "Peers": []
    }

def build_exit_nodes():
    devices = get_devices()
    nodes = []
    for d in devices:
        if "0.0.0.0/0" in d.get("advertisedRoutes", []):
            ips = d.get("addresses", [])
            ip = ips[0].split("/")[0] if ips else ""
            nodes.append({
                "ID": d.get("id", ""),
                "Name": d.get("hostname", ""),
                "Online": d.get("connectedToControl", False),
                "Location": None
            })
    return nodes

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIR, **kwargs)

    def log_message(self, format, *args):
        pass

    def do_GET(self):
        path = self.path.split("?")[0]
        if path == "/api/auth":
            user = get_current_user()
            self.send_json({
                "authNeeded": None,
                "canManageNode": True,
                "self": {
                    "loginName": user.get("loginName", "unknown"),
                    "displayName": user.get("DisplayName", "unknown"),
                    "profilePicURL": ""
                }
            })
        elif path == "/exit-nodes":
            self.send_json(build_exit_nodes())
        elif path in ("/data", "/api/data"):
            self.send_json(build_data())
        elif path.endswith(".css"):
            filepath = path.lstrip("/")
            full = os.path.join(WEB_DIR, filepath)
            with open(full, "rb") as f:
                data = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/css")
            self.send_header("Content-Encoding", "gzip")
            self.send_header("Content-Length", len(data))
            self.end_headers()
            self.wfile.write(data)
        else:
            if path == "/" or not path.startswith("/assets"):
                self.path = "/index.html"
            super().do_GET()

    def do_POST(self):
        path = self.path.split("?")[0]
        length = int(self.headers.get("Content-Length", 0))
        body = json.loads(self.rfile.read(length)) if length else {}
        if path == "/api/up":
            import subprocess, time
            subprocess.run("am broadcast -a com.tailscale.ipn.CONNECT_VPN -n com.tailscale.ipn/.TermuxPluginReceiver 2>/dev/null", shell=True)
            time.sleep(5)
            self.send_json({"ok": True})
        elif path == "/api/down":
            import subprocess, time
            subprocess.run("am broadcast -a com.tailscale.ipn.DISCONNECT_VPN -n com.tailscale.ipn/.TermuxPluginReceiver 2>/dev/null", shell=True)
            time.sleep(2)
            self.send_json({"ok": True})
        elif path == "/api/device-details-click":
            self.send_json({"ok": True})
        elif path in ("/local/v0/prefs", "/api/prefs"):
            self.send_json({"ok": True})
        else:
            self.send_json({"ok": True})

    def send_json(self, data, status=200):
        body = json.dumps(data).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", len(body))
        self.end_headers()
        self.wfile.write(body)

if __name__ == "__main__":
    ip = get_ip()
    if not ip:
        print("Not connected to Tailscale. Starting anyway...")
        bind = "0.0.0.0"
    else:
        bind = "0.0.0.0"
    print(f"Web client running at http://{ip or '127.0.0.1'}:{PORT}")
    http.server.HTTPServer((bind, PORT), Handler).serve_forever()
