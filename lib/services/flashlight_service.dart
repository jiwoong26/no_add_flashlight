import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// 플래시라이트 제어 서비스
class FlashlightService {
  static final FlashlightService _instance = FlashlightService._internal();
  factory FlashlightService() => _instance;
  FlashlightService._internal();

  static const platform = MethodChannel('com.chojiwoong.noadsflashlight/flashlight');

  bool _isOn = false;
  double _brightness = 1.0; // 0.0 ~ 1.0
  
  static const String _keyIsOn = 'flashlight_is_on';
  static const String _keyBrightness = 'flashlight_brightness';
  
  bool get isOn => _isOn;
  double get brightness => _brightness;

  /// 플래시 사용 가능 여부 확인
  Future<bool> isAvailable() async {
    try {
      final bool? result = await platform.invokeMethod('isAvailable');
      return result ?? false;
    } catch (e) {
      return false;
    }
  }

  /// 저장된 상태 로드 (위젯과 동기화)
  Future<void> loadState() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      
      // 🔥 중요: 네이티브에서 변경된 값을 읽기 위해 캐시를 무효화하고 디스크에서 다시 로드
      await prefs.reload();
      print('🟢 FlashlightService - SharedPreferences reloaded from disk');
      
      _brightness = prefs.getDouble(_keyBrightness) ?? 1.0;
      final savedIsOn = prefs.getBool(_keyIsOn) ?? false;
      
      print('🟢 FlashlightService - loadState: savedIsOn=$savedIsOn, current _isOn=$_isOn');
      
      // 상태가 다를 때만 플래시 동기화
      if (savedIsOn != _isOn) {
        print('🟢 FlashlightService - State mismatch! Syncing...');
        if (savedIsOn) {
          // 저장된 상태가 ON인데 현재 OFF면 켬
          print('🟢 FlashlightService - Turning ON flashlight (without save)');
          await turnOnWithoutSave();
        } else {
          // 저장된 상태가 OFF인데 현재 ON이면 끔
          print('🟢 FlashlightService - Turning OFF flashlight (without save)');
          await turnOffWithoutSave();
        }
        _isOn = savedIsOn;
        print('🟢 FlashlightService - Sync completed, _isOn=$_isOn');
      } else {
        print('🟢 FlashlightService - State already in sync, no action needed');
      }
    } catch (e) {
      print('❌ FlashlightService - loadState error: $e');
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

  /// 위젯 업데이트
  Future<void> updateWidget() async {
    try {
      await platform.invokeMethod('updateWidget');
    } catch (e) {
      // 위젯 업데이트 실패 시 무시
    }
  }

  /// 플래시 켜기 (내부용 - 저장하지 않음)
  Future<bool> turnOnWithoutSave() async {
    try {
      final bool? result = await platform.invokeMethod('turnOn', {
        'brightness': _brightness,
      });
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// 플래시 끄기 (내부용 - 저장하지 않음)
  Future<bool> turnOffWithoutSave() async {
    try {
      final bool? result = await platform.invokeMethod('turnOff');
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// 플래시 켜기 (권한 요청 없이 기본 밝기로 작동)
  Future<bool> turnOn() async {
    try {
      final bool? result = await platform.invokeMethod('turnOn', {
        'brightness': _brightness,
      });
      
      if (result == true) {
        _isOn = true;
        await _saveState();
        return true;
      }
      return false;
    } catch (e) {
      _isOn = false;
      return false;
    }
  }

  /// 플래시 끄기
  Future<bool> turnOff() async {
    try {
      final bool? result = await platform.invokeMethod('turnOff');
      
      if (result == true) {
        _isOn = false;
        await _saveState();
        return true;
      }
      return false;
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
  /// Android 13+ (API 33+)에서 밝기 조절을 지원합니다.
  /// Android 12 이하에서는 ON/OFF만 가능합니다.
  Future<bool> setBrightness(double value) async {
    _brightness = value.clamp(0.0, 1.0);
    await _saveState();
    
    // 플래시가 켜져 있으면 실시간으로 밝기 변경
    if (_isOn) {
      try {
        await platform.invokeMethod('setBrightness', {
          'brightness': _brightness,
        });
        return true;
      } catch (e) {
        // 밝기 조절 실패 시 무시 (Android 12 이하)
        return false;
      }
    }
    return true;
  }

  /// 앱 종료 시 플래시 끄기
  Future<void> dispose() async {
    if (_isOn) {
      await turnOff();
    }
  }
}

