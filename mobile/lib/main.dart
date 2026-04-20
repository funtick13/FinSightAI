import 'package:flutter/material.dart';

void main() {
  runApp(const FinSightApp());
}

/// FinSightApp — минимальное Flutter‑приложение, служащее отправной
/// точкой для проекта FinSight AI.  Сейчас оно отображает простой
/// домашний экран с приветственным сообщением.  В дальнейшем это будет
/// заменено полноценным потоком приложения (аутентификация, загрузка
/// выписок, аналитическая панель и т.д.).
class FinSightApp extends StatelessWidget {
  const FinSightApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FinSight AI',
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatelessWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('FinSight AI'),
      ),
      body: const Center(
        child: Text('Добро пожаловать в FinSight AI'),
      ),
    );
  }
}