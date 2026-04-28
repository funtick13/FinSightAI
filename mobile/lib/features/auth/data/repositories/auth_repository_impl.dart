import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/errors/app_exception.dart';
import '../../../../core/storage/token_storage.dart';
import '../../domain/entities/auth_user.dart';
import '../../domain/repositories/auth_repository.dart';
import '../datasources/auth_remote_datasource.dart';
import '../models/login_request.dart';
import '../models/register_request.dart';
import '../models/register_response.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepositoryImpl(
    remoteDataSource: ref.watch(authRemoteDataSourceProvider),
    tokenStorage: ref.watch(tokenStorageProvider),
  );
});

class AuthRepositoryImpl implements AuthRepository {
  const AuthRepositoryImpl({
    required AuthRemoteDataSource remoteDataSource,
    required TokenStorage tokenStorage,
  })  : _remoteDataSource = remoteDataSource,
        _tokenStorage = tokenStorage;

  final AuthRemoteDataSource _remoteDataSource;
  final TokenStorage _tokenStorage;

  @override
  Future<RegisterResponse> register({
    required String email,
    required String password,
  }) {
    return _remoteDataSource.register(
      RegisterRequest(email: email, password: password),
    );
  }

  @override
  Future<AuthUser> login({
    required String email,
    required String password,
  }) async {
    final response = await _remoteDataSource.login(
      LoginRequest(email: email, password: password),
    );

    if (!response.success || response.accessToken.isEmpty) {
      throw const AppException('Не удалось выполнить вход');
    }

    await _tokenStorage.saveSession(token: response.accessToken, email: email);

    return AuthUser(email: email, accessToken: response.accessToken);
  }

  @override
  Future<AuthUser?> restoreSession() async {
    final token = await _tokenStorage.readToken();

    if (token == null || token.isEmpty) {
      return null;
    }

    final email = await _tokenStorage.readEmail();
    return AuthUser(email: email ?? '', accessToken: token);
  }

  @override
  Future<void> logout() {
    return _tokenStorage.clear();
  }
}
