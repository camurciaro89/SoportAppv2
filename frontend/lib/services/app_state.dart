import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AppState extends ChangeNotifier {
  String? _userType;
  bool _isLoggedIn = false;

  String? get userType => _userType;
  bool get isLoggedIn => _isLoggedIn;

  Future<void> loadSession() async {
    final prefs = await SharedPreferences.getInstance();
    final token = prefs.getString('token');
    _userType = prefs.getString('user_type');
    _isLoggedIn = token != null;
    notifyListeners();
  }

  void login(String userType, String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('token', token);
    await prefs.setString('user_type', userType);
    _userType = userType;
    _isLoggedIn = true;
    notifyListeners();
  }

  void logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.clear();
    _userType = null;
    _isLoggedIn = false;
    notifyListeners();
  }
}
