package com.example.arin

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.arin.ui.theme.ArinTheme
import java.io.File
import java.io.IOException
import android.graphics.BitmapFactory;
import android.os.Build
import androidx.core.content.FileProvider

//https://todaycode.tistory.com/118
//https://devgeek.tistory.com/12
//출처: https://jwsoft91.tistory.com/278 [혀가 길지 않은 개발자:티스토리]
class MainActivity : ComponentActivity() {
    var TAG = "ARIN"
    lateinit var ivProfile: Button
    lateinit var bg_image: ImageView
    lateinit var context_: Context
    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getActionBar()!!.setTitle("장아린 전용 앱") crash
        setContentView(R.layout.main_layout)
        bg_image = findViewById(R.id.bg)
        context_ = getApplicationContext();
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                Log.d(TAG, "user select " + imageUri)//
                Log.d(TAG, "path:" + imageUri!!.getPath().toString())

                bg_image = findViewById(R.id.bg)
                bg_image.setImageURI(imageUri)

            }
        }
        initImageViewProfile()
        add_btn_action()
    }
    fun getContext(): Context {
        return context_
    }
    private fun uriToBitmap(uri: Uri) {
        val directory = File(context_.cacheDir, "images")
        directory.mkdirs() // 임시 파일이 위치할 폴더를 생성한다.

        val file = File.createTempFile(
            "selected_image",
            ".jpg",
            directory,
        ) // 해당 폴더에 임시 파일을 만든다.

        val authority = context_.packageName + ".fileprovider" //

        val outputFileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context_,
                authority,
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }
    fun add_btn_action() {
        //SMS
        var wakeup = findViewById<Button>(R.id.btn_wakeup)
        wakeup!!.setOnClickListener(View.OnClickListener {
            sendSms(wakeup.getText().toString());
        })
        var gohome = findViewById<Button>(R.id.btn_gohome)
        gohome!!.setOnClickListener(View.OnClickListener {
            sendSms(gohome.getText().toString());
        })
        var whereareyou = findViewById<Button>(R.id.btn_whereareyou)
        whereareyou!!.setOnClickListener(View.OnClickListener {
            sendSms(whereareyou.getText().toString());
        })
        var moretime = findViewById<Button>(R.id.btn_moretime)
        moretime!!.setOnClickListener(View.OnClickListener {
            sendSms(moretime.getText().toString());
        })
        //CALL
        var callmom = findViewById<Button>(R.id.btn_call2mom)
        callmom!!.setOnClickListener(View.OnClickListener {
            call("01095444074")
        })
        var calldad = findViewById<Button>(R.id.btn_call2dad)
        calldad!!.setOnClickListener(View.OnClickListener {
            call("01093597899")
        })
    }
    fun sendSms(message: String) {
        val SmsManager = SmsManager.getDefault()
        SmsManager.sendTextMessage("01095444074", null, message + "[by mom app]", null, null)
        Toast.makeText(
            this@MainActivity,
            "엄마에게 " + message + "라고 보라고 보냈어요",
            Toast.LENGTH_SHORT
        ).show()
    }
    fun call(phonenum: String) {
        var intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:" + phonenum)
        startActivity(intent)
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
        ivProfile = findViewById(R.id.change_bgimage)
        Log.d(TAG, "change_bgimage")
        ivProfile.setOnClickListener {
            Log.d(TAG, "change_bgimage.setOnClickListener execute")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_option, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.menu_item_setting -> {
                Toast.makeText(this, "설정 선택 됨", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_item_help -> {
                Toast.makeText(this, "고객센터 선택 됨", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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