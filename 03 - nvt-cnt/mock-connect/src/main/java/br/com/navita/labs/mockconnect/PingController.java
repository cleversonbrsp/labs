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
            + ".container{max-width:720px;width:100%}"
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
            + ".diagram-intro{font-size:.85rem;color:var(--text-muted);margin-bottom:1rem;line-height:1.5}"
            + ".diagram-title{font-size:.8rem;font-weight:600;color:var(--text-muted);margin-bottom:.5rem;letter-spacing:.05em;text-transform:uppercase}"
            + ".diagram-wrap{background:rgba(0,0,0,0.2);border-radius:12px;padding:1rem;overflow:auto;min-height:1px}"
            + ".diagram-wrap svg{display:block;margin:0 auto;max-width:100%;height:auto;min-width:280px}"
            + "</style></head><body><div class=\"container\"><div class=\"card\">"
            + "<div class=\"badge\">Aplicação no ar</div>"
            + "<h1>Portal Demo (Lab)</h1>"
            + "<p class=\"subtitle\">Lab 03 – fluxo Connect no k8s Kind cluster. Aplicação está no ar.</p>"
            + "<div class=\"diagram-section\"><p class=\"diagram-title\">Comparativo didático</p>"
            + "<p class=\"diagram-intro\">O lab replica o fluxo enterprise do repositório <strong>nvt-repos</strong> (Navita Connect) em ambiente local. À esquerda: fluxo original (OKE, OCIR, Git). À direita: o que fazemos aqui (Kind, ConfigMap, kind load).</p>"
            + "<div class=\"diagram-wrap\">"
            + "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 640 400\" font-family=\"Outfit,sans-serif\" font-size=\"10\">"
            + "<defs><marker id=\"ar\" markerWidth=\"8\" markerHeight=\"8\" refX=\"6\" refY=\"4\" orient=\"auto\"><path d=\"M0 0L8 4L0 8z\" fill=\"#64748b\"/></marker></defs>"
            + "<!-- Título -->"
            + "<text x=\"320\" y=\"22\" text-anchor=\"middle\" fill=\"#e2e8f0\" font-weight=\"600\" font-size=\"12\">Fluxo original (nvt-repos) vs Nosso lab (Kind)</text>"
            + "<!-- Coluna ORIGINAL -->"
            + "<text x=\"155\" y=\"48\" text-anchor=\"middle\" fill=\"#94a3b8\" font-weight=\"600\" font-size=\"9\">Fluxo original — nvt-repos</text>"
            + "<rect x=\"20\" y=\"58\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#475569\" stroke-width=\"1\"/>"
            + "<text x=\"155\" y=\"75\" text-anchor=\"middle\" fill=\"#e2e8f0\">1. navita-config-properties (Git)</text>"
            + "<text x=\"155\" y=\"86\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">connect.properties + connect-{perfil}.properties</text>"
            + "<path d=\"M155 92 L155 105\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"20\" y=\"108\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#475569\" stroke-width=\"1\"/>"
            + "<text x=\"155\" y=\"125\" text-anchor=\"middle\" fill=\"#e2e8f0\">2. Config Server (navita-config-server)</text>"
            + "<text x=\"155\" y=\"136\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Deploy OKE, aponta Git; configserver.navita.com.br</text>"
            + "<path d=\"M155 140 L155 153\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"20\" y=\"156\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#475569\" stroke-width=\"1\"/>"
            + "<text x=\"155\" y=\"173\" text-anchor=\"middle\" fill=\"#e2e8f0\">3. navita-connect: Maven → Docker → push OCIR</text>"
            + "<text x=\"155\" y=\"184\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Pipeline GitHub Actions</text>"
            + "<path d=\"M155 188 L155 201\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"20\" y=\"204\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#475569\" stroke-width=\"1\"/>"
            + "<text x=\"155\" y=\"221\" text-anchor=\"middle\" fill=\"#e2e8f0\">4. devops-config: deployment, service, hpa, ingress</text>"
            + "<text x=\"155\" y=\"232\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Manifestos K8s em repo separado</text>"
            + "<path d=\"M155 236 L155 249\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"20\" y=\"252\" width=\"270\" height=\"36\" rx=\"6\" fill=\"#1e293b\" stroke=\"#3b82f6\" stroke-width=\"1\"/>"
            + "<text x=\"155\" y=\"268\" text-anchor=\"middle\" fill=\"#e2e8f0\">5. OKE: kubectl apply → Pods</text>"
            + "<text x=\"155\" y=\"280\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">CLOUD_PROFILE → bootstrap Config Server → /portal</text>"
            + "<!-- Coluna LAB -->"
            + "<text x=\"485\" y=\"48\" text-anchor=\"middle\" fill=\"#22c55e\" font-weight=\"600\" font-size=\"9\">Nosso lab — 03 - nvt-cnt</text>"
            + "<rect x=\"350\" y=\"58\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"485\" y=\"75\" text-anchor=\"middle\" fill=\"#e2e8f0\">1. config-properties/ + ConfigMap</text>"
            + "<text x=\"485\" y=\"86\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">portal-demo.properties + portal-demo-kind.properties</text>"
            + "<path d=\"M485 92 L485 105\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"350\" y=\"108\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"485\" y=\"125\" text-anchor=\"middle\" fill=\"#e2e8f0\">2. Config Server (Kind, perfil native)</text>"
            + "<text x=\"485\" y=\"136\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Imagem springcloud/configserver; lê ConfigMap</text>"
            + "<path d=\"M485 140 L485 153\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"350\" y=\"156\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"485\" y=\"173\" text-anchor=\"middle\" fill=\"#e2e8f0\">3. mock-connect: Maven → Docker → kind load</text>"
            + "<text x=\"485\" y=\"184\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Sem registry; imagem no cluster local</text>"
            + "<path d=\"M485 188 L485 201\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"350\" y=\"204\" width=\"270\" height=\"32\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"485\" y=\"221\" text-anchor=\"middle\" fill=\"#e2e8f0\">4. manifests/: deployment, service (este repo)</text>"
            + "<text x=\"485\" y=\"232\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Namespace lab-portal; sem HPA/Ingress</text>"
            + "<path d=\"M485 236 L485 249\" stroke=\"#64748b\" stroke-width=\"1\" marker-end=\"url(#ar)\"/>"
            + "<rect x=\"350\" y=\"252\" width=\"270\" height=\"36\" rx=\"6\" fill=\"#1e293b\" stroke=\"#22c55e\" stroke-width=\"1\"/>"
            + "<text x=\"485\" y=\"268\" text-anchor=\"middle\" fill=\"#e2e8f0\">5. Kind: kubectl apply → port-forward</text>"
            + "<text x=\"485\" y=\"280\" text-anchor=\"middle\" fill=\"#94a3b8\" font-size=\"8\">Bootstrap Config Server → /portal · /ping (esta página)</text>"
            + "<!-- Seta central equivalencia -->"
            + "<line x1=\"290\" y1=\"290\" x2=\"350\" y2=\"290\" stroke=\"#475569\" stroke-width=\"1\" stroke-dasharray=\"4 2\"/>"
            + "<text x=\"320\" y=\"310\" text-anchor=\"middle\" fill=\"#64748b\" font-size=\"9\">mesmo fluxo: config → Config Server → app → cluster</text>"
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
