import 'package:flutter/material.dart';
import '../services/flashlight_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with WidgetsBindingObserver {
  final FlashlightService _flashlight = FlashlightService();
  bool _isLoading = true;
  bool _isAvailable = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initialize();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    _flashlight.dispose();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.paused ||
        state == AppLifecycleState.detached) {
      // 앱이 백그라운드로 가거나 종료될 때 플래시 끄기
      _flashlight.turnOff();
    }
  }

  Future<void> _initialize() async {
    final available = await _flashlight.isAvailable();
    await _flashlight.loadState();
    
    setState(() {
      _isAvailable = available;
      _isLoading = false;
    });

    if (!available) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('이 기기는 플래시를 지원하지 않습니다.'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _toggleFlashlight() async {
    if (!_isAvailable) return;

    final success = await _flashlight.toggle();
    
    if (!success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            _flashlight.isOn
                ? '플래시를 끌 수 없습니다.'
                : '플래시를 켤 수 없습니다. 카메라 권한을 확인해주세요.',
          ),
          backgroundColor: Colors.red,
        ),
      );
    }
    
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    
    return Scaffold(
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: _flashlight.isOn
                ? [
                    Colors.yellow.shade200,
                    Colors.orange.shade100,
                    Colors.white,
                  ]
                : isDark
                    ? [
                        Colors.grey.shade900,
                        Colors.black,
                      ]
                    : [
                        Colors.grey.shade100,
                        Colors.grey.shade200,
                      ],
          ),
        ),
        child: SafeArea(
          child: _isLoading
              ? const Center(child: CircularProgressIndicator())
              : Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const Spacer(),
                    
                    // 플래시 상태 텍스트
                    Text(
                      _flashlight.isOn ? '플래시 켜짐' : '플래시 꺼짐',
                      style: theme.textTheme.headlineMedium?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: _flashlight.isOn
                            ? Colors.orange.shade900
                            : theme.colorScheme.onSurface,
                      ),
                    ),
                    
                    const SizedBox(height: 60),
                    
                    // 플래시 토글 버튼
                    GestureDetector(
                      onTap: _isAvailable ? _toggleFlashlight : null,
                      child: Container(
                        width: 200,
                        height: 200,
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          gradient: _flashlight.isOn
                              ? RadialGradient(
                                  colors: [
                                    Colors.yellow.shade300,
                                    Colors.orange.shade400,
                                  ],
                                )
                              : RadialGradient(
                                  colors: [
                                    Colors.grey.shade300,
                                    Colors.grey.shade500,
                                  ],
                                ),
                          boxShadow: [
                            BoxShadow(
                              color: _flashlight.isOn
                                  ? Colors.orange.withValues(alpha: 0.5)
                                  : Colors.black26,
                              blurRadius: 30,
                              spreadRadius: 10,
                            ),
                          ],
                        ),
                        child: Icon(
                          _flashlight.isOn
                              ? Icons.flashlight_on
                              : Icons.flashlight_off,
                          size: 80,
                          color: _flashlight.isOn
                              ? Colors.white
                              : Colors.grey.shade700,
                        ),
                      ),
                    ),
                    
                    const SizedBox(height: 60),
                    
                    // 밝기 조절 섹션
                    Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 40),
                      child: Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                '밝기',
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              Text(
                                '${(_flashlight.brightness * 100).round()}%',
                                style: theme.textTheme.titleMedium?.copyWith(
                                  fontWeight: FontWeight.bold,
                                  color: theme.colorScheme.primary,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Icon(
                                Icons.brightness_low,
                                color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                              ),
                              Expanded(
                                child: SliderTheme(
                                  data: SliderThemeData(
                                    trackHeight: 8,
                                    thumbShape: const RoundSliderThumbShape(
                                      enabledThumbRadius: 14,
                                    ),
                                    overlayShape: const RoundSliderOverlayShape(
                                      overlayRadius: 24,
                                    ),
                                  ),
                                  child: Slider(
                                    value: _flashlight.brightness,
                                    min: 0.0,
                                    max: 1.0,
                                    divisions: 10,
                                    activeColor: _flashlight.isOn
                                        ? Colors.orange.shade600
                                        : theme.colorScheme.primary,
                                    inactiveColor: Colors.grey.shade300,
                                    onChanged: _isAvailable
                                        ? (value) {
                                            setState(() {
                                              _flashlight.setBrightness(value);
                                            });
                                          }
                                        : null,
                                  ),
                                ),
                              ),
                              Icon(
                                Icons.brightness_high,
                                color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                              ),
                            ],
                          ),
                          const SizedBox(height: 8),
                          Text(
                            '참고: 대부분의 기기에서 밝기 조절은 하드웨어적으로 지원되지 않습니다.',
                            style: theme.textTheme.bodySmall?.copyWith(
                              color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
                            ),
                            textAlign: TextAlign.center,
                          ),
                        ],
                      ),
                    ),
                    
                    const Spacer(),
                    
                    // 앱 정보
                    Padding(
                      padding: const EdgeInsets.only(bottom: 20),
                      child: Text(
                        'No Ad Flashlight\n광고 없는 플래시라이트',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurface.withValues(alpha: 0.5),
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}

