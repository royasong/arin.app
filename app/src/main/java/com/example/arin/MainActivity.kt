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

import android.graphics.BitmapFactory;
import android.os.Build
import androidx.core.content.FileProvider
import android.graphics.ImageDecoder
import android.provider.MediaStore
import android.content.ContentValues
import java.io.FileOutputStream;

import java.io.FileNotFoundException;

import java.io.IOException
//https://todaycode.tistory.com/118
//https://devgeek.tistory.com/12
//출처: https://jwsoft91.tistory.com/278 [혀가 길지 않은 개발자:티스토리]
class MainActivity : ComponentActivity() {
    var TAG = "ARIN"
    lateinit var ivProfile: Button
    lateinit var bg_image: ImageView
    lateinit var bg_string_: String
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
                var currentImageUri  = intent.data
/*
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                Log.d(TAG, "user select " + imageUri)//
                Log.d(TAG, "path:" + imageUri!!.getPath().toString())

                bg_image = findViewById(R.id.bg)
                bg_image.setImageURI(imageUri)
*/
                try{
                    currentImageUri?.let {
                        if(Build.VERSION.SDK_INT < 28) {
                            Log.e(TAG, "MediaStore.Images.Media.getBitmap")//
                            val bitmap = MediaStore.Images.Media.getBitmap(
                                this.contentResolver,
                                currentImageUri
                            )
                            bg_image?.setImageBitmap(bitmap)
                        } else {

                            Log.e(TAG, "ImageDecoder.createSource")//
                            val source = ImageDecoder.createSource(this.contentResolver, currentImageUri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            bg_image?.setImageBitmap(bitmap)
                        }
                    }
                } catch(e : Exception) {
                    Log.e(TAG, "ERROR " + e)//
                    e.printStackTrace()
                }
            }
        }
        initImageViewProfile()
        add_btn_action()
    }
    fun getContext(): Context {
        return context_
    }
    private fun saveBitmapImage(bitmap: Bitmap) {
        val fileName =  "arin_background.png"

        /*
        * ContentValues() 객체 생성.
        * ContentValues는 ContentResolver가 처리할 수 있는 값을 저장해둘 목적으로 사용된다.
        * */
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/ImageSave") // 경로 설정
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName) // 파일이름을 put해준다.
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.IS_PENDING, 1) // 현재 is_pending 상태임을 만들어준다.
            // 다른 곳에서 이 데이터를 요구하면 무시하라는 의미로, 해당 저장소를 독점할 수 있다.
        }

        // 이미지를 저장할 uri를 미리 설정해놓는다.
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        bg_string_ = uri.toString()
        try {
            if(uri != null) {
                val image = contentResolver.openFileDescriptor(uri, "w", null)
                // write 모드로 file을 open한다.

                if(image != null) {
                    val fos = FileOutputStream(image.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    //비트맵을 FileOutputStream를 통해 compress한다.
                    fos.close()

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0) // 저장소 독점을 해제한다.
                    contentResolver.update(uri, contentValues, null, null)
                }
            }
        } catch(e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
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