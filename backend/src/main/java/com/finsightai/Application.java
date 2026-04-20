package com.finsightai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Класс Application является точкой входа для серверного приложения FinSight AI.
 * Это минимальное приложение Spring Boot служит основой, на которой будет
 * построена остальная часть backend.  На данном этапе приложение
 * предоставляет только одну точку проверки работоспособности; на последующих
 * этапах будут добавлены аутентификация, обработка загрузки PDF,
 * взаимодействие с базой данных и контроллеры API.
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}