import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/errors/app_exception.dart';
import '../../data/repositories/auth_repository_impl.dart';
import '../../domain/entities/auth_user.dart';
import '../../domain/repositories/auth_repository.dart';

final authControllerProvider = StateNotifierProvider<AuthController, AuthState>(
  (ref) {
    return AuthController(ref.watch(authRepositoryProvider));
  },
);

enum AuthStatus { unknown, authenticated, unauthenticated }

class AuthState {
  const AuthState({
    required this.status,
    this.user,
    this.isLoading = false,
    this.errorMessage,
    this.successMessage,
  });

  const AuthState.unknown()
      : status = AuthStatus.unknown,
        user = null,
        isLoading = false,
        errorMessage = null,
        successMessage = null;

  const AuthState.authenticated(AuthUser this.user)
      : status = AuthStatus.authenticated,
        isLoading = false,
        errorMessage = null,
        successMessage = null;

  const AuthState.unauthenticated({
    this.errorMessage,
    this.successMessage,
    this.isLoading = false,
  })  : status = AuthStatus.unauthenticated,
        user = null;

  final AuthStatus status;
  final AuthUser? user;
  final bool isLoading;
  final String? errorMessage;
  final String? successMessage;

  AuthState copyWith({
    AuthStatus? status,
    AuthUser? user,
    bool clearUser = false,
    bool? isLoading,
    String? errorMessage,
    bool clearError = false,
    String? successMessage,
    bool clearSuccess = false,
  }) {
    return AuthState(
      status: status ?? this.status,
      user: clearUser ? null : user ?? this.user,
      isLoading: isLoading ?? this.isLoading,
      errorMessage: clearError ? null : errorMessage ?? this.errorMessage,
      successMessage:
          clearSuccess ? null : successMessage ?? this.successMessage,
    );
  }
}

class AuthController extends StateNotifier<AuthState> {
  AuthController(this._repository) : super(const AuthState.unknown()) {
    _restoreSession();
  }

  static const _operationTimeout = Duration(seconds: 15);

  final AuthRepository _repository;

  Future<void> _restoreSession() async {
    final user = await _repository.restoreSession().timeout(
          _operationTimeout,
          onTimeout: () => null,
        );

    if (user == null) {
      state = const AuthState.unauthenticated();
      return;
    }

    state = AuthState.authenticated(user);
  }

  Future<bool> register({
    required String email,
    required String password,
  }) async {
    final validationError = _validateCredentials(email, password);

    if (validationError != null) {
      state = AuthState.unauthenticated(errorMessage: validationError);
      return false;
    }

    state = const AuthState.unauthenticated(isLoading: true);

    try {
      final response = await _repository
          .register(email: email.trim(), password: password)
          .timeout(_operationTimeout);

      state = AuthState.unauthenticated(successMessage: response.message);
      return true;
    } catch (error) {
      state = AuthState.unauthenticated(errorMessage: _messageFrom(error));
      return false;
    }
  }

  Future<bool> login({required String email, required String password}) async {
    final validationError = _validateCredentials(email, password);

    if (validationError != null) {
      state = AuthState.unauthenticated(errorMessage: validationError);
      return false;
    }

    state = const AuthState.unauthenticated(isLoading: true);

    try {
      final user = await _repository
          .login(email: email.trim(), password: password)
          .timeout(_operationTimeout);

      state = AuthState.authenticated(user);
      return true;
    } catch (error) {
      state = AuthState.unauthenticated(errorMessage: _messageFrom(error));
      return false;
    }
  }

  Future<void> logout() async {
    await _repository.logout();
    state = const AuthState.unauthenticated();
  }

  void clearMessages() {
    state = state.copyWith(clearError: true, clearSuccess: true);
  }

  String? _validateCredentials(String email, String password) {
    final trimmedEmail = email.trim();
    final emailRegExp = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');

    if (trimmedEmail.isEmpty || !emailRegExp.hasMatch(trimmedEmail)) {
      return 'Введите корректный email';
    }

    if (password.isEmpty) {
      return 'Введите пароль';
    }

    return null;
  }

  String _messageFrom(Object error) {
    if (error is AppException) {
      return error.message;
    }
    if (error is TimeoutException) {
      return 'Сервер не ответил. Проверьте, что backend запущен.';
    }
    return 'Произошла непредвиденная ошибка';
  }
}
