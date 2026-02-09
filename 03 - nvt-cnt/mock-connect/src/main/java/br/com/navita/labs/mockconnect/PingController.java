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
            + ".diagram-section{margin-top:1.75rem;padding-top:1.5rem;border-top:1px solid var(--border)}"
            + ".diagram-title{font-size:.8rem;font-weight:600;color:var(--text-muted);margin-bottom:1rem;letter-spacing:.05em;text-transform:uppercase}"
            + ".diagram-wrap{background:rgba(0,0,0,0.2);border-radius:12px;padding:1rem;overflow:auto}"
            + ".diagram-wrap svg{display:block;margin:0 auto;max-width:100%;height:auto}"
            + "</style></head><body><div class=\"container\"><div class=\"card\">"
            + "<div class=\"badge\">Aplicação no ar</div>"
            + "<h1>Portal Demo (Lab)</h1>"
            + "<p class=\"subtitle\">Lab 03 – fluxo Connect no k8s Kind cluster. Aplicação está no ar.</p>"
            + "<div class=\"diagram-section\"><p class=\"diagram-title\">Fluxo Connect no Kind</p><div class=\"diagram-wrap\">"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 440 200\" font-family=\"Outfit,sans-serif\" font-size=\"11\">"
            + "<defs>"
            + "<linearGradient id=\"kg\" x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\"><stop offset=\"0%\" stop-color=\"#334155\"/><stop offset=\"100%\" stop-color=\"#1e293b\"/></linearGradient>"
            + "<marker id=\"ar\" markerWidth=\"8\" markerHeight=\"8\" refX=\"6\" refY=\"4\" orient=\"auto\"><path d=\"M0 0L8 4L0 8z\" fill=\"#64748b\"/></marker>"
            + "</defs>"
            + "<rect x=\"10\" y=\"10\" width=\"420\" height=\"180\" rx=\"10\" fill=\"none\" stroke=\"#334155\" stroke-width=\"1.5\" stroke-dasharray=\"6 4\"/>"
            + "<text x=\"220\" y=\"28\" text-anchor=\"middle\" fill=\"#94a3b8\" font-weight=\"600\" font-size=\"10\">Kind cluster</text>"
            + "<rect x=\"30\" y=\"50\" width=\"90\" height=\"52\" rx=\"6\" fill=\"#1e293b\" stroke=\"#475569\" stroke-width=\"1\"/>"
            + "<text x=\"75\" y=\"72\" text-anchor=\"middle\" fill=\"#e2e8f0\" font-weight=\"600\">ConfigMap</text>"
            + "<text x=\"75\" y=\"88\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">portal-demo</text>"
            + "<text x=\"75\" y=\"98\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">-kind.properties</text>"
            + "<path d=\"M125 76 L175 76\" stroke=\"#64748b\" stroke-width=\"1.5\" marker-end=\"url(#ar)\"/>"
            + "<text x=\"150\" y=\"70\" text-anchor=\"middle\" fill=\"#64748b\" font-size=\"9\">lê</text>"
            + "<rect x=\"180\" y=\"50\" width=\"100\" height=\"52\" rx=\"6\" fill=\"#1e293b\" stroke=\"#3b82f6\" stroke-width=\"1\"/>"
            + "<text x=\"230\" y=\"72\" text-anchor=\"middle\" fill=\"#e2e8f0\" font-weight=\"600\">Config Server</text>"
            + "<text x=\"230\" y=\"88\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">Spring Cloud</text>"
            + "<text x=\"230\" y=\"98\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">(native)</text>"
            + "<path d=\"M285 76 L335 76\" stroke=\"#64748b\" stroke-width=\"1.5\" marker-end=\"url(#ar)\"/>"
            + "<text x=\"310\" y=\"70\" text-anchor=\"middle\" fill=\"#64748b\" font-size=\"9\">bootstrap</text>"
            + "<rect x=\"340\" y=\"50\" width=\"90\" height=\"52\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"385\" y=\"72\" text-anchor=\"middle\" fill=\"#e2e8f0\" font-weight=\"600\">portal-demo</text>"
            + "<text x=\"385\" y=\"88\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">Config Client</text>"
            + "<text x=\"385\" y=\"98\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"9\">/portal · /ping</text>"
            + "<rect x=\"30\" y=\"155\" width=\"80\" height=\"28\" rx=\"4\" fill=\"rgba(59,130,246,0.15)\" stroke=\"#3b82f6\" stroke-width=\"1\"/>"
            + "<text x=\"70\" y=\"173\" text-anchor=\"middle\" fill=\"#93c5fd\" font-size=\"9\">navegador</text>"
            + "<path d=\"M110 169 L230 169 L230 76 L340 76\" stroke=\"#3b82f6\" stroke-width=\"1.2\" stroke-dasharray=\"3 2\" marker-end=\"url(#ar)\"/>"
            + "<text x=\"175\" y=\"163\" text-anchor=\"middle\" fill=\"#3b82f6\" font-size=\"9\">port-forward</text>"
            + "</svg></div></div>"
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
