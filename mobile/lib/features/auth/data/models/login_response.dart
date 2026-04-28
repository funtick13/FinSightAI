class LoginResponse {
  const LoginResponse({
    required this.success,
    required this.message,
    required this.accessToken,
  });

  final bool success;
  final String message;
  final String accessToken;

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      success: json['success'] as bool? ?? false,
      message: json['message'] as String? ?? '',
      accessToken: json['accessToken'] as String? ?? '',
    );
  }
}
