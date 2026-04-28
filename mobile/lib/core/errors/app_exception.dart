import 'package:dio/dio.dart';

class AppException implements Exception {
  const AppException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  factory AppException.fromDio(DioException error) {
    final statusCode = error.response?.statusCode;
    final backendMessage = _extractMessage(error.response?.data);

    if (backendMessage != null && backendMessage.isNotEmpty) {
      return AppException(backendMessage, statusCode: statusCode);
    }

    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.connectionError:
        return const AppException('Не удалось подключиться к серверу');
      case DioExceptionType.badResponse:
        return AppException(
          _messageByStatus(statusCode),
          statusCode: statusCode,
        );
      case DioExceptionType.cancel:
        return const AppException('Запрос был отменён');
      case DioExceptionType.badCertificate:
      case DioExceptionType.unknown:
        return const AppException('Произошла ошибка соединения');
    }
  }

  static String? _extractMessage(Object? data) {
    if (data is Map<String, dynamic>) {
      final message = data['message'];
      if (message is String) {
        return message;
      }
    }
    return null;
  }

  static String _messageByStatus(int? statusCode) {
    switch (statusCode) {
      case 400:
        return 'Проверьте правильность введённых данных';
      case 401:
        return 'Неверный email или пароль, либо email ещё не подтверждён';
      case 409:
        return 'Пользователь с таким email уже существует';
      case 500:
        return 'Ошибка backend-сервера';
      default:
        return 'Не удалось выполнить запрос';
    }
  }

  @override
  String toString() => message;
}
