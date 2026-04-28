import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

final secureStorageProvider = Provider<FlutterSecureStorage>((ref) {
  return const FlutterSecureStorage();
});

final tokenStorageProvider = Provider<TokenStorage>((ref) {
  return TokenStorage(ref.watch(secureStorageProvider));
});

class TokenStorage {
  TokenStorage(this._storage);

  static const _tokenKey = 'jwt_token';
  static const _emailKey = 'auth_email';

  final FlutterSecureStorage _storage;

  Future<void> saveSession({
    required String token,
    required String email,
  }) async {
    await _storage.write(key: _tokenKey, value: token);
    await _storage.write(key: _emailKey, value: email);
  }

  Future<String?> readToken() {
    return _storage.read(key: _tokenKey);
  }

  Future<String?> readEmail() {
    return _storage.read(key: _emailKey);
  }

  Future<void> clear() async {
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _emailKey);
  }
}
