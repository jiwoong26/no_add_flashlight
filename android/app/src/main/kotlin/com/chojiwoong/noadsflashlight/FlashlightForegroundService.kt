package com.chojiwoong.noadsflashlight

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * 플래시라이트를 백그라운드에서 유지하기 위한 Foreground Service
 */
class FlashlightForegroundService : Service() {
    
    companion object {
        private const val TAG = "FlashlightService"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "flashlight_service_channel"
        const val ACTION_START = "com.chojiwoong.noadsflashlight.ACTION_START"
        const val ACTION_STOP = "com.chojiwoong.noadsflashlight.ACTION_STOP"
        const val EXTRA_BRIGHTNESS = "brightness"
        
        private var isServiceRunning = false
        
        fun isRunning(): Boolean = isServiceRunning
    }
    
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        findCameraWithFlash()
        
        // Notification channel 생성 (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "플래시라이트 서비스",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "플래시라이트가 켜져 있습니다"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        
        when (intent?.action) {
            ACTION_START -> {
                val brightness = intent.getFloatExtra(EXTRA_BRIGHTNESS, 1.0f)
                startForegroundService(brightness)
                turnOnFlashlight(brightness)
            }
            ACTION_STOP -> {
                turnOffFlashlight()
                stopForegroundService()
            }
        }
        
        return START_NOT_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        turnOffFlashlight()
        isServiceRunning = false
    }
    
    private fun findCameraWithFlash() {
        try {
            val cameraIdList = cameraManager?.cameraIdList
            cameraIdList?.forEach { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                if (hasFlash == true) {
                    cameraId = id
                    Log.d(TAG, "Found camera with flash: $id")
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding camera with flash", e)
        }
    }
    
    private fun startForegroundService(brightness: Float) {
        Log.d(TAG, "Starting foreground service with brightness: $brightness")
        
        // 알림을 탭하면 앱을 여는 PendingIntent
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        
        // 알림 생성
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("플래시라이트 켜짐")
            .setContentText("플래시가 백그라운드에서 작동 중입니다")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        isServiceRunning = true
        
        Log.d(TAG, "Foreground service started")
    }
    
    private fun stopForegroundService() {
        Log.d(TAG, "Stopping foreground service")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        
        stopSelf()
        isServiceRunning = false
        
        Log.d(TAG, "Foreground service stopped")
    }
    
    private fun turnOnFlashlight(brightness: Float) {
        cameraId?.let { id ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ (API 33+): 밝기 조절 지원
                    val characteristics = cameraManager?.getCameraCharacteristics(id)
                    val maxStrength = characteristics?.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                    val strengthLevel = (brightness * maxStrength).toInt().coerceAtLeast(1)
                    cameraManager?.turnOnTorchWithStrengthLevel(id, strengthLevel)
                    Log.d(TAG, "Flashlight ON in service with brightness: $brightness (level: $strengthLevel)")
                } else {
                    // Android 12 이하: ON/OFF만 가능
                    cameraManager?.setTorchMode(id, true)
                    Log.d(TAG, "Flashlight ON in service (brightness control not supported)")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error turning on flashlight in service", e)
            }
        }
    }
    
    private fun turnOffFlashlight() {
        cameraId?.let { id ->
            try {
                cameraManager?.setTorchMode(id, false)
                Log.d(TAG, "Flashlight OFF in service")
            } catch (e: Exception) {
                Log.e(TAG, "Error turning off flashlight in service", e)
            }
        }
    }
}

