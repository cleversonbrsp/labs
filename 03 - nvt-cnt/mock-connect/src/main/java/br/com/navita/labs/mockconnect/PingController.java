package br.com.navita.labs.mockconnect;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint /portal/ping para startupProbe e readinessProbe no Deployment.
 * GET /ping com context-path /portal = /portal/ping. Retorna lab.profile e
 * lab.env vindos do Config Server (portal-demo-kind.properties).
 */
@RestController
public class PingController {

    @Value("${lab.profile:unknown}")
    private String labProfile;

    @Value("${lab.env:local}")
    private String labEnv;

    /**
     * Página inicial do /portal: evita 404 ao acessar http://localhost:9090/portal/
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return "<!DOCTYPE html><html lang=\"pt-BR\"><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<title>Portal Demo — Lab 03</title>"
            + "<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\"><link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>"
            + "<link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;500;600;700&display=swap\" rel=\"stylesheet\">"
            + "<style>"
            + ":root{--bg:#0f1419;--surface:#1a2332;--surface-hover:#243044;--accent:#3b82f6;--success:#22c55e;--text:#e2e8f0;--text-muted:#94a3b8;--border:#334155}"
            + "*{box-sizing:border-box;margin:0;padding:0}"
            + "body{font-family:'Outfit',sans-serif;background:var(--bg);color:var(--text);min-height:100vh;display:flex;align-items:center;justify-content:center;padding:2rem;"
            + "background-image:radial-gradient(ellipse 80% 50% at 50% -20%,rgba(59,130,246,0.12),transparent)}"
            + ".container{max-width:520px;width:100%}"
            + ".card{background:var(--surface);border:1px solid var(--border);border-radius:16px;padding:2rem;box-shadow:0 4px 24px rgba(0,0,0,0.25)}"
            + ".badge{display:inline-flex;align-items:center;gap:.5rem;font-size:.75rem;font-weight:600;color:var(--success);background:rgba(34,197,94,0.12);padding:.35rem .75rem;border-radius:999px;margin-bottom:1.5rem}"
            + ".badge::before{content:'';width:6px;height:6px;background:var(--success);border-radius:50%;animation:pulse 2s ease-in-out infinite}"
            + "@keyframes pulse{0%,100%{opacity:1}50%{opacity:.5}}"
            + "h1{font-size:1.75rem;font-weight:700;letter-spacing:-.02em;margin-bottom:.5rem;background:linear-gradient(135deg,#fff 0%,#94a3b8 100%);-webkit-background-clip:text;-webkit-text-fill-color:transparent;background-clip:text}"
            + ".subtitle{font-size:.95rem;color:var(--text-muted);font-weight:300;margin-bottom:1.75rem;line-height:1.5}"
            + ".grid{display:flex;flex-direction:column;gap:.75rem}"
            + ".row{display:flex;align-items:center;justify-content:space-between;padding:.9rem 1rem;background:rgba(255,255,255,0.03);border:1px solid var(--border);border-radius:10px;transition:background .2s,border-color .2s}"
            + ".row:hover{background:var(--surface-hover);border-color:rgba(59,130,246,0.3)}"
            + ".row-label{font-size:.8rem;color:var(--text-muted);font-weight:500}"
            + ".row-value{font-size:.9rem;font-weight:600;color:var(--text)}"
            + ".row-value a{color:var(--accent);text-decoration:none;font-weight:500}"
            + ".row-value a:hover{text-decoration:underline}"
            + ".footer{margin-top:1.5rem;font-size:.75rem;color:var(--text-muted);text-align:center}"
            + "</style></head><body><div class=\"container\"><div class=\"card\">"
            + "<div class=\"badge\">Aplicação no ar</div>"
            + "<h1>Portal Demo (Lab)</h1>"
            + "<p class=\"subtitle\">Lab 03 – fluxo Connect no k8s Kind cluster. Aplicação está no ar.</p>"
            + "<div class=\"grid\">"
            + "<div class=\"row\"><span class=\"row-label\">Status</span><span class=\"row-value\"><a href=\"/portal/ping\">/portal/ping</a></span></div>"
            + "<div class=\"row\"><span class=\"row-label\">Perfil (Config Server)</span><span class=\"row-value\">" + labProfile + "</span></div>"
            + "<div class=\"row\"><span class=\"row-label\">Ambiente</span><span class=\"row-value\">" + labEnv + "</span></div>"
            + "</div><p class=\"footer\">Kind cluster · Config Client</p></div></div></body></html>";
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> body = new HashMap<>();
        body.put("status", "UP");
        body.put("app", "portal-demo");
        body.put("lab.profile", labProfile);
        body.put("lab.env", labEnv);
        return ResponseEntity.ok(body);
    }
}
