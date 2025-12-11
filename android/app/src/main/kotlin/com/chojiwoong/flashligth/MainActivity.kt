package com.chojiwoong.flashligth

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.chojiwoong.flashligth/flashlight"
    private val EVENT_CHANNEL = "com.chojiwoong.flashligth/flashlight_event"
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn = false
    private var currentBrightness = 1.0f
    
    // EventChannel Sink (Flutter로 이벤트를 보내는 객체)
    private var eventSink: EventChannel.EventSink? = null

    // 토치 콜백 정의
    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            // 현재 사용 중인 카메라 ID와 일치할 때만 처리
            if (cameraId == this@MainActivity.cameraId) {
                Log.d("MainActivity", "onTorchModeChanged: $enabled")
                isFlashlightOn = enabled
                
                // Flutter로 상태 전달
                runOnUiThread {
                    eventSink?.success(enabled)
                }
                
                // 시스템에 의해 꺼진 경우 서비스도 중지 등 처리
                if (!enabled) {
                     stopFlashlightService()
                } else {
                     // 켜졌는데 서비스가 안돌고 있다면 (외부에서 켠 경우) 서비스 시작 고려
                     // 하지만 외부에서 켠 경우 밝기 값을 알 수 없으므로 기본적으로는 상태 동기화에 집중
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        findCameraWithFlash()
        
        // 콜백 등록
        cameraManager?.registerTorchCallback(torchCallback, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 콜백 해제
        cameraManager?.unregisterTorchCallback(torchCallback)
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // EventChannel 설정
        EventChannel(flutterEngine.dartExecutor.binaryMessenger, EVENT_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                    // 연결되자마자 현재 상태 한번 보냄
                    events?.success(isFlashlightOn)
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "isAvailable" -> {
                    result.success(cameraId != null)
                }
                "turnOn" -> {
                    val brightness = call.argument<Double>("brightness")?.toFloat() ?: 1.0f
                    try {
                        turnOnFlashlight(brightness)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error turning on flashlight", e)
                        result.success(false)
                    }
                }
                "turnOff" -> {
                    try {
                        turnOffFlashlight()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error turning off flashlight", e)
                        result.success(false)
                    }
                }
                "setBrightness" -> {
                    val brightness = call.argument<Double>("brightness")?.toFloat() ?: 1.0f
                    try {
                        setBrightness(brightness)
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error setting brightness", e)
                        result.success(false)
                    }
                }
                "updateWidget" -> {
                    try {
                        updateAllWidgets()
                        result.success(true)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error updating widget", e)
                        result.success(false)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun findCameraWithFlash() {
        try {
            val cameraIdList = cameraManager?.cameraIdList
            cameraIdList?.forEach { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                if (hasFlash == true) {
                    cameraId = id
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error finding camera with flash", e)
        }
    }

    private fun turnOnFlashlight(brightness: Float) {
        cameraId?.let { id ->
            currentBrightness = brightness.coerceIn(0.0f, 1.0f)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ (API 33+): 밝기 조절 지원
                val strengthLevel = (currentBrightness * getMaxTorchStrength()).toInt().coerceAtLeast(1)
                cameraManager?.turnOnTorchWithStrengthLevel(id, strengthLevel)
                Log.d("MainActivity", "Flashlight ON with brightness: $currentBrightness (level: $strengthLevel)")
            } else {
                // Android 12 이하: ON/OFF만 가능
                cameraManager?.setTorchMode(id, true)
                Log.d("MainActivity", "Flashlight ON (brightness control not supported)")
            }
            isFlashlightOn = true
            
            // Foreground Service 시작 (앱이 kill되어도 플래시 유지)
            startFlashlightService(currentBrightness)
        }
    }

    private fun turnOffFlashlight() {
        cameraId?.let { id ->
            cameraManager?.setTorchMode(id, false)
            isFlashlightOn = false
            Log.d("MainActivity", "Flashlight OFF")
            
            // Foreground Service 중지
            stopFlashlightService()
        }
    }

    private fun setBrightness(brightness: Float) {
        if (isFlashlightOn) {
            // 플래시가 켜져 있을 때만 밝기 변경
            // 권한이 필요하므로 SecurityException이 발생할 수 있음
            turnOnFlashlight(brightness)
        } else {
            // 플래시가 꺼져 있으면 값만 저장
            currentBrightness = brightness.coerceIn(0.0f, 1.0f)
            Log.d("MainActivity", "Brightness saved: $currentBrightness (flashlight is off)")
        }
    }

    private fun getMaxTorchStrength(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            cameraId?.let { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
            } ?: 1
        } else {
            1
        }
    }

    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, FlashlightWidget::class.java)
        )
        
        if (appWidgetIds.isNotEmpty()) {
            Log.d("MainActivity", "Updating ${appWidgetIds.size} widgets")
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(this, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // 앱이 백그라운드로 갈 때 위젯 업데이트
        Log.d("MainActivity", "onPause - updating widgets")
        updateAllWidgets()
    }

    override fun onResume() {
        super.onResume()
        // 앱이 포그라운드로 올 때 위젯 업데이트
        Log.d("MainActivity", "onResume - updating widgets")
        updateAllWidgets()
    }
    
    private fun startFlashlightService(brightness: Float) {
        try {
            val intent = Intent(this, FlashlightForegroundService::class.java).apply {
                action = FlashlightForegroundService.ACTION_START
                putExtra(FlashlightForegroundService.EXTRA_BRIGHTNESS, brightness)
            }
            ContextCompat.startForegroundService(this, intent)
            Log.d("MainActivity", "Foreground service start requested")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error starting foreground service", e)
        }
    }
    
    private fun stopFlashlightService() {
        try {
            val intent = Intent(this, FlashlightForegroundService::class.java).apply {
                action = FlashlightForegroundService.ACTION_STOP
            }
            startService(intent)
            Log.d("MainActivity", "Foreground service stop requested")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error stopping foreground service", e)
        }
    }
}
