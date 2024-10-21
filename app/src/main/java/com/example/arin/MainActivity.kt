package com.example.arin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.widget.Button
import android.view.View
import android.widget.ImageView
import androidx.compose.ui.tooling.preview.Preview
import com.example.arin.ui.theme.ArinTheme
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.content.Intent
import android.app.AlertDialog
import android.util.Log

import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher

//https://devgeek.tistory.com/12
//출처: https://jwsoft91.tistory.com/278 [혀가 길지 않은 개발자:티스토리]
class MainActivity : ComponentActivity() {
    var TAG = "ARIN"

    lateinit var ivProfile: Button
    lateinit var bg_image: ImageView
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar()!!.setTitle("장아린 전용 앱")
        setContentView(R.layout.main_layout)
        bg_image = findViewById(R.id.ImageView_bg)
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                Log.d(TAG, "user select " + imageUri)//
                bg_image = findViewById(R.id.ImageView_bg)
                bg_image.setImageURI(imageUri)
            }
        }

        initImageViewProfile()
    }
    private fun navigateGallery() {
        //val intent = Intent(this, SubActivity::class.java)
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        activityResultLauncher.launch(intent)

    }
    private fun showPermissionContextPopup() {
        Log.d(TAG, "navigateGalleryshowPermissionContextPopup")
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES), 1000)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }
    private fun initImageViewProfile() {
        Log.d(TAG, "initImageViewProfile")
        ivProfile = findViewById(R.id.ivProfile)
        Log.d(TAG, "initImageViewProfile")
        ivProfile.setOnClickListener {
            Log.d(TAG, "ivProfile.setOnClickListener execute")
            when {
                // 갤러리 접근 권한이 있는 경우
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                -> {
                    Log.d(TAG, "ivProfile.setOnClickListener access gallary")
                    navigateGallery()
                }

                // 갤러리 접근 권한이 없는 경우 & 교육용 팝업을 보여줘야 하는 경우
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_MEDIA_IMAGES)
                -> {
                    Log.d(TAG, "ivProfile.setOnClickListener can't access gallary")
                    showPermissionContextPopup()
                }
                // 권한 요청 하기(requestPermissions) -> 갤러리 접근(onRequestPermissionResult)
                else -> {
                    Log.d(TAG, "ivProfile.setOnClickListener requestPermissions")
                    requestPermissions(
                        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                        1000

                    )
                }
            }

        }
    }

    public fun onClick1(v: View?) {
        //setbackground
    }

}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArinTheme {
        Greeting("Android")
    }
}