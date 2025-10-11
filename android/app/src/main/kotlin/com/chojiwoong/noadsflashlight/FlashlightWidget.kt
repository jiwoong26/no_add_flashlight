package com.chojiwoong.noadsflashlight

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast

/**
 * 플래시라이트 홈 화면 위젯
 */
class FlashlightWidget : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_TOGGLE_FLASHLIGHT = "com.chojiwoong.noadsflashlight.TOGGLE_FLASHLIGHT"
        private const val PREFS_NAME = "flashlight_widget_prefs"
        private const val PREF_IS_ON = "is_flashlight_on"
        
        private var cameraManager: CameraManager? = null
        private var cameraId: String? = null
        
        /**
         * 플래시 상태 가져오기
         */
        private fun isFlashlightOn(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_IS_ON, false)
        }
        
        /**
         * 플래시 상태 저장
         */
        private fun setFlashlightState(context: Context, isOn: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(PREF_IS_ON, isOn).apply()
        }
        
        /**
         * 플래시 토글
         */
        private fun toggleFlashlight(context: Context): Boolean {
            try {
                if (cameraManager == null) {
                    cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                }
                
                if (cameraId == null) {
                    val cameraIdList = cameraManager?.cameraIdList
                    if (cameraIdList != null && cameraIdList.isNotEmpty()) {
                        cameraId = cameraIdList[0]
                    }
                }
                
                if (cameraId != null) {
                    val isCurrentlyOn = isFlashlightOn(context)
                    val newState = !isCurrentlyOn
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager?.setTorchMode(cameraId!!, newState)
                        setFlashlightState(context, newState)
                        return true
                    }
                }
            } catch (e: CameraAccessException) {
                Toast.makeText(
                    context,
                    "플래시를 제어할 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "오류가 발생했습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            return false
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 모든 위젯 인스턴스 업데이트
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_TOGGLE_FLASHLIGHT) {
            // 플래시 토글
            val success = toggleFlashlight(context)
            
            if (success) {
                // 모든 위젯 업데이트
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, FlashlightWidget::class.java)
                )
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        
        // 마지막 위젯이 제거될 때 플래시 끄기
        if (isFlashlightOn(context)) {
            toggleFlashlight(context)
        }
    }
}

/**
 * 위젯 업데이트
 */
private fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val isOn = FlashlightWidget.Companion::class.java
        .getDeclaredMethod("isFlashlightOn", Context::class.java)
        .invoke(null, context) as Boolean
    
    val views = RemoteViews(context.packageName, R.layout.flashlight_widget)
    
    // 위젯 아이콘 및 배경 변경
    if (isOn) {
        views.setImageViewResource(R.id.widget_icon, android.R.drawable.ic_lock_power_off)
        views.setInt(R.id.widget_background, "setBackgroundResource", android.R.color.holo_orange_light)
    } else {
        views.setImageViewResource(R.id.widget_icon, android.R.drawable.ic_lock_power_off)
        views.setInt(R.id.widget_background, "setBackgroundResource", android.R.color.darker_gray)
    }
    
    // 클릭 이벤트 설정
    val intent = Intent(context, FlashlightWidget::class.java).apply {
        action = "com.chojiwoong.noadsflashlight.TOGGLE_FLASHLIGHT"
    }
    
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    )
    
    views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)
    
    // 위젯 업데이트
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

