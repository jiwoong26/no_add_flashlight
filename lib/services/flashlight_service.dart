import 'package:torch_light/torch_light.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 플래시라이트 제어 서비스
class FlashlightService {
  static final FlashlightService _instance = FlashlightService._internal();
  factory FlashlightService() => _instance;
  FlashlightService._internal();

  bool _isOn = false;
  double _brightness = 1.0; // 0.0 ~ 1.0
  
  static const String _keyIsOn = 'flashlight_is_on';
  static const String _keyBrightness = 'flashlight_brightness';

  bool get isOn => _isOn;
  double get brightness => _brightness;

  /// 플래시 사용 가능 여부 확인
  Future<bool> isAvailable() async {
    try {
      return await TorchLight.isTorchAvailable();
    } catch (e) {
      return false;
    }
  }

  /// 카메라 권한 요청
  Future<bool> requestPermission() async {
    var status = await Permission.camera.status;
    
    if (status.isGranted) {
      return true;
    }
    
    if (status.isDenied) {
      var result = await Permission.camera.request();
      return result.isGranted;
    }
    
    return false;
  }

  /// 저장된 상태 로드
  Future<void> loadState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      _brightness = prefs.getDouble(_keyBrightness) ?? 1.0;
      // 앱 시작 시 플래시는 항상 꺼진 상태로 시작
      _isOn = false;
    } catch (e) {
      _brightness = 1.0;
      _isOn = false;
    }
  }

  /// 상태 저장
  Future<void> _saveState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setBool(_keyIsOn, _isOn);
      await prefs.setDouble(_keyBrightness, _brightness);
    } catch (e) {
      // 저장 실패 시 무시
    }
  }

  /// 플래시 켜기
  Future<bool> turnOn() async {
    try {
      final hasPermission = await requestPermission();
      if (!hasPermission) {
        return false;
      }

      await TorchLight.enableTorch();
      _isOn = true;
      await _saveState();
      return true;
    } catch (e) {
      _isOn = false;
      return false;
    }
  }

  /// 플래시 끄기
  Future<bool> turnOff() async {
    try {
      await TorchLight.disableTorch();
      _isOn = false;
      await _saveState();
      return true;
    } catch (e) {
      return false;
    }
  }

  /// 플래시 토글 (켜기/끄기)
  Future<bool> toggle() async {
    if (_isOn) {
      return await turnOff();
    } else {
      return await turnOn();
    }
  }

  /// 밝기 설정 (0.0 ~ 1.0)
  /// 참고: torch_light 패키지는 밝기 조절을 직접 지원하지 않습니다.
  /// 일부 기기에서는 하드웨어적으로 밝기 조절이 불가능합니다.
  Future<void> setBrightness(double value) async {
    _brightness = value.clamp(0.0, 1.0);
    await _saveState();
    
    // torch_light는 밝기 조절을 지원하지 않으므로
    // 여기서는 밝기 값만 저장하고 UI에 표시합니다.
    // 실제 밝기 조절은 하드웨어 제약으로 대부분의 기기에서 불가능합니다.
  }

  /// 앱 종료 시 플래시 끄기
  Future<void> dispose() async {
    if (_isOn) {
      await turnOff();
    }
  }
}

