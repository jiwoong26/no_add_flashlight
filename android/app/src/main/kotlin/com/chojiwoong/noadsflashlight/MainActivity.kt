package com.chojiwoong.noadsflashlight

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
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "com.chojiwoong.noadsflashlight/flashlight"
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn = false
    private var currentBrightness = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        findCameraWithFlash()
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
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
