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
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Mock Connect - Lab</title></head>"
            + "<body><h1>Portal Demo (Lab)</h1>"
            + "<p>Lab 03 – fluxo Connect no k8s Kind cluster. Aplicação está no ar.</p>"
            + "<ul><li><strong>Status:</strong> <a href=\"/portal/ping\">/portal/ping</a></li>"
            + "<li><strong>Perfil (Config Server):</strong> " + labProfile + "</li>"
            + "<li><strong>Ambiente:</strong> " + labEnv + "</li></ul></body></html>";
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
