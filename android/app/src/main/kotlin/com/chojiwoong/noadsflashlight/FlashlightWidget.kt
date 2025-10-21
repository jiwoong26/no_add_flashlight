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
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast

/**
 * 플래시라이트 홈 화면 위젯
 */
class FlashlightWidget : AppWidgetProvider() {
    
    companion object {
        const val ACTION_TOGGLE_FLASHLIGHT = "com.chojiwoong.noadsflashlight.TOGGLE_FLASHLIGHT"
        private const val PREFS_NAME = "flashlight_widget_prefs"
        private const val PREF_IS_ON = "is_flashlight_on"
        private const val TAG = "FlashlightWidget"
        
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
                Log.d(TAG, "toggleFlashlight called")
                
                if (cameraManager == null) {
                    cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    Log.d(TAG, "CameraManager initialized")
                }
                
                if (cameraId == null) {
                    val cameraIdList = cameraManager?.cameraIdList
                    if (cameraIdList != null && cameraIdList.isNotEmpty()) {
                        cameraId = cameraIdList[0]
                        Log.d(TAG, "Camera ID: $cameraId")
                    }
                }
                
                if (cameraId != null) {
                    val isCurrentlyOn = isFlashlightOn(context)
                    val newState = !isCurrentlyOn
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        cameraManager?.setTorchMode(cameraId!!, newState)
                        setFlashlightState(context, newState)
                        
                        Toast.makeText(
                            context,
                            if (newState) "ON" else "OFF",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        Log.d(TAG, "Flashlight toggled successfully")
                        return true
                    }
                }
            } catch (e: CameraAccessException) {
                Log.e(TAG, "CameraAccessException", e)
                Toast.makeText(
                    context,
                    "플래시를 제어할 수 없습니다",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            } catch (e: Exception) {
                Log.e(TAG, "Exception", e)
                Toast.makeText(
                    context,
                    "오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            Log.w(TAG, "toggleFlashlight failed - no camera available")
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
    
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        // 위젯 크기가 변경될 때 레이아웃 업데이트
        Log.d(TAG, "Widget options changed for widget $appWidgetId")
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        Log.d(TAG, "onReceive: action=${intent.action}")
        
        if (intent.action == ACTION_TOGGLE_FLASHLIGHT) {
            Log.d(TAG, "Toggling flashlight...")
            
            // 플래시 토글
            val success = toggleFlashlight(context)
            
            Log.d(TAG, "Toggle success: $success")
            
            if (success) {
                // 모든 위젯 업데이트
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, FlashlightWidget::class.java)
                )
                Log.d(TAG, "Updating ${appWidgetIds.size} widgets")
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
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    Log.d("FlashlightWidget", "updateAppWidget called for widget $appWidgetId")
    
    // 현재 플래시 상태 가져오기
    val prefs = context.getSharedPreferences("flashlight_widget_prefs", Context.MODE_PRIVATE)
    val isOn = prefs.getBoolean("is_flashlight_on", false)
    
    Log.d("FlashlightWidget", "Flashlight state: $isOn")
    
    // 위젯 크기에 따라 레이아웃 선택
    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    
    Log.d("FlashlightWidget", "Widget size: ${minWidth}dp x ${minHeight}dp")
    
    // 레이아웃 선택 로직
    val layoutId = when {
        // 1×1: 아이콘만 (글자 없음)
        minWidth < 100 && minHeight < 100 -> {
            Log.d("FlashlightWidget", "1×1 - Using small layout (icon only)")
            R.layout.flashlight_widget_small
        }
        // 2×1, 3×1, 4×1: 가로로 긴 위젯 → 아이콘 + 글자 가로 배치
        minWidth > minHeight * 1.5 -> {
            Log.d("FlashlightWidget", "2×1/3×1/4×1 - Using horizontal layout (icon + text horizontal)")
            R.layout.flashlight_widget_horizontal
        }
        // 2×2, 3×2, 4×2: 기본 → 아이콘 + 글자 세로 배치
        else -> {
            Log.d("FlashlightWidget", "2×2/3×2/4×2 - Using vertical layout (icon + text vertical)")
            R.layout.flashlight_widget
        }
    }
    
    val isSmall = (layoutId == R.layout.flashlight_widget_small)
    val views = RemoteViews(context.packageName, layoutId)
    
    // 위젯 아이콘 및 배경 변경
    if (isOn) {
        // 플래시 켜짐 - 노란색/오렌지 그라데이션 배경, 흰색 아이콘
        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_flashlight_on)
        views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.flashlight_widget_background_on)
        
        // 큰 위젯인 경우에만 텍스트 표시
        if (!isSmall) {
            views.setTextColor(R.id.widget_text, 0xFFFF6F00.toInt()) // 진한 오렌지색
            views.setTextViewText(R.id.widget_text, "ON")
        }
    } else {
        // 플래시 꺼짐 - 회색 배경, 회색 아이콘
        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_flashlight_off)
        views.setInt(R.id.widget_button, "setBackgroundResource", R.drawable.flashlight_widget_background_off)
        
        // 큰 위젯인 경우에만 텍스트 표시
        if (!isSmall) {
            views.setTextColor(R.id.widget_text, 0xFF757575.toInt()) // 회색
            views.setTextViewText(R.id.widget_text, "OFF")
        }
    }
    
    // 클릭 이벤트 설정
    val intent = Intent(context, FlashlightWidget::class.java).apply {
        action = FlashlightWidget.ACTION_TOGGLE_FLASHLIGHT
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

