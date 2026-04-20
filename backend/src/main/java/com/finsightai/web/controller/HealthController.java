package com.finsightai.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Контроллер HealthController предоставляет простой HTTP‑эндпоинт для
 * проверки того, что серверная часть работает.  Он возвращает JSON с
 * фиксированным сообщением.  В дальнейшем этот контроллер будет
 * расширен или дополнен другими контроллерами, реализующими функционал,
 * описанный в техническом задании.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}