package com.streamlite

import android.Manifest
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.streamlite.stream.StreamingService
import com.streamlite.ui.StreamLiteApp
import com.streamlite.ui.StreamViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val viewModel: StreamViewModel by viewModels()
  private var pendingStart: StreamViewModel.StartRequest? = null
  private val projectionRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    val request = pendingStart
    pendingStart = null
    if (result.resultCode == RESULT_OK && result.data != null && request != null) {
      StreamingService.start(this, result.resultCode, result.data!!, request.config)
    } else StreamViewModel.reportProjectionDenied()
  }
  private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
    if (granted[Manifest.permission.RECORD_AUDIO] == true) launchProjection() else {
      pendingStart = null
      StreamViewModel.reportPermissionDenied()
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    lifecycleScope.launch { StreamViewModel.starts.collect(::requestProjection) }
    setContent { StreamLiteApp(viewModel) }
  }

  private fun requestProjection(request: StreamViewModel.StartRequest) {
    pendingStart = request
    val needed = buildList {
      if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.RECORD_AUDIO)
      if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) add(Manifest.permission.POST_NOTIFICATIONS)
    }
    if (needed.isEmpty()) launchProjection() else permissionRequest.launch(needed.toTypedArray())
  }

  private fun launchProjection() {
    projectionRequest.launch((getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent())
  }
}
