package com.vlog.my.ui.screens.publish

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 视频发布页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPublishScreen(
    onNavigateBack: () -> Unit,
    viewModel: VideoPublishViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // 状态
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val videoUri by viewModel.videoUri.collectAsState()
    val thumbnailBitmap by viewModel.thumbnailBitmap.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()

    // 相机权限状态
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 录音权限状态
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 临时视频 URI
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 录制视频结果
    val recordVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            // 录制成功，添加视频
            tempVideoUri?.let { uri ->
                Log.d("VideoPublishScreen", "视频录制成功: $uri")
                try {
                    // 确保文件存在
                    val file = File(uri.path ?: "")
                    if (file.exists()) {
                        Log.d("VideoPublishScreen", "视频文件存在: ${file.length()} bytes")
                    } else {
                        Log.e("VideoPublishScreen", "视频文件不存在")
                    }

                    if (!viewModel.setVideo(uri)) {
                        // 视频时长超过限制
                        Toast.makeText(
                            context,
                            "视频时长不能超过30秒，请重新录制",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("VideoPublishScreen", "处理视频失败", e)
                    Toast.makeText(
                        context,
                        "处理视频失败: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } ?: run {
                Log.e("VideoPublishScreen", "tempVideoUri 为空")
                Toast.makeText(
                    context,
                    "无法获取视频文件",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.e("VideoPublishScreen", "视频录制失败")
            Toast.makeText(
                context,
                "视频录制失败",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 视频选择结果
    val pickVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (!viewModel.setVideo(it)) {
                // 视频时长超过限制
                Toast.makeText(
                    context,
                    "视频时长不能超过30秒，请选择较短的视频",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 录音权限请求
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            // 相机和录音权限都已获取，启动相机
            val videoUri = createVideoFile(context)
            videoUri?.let { uri ->
                tempVideoUri = uri
                startVideoRecording(context, uri, recordVideoLauncher)
            }
        } else {
            // 权限被拒绝
            Toast.makeText(context, "需要录音权限才能录制视频", Toast.LENGTH_SHORT).show()
        }
    }

    // 相机权限请求
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            // 检查录音权限
            if (hasAudioPermission) {
                val videoUri = createVideoFile(context)
                videoUri?.let { uri ->
                    tempVideoUri = uri
                    startVideoRecording(context, uri, recordVideoLauncher)
                }
            } else {
                // 请求录音权限
                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            // 权限被拒绝
            Toast.makeText(context, "需要相机权限才能录制视频", Toast.LENGTH_SHORT).show()
        }
    }

    // 多权限请求
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        hasCameraPermission = cameraPermissionGranted
        hasAudioPermission = audioPermissionGranted

        if (cameraPermissionGranted && audioPermissionGranted) {
            // 所有权限都已获取，启动相机
            val videoUri = createVideoFile(context)
            videoUri?.let { uri ->
                tempVideoUri = uri
                startVideoRecording(context, uri, recordVideoLauncher)
            }
        } else {
            // 权限被拒绝
            Toast.makeText(context, "需要相机和录音权限才能录制视频", Toast.LENGTH_SHORT).show()
        }
    }

    // 处理错误信息
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // 处理发布成功
    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            snackbarHostState.showSnackbar("发布成功")
            viewModel.resetPublishSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("发布视频") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 视频选择区域
                Text(
                    text = "添加视频",
                    style = MaterialTheme.typography.titleMedium
                )

                // 视频预览区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            if (videoUri == null) {
                                // 显示选择对话框
                                showVideoSourceDialog(
                                    context = context,
                                    onCameraSelected = {
                                        // 检查相机和录音权限
                                        if (hasCameraPermission && hasAudioPermission) {
                                            // 已有所有权限，直接启动相机
                                            val videoUri = createVideoFile(context)
                                            videoUri?.let { uri ->
                                                tempVideoUri = uri
                                                startVideoRecording(context, uri, recordVideoLauncher)
                                            }
                                        } else {
                                            // 请求所有需要的权限
                                            multiplePermissionsLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.CAMERA,
                                                    Manifest.permission.RECORD_AUDIO
                                                )
                                            )
                                        }
                                    },
                                    onGallerySelected = {
                                        // 先提示用户视频时长限制
                                        Toast.makeText(
                                            context,
                                            "请注意：视频最长只能为30秒",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        // 从相册选择视频
                                        pickVideoLauncher.launch("video/*")
                                    }
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    videoUri?.let { uri ->
                        // 使用视频预览播放器显示视频
                        VideoPreviewPlayer(
                            videoUri = uri,
                            autoPlay = true,
                            showControls = true,
                            repeatMode = true
                        )

                        // 删除按钮
                        IconButton(
                            onClick = { viewModel.clearVideo() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "删除",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } ?: run {
                        // 显示添加视频图标
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "添加视频",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击添加视频",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 标题输入框（可选）
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("标题（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1
                )

                // 描述输入框（必填）
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("描述（必填）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // 标签输入框（可选）
                OutlinedTextField(
                    value = tags,
                    onValueChange = { viewModel.updateTags(it) },
                    label = { Text("标签（可选，用空格分隔）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // 发布按钮
                Button(
                    onClick = { viewModel.publishVideo() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && videoUri != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("发布")
                }
            }

            // 加载指示器
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "正在发布...",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 显示视频来源选择对话框
 */
private fun showVideoSourceDialog(
    context: Context,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    val options = arrayOf("拍摄视频", "从相册选择")

    android.app.AlertDialog.Builder(context)
        .setTitle("选择视频来源")
        .setItems(options) { _, which ->
            when (which) {
                0 -> onCameraSelected()
                1 -> onGallerySelected()
            }
        }
        .show()
}

/**
 * 创建视频文件并返回 Uri
 */
private fun createVideoFile(context: Context): Uri? {
    return try {
        // 创建视频文件名
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_${timeStamp}_"
        val storageDir = context.getExternalFilesDir("Videos")
        val videoFile = File.createTempFile(
            videoFileName,
            ".mp4",
            storageDir
        )

        // 返回 FileProvider Uri
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 开始录制视频
 */
private fun startVideoRecording(
    context: Context,
    videoUri: Uri,
    recordVideoLauncher: androidx.activity.result.ActivityResultLauncher<Uri>
) {
    // 先提示用户视频时长限制
    Toast.makeText(
        context,
        "请注意：视频最长只能录制30秒",
        Toast.LENGTH_LONG
    ).show()

    try {
        // 启动相机录制视频
        Log.d("VideoPublishScreen", "开始录制视频: $videoUri")
        recordVideoLauncher.launch(videoUri)
    } catch (e: Exception) {
        Log.e("VideoPublishScreen", "启动视频录制失败", e)
        Toast.makeText(
            context,
            "启动视频录制失败: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}
