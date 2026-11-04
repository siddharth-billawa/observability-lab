from http.server import BaseHTTPRequestHandler, HTTPServer
class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        body = self.rfile.read(int(self.headers.get('Content-Length', '0')))
        print(body.decode('utf-8'), flush=True)
        self.send_response(200); self.end_headers(); self.wfile.write(b'ok')
HTTPServer(('0.0.0.0', 8090), Handler).serve_forever()
