import '../../data/models/register_response.dart';
import '../entities/auth_user.dart';

abstract class AuthRepository {
  Future<RegisterResponse> register({
    required String email,
    required String password,
  });

  Future<AuthUser> login({required String email, required String password});

  Future<AuthUser?> restoreSession();

  Future<void> logout();
}
