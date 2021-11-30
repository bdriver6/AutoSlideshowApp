package jp.techacademy.hiromi.kakoo.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.net.Uri
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 100
    private val uriArrayList = arrayListOf<Uri>()
    private var index = 0
    private var mTimer: Timer? = null
    private var mHandler = Handler()
    var kirikae: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonAuto.text = "再生"

        buttonNext.setOnClickListener(this)
        buttonBack.setOnClickListener(this)
        buttonAuto.setOnClickListener(this)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }
        }
    }

    fun getContentsInfo() {
        // 画像の情報を取得する

        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                uriArrayList.add(imageUri)

                Log.d("ANDROID", "URI : " + imageUri.toString())
                imageView.setImageURI(imageUri)

            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    override fun onClick(v: View?) {

        if (v != null) {

            if (v.id == R.id.buttonNext) {


                if (index < uriArrayList.size - 1) {
                    index++
                } else {
                    index = 0
                }
                imageView.setImageURI(uriArrayList.get(index))

            } else if (v.id == R.id.buttonBack) {

                if (mTimer != null) {
                    mTimer!!.cancel()
                    mTimer = null
                }

                if (index > 0) {
                    index--
                } else {
                    index = uriArrayList.size - 1
                }
                imageView.setImageURI(uriArrayList.get(index))

            } else if (v.id == R.id.buttonAuto) {

                if (kirikae) {
                    kirikae = false
                    buttonAuto.text = "停止"
                    buttonBack.isClickable = false
                    buttonNext.isClickable = false

                    // タイマーの作成
                    mTimer = Timer()
                    // タイマーの始動
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            if (index < uriArrayList.size - 1) {
                                index++
                            } else {
                                index = 0
                            }
                            Log.d("ANDROID", "URI : " + index.toString())
                            mHandler.post {
                                imageView.setImageURI(uriArrayList.get(index))
                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
                } else {
                    kirikae = true
                    buttonAuto.text = "再生"
                    buttonBack.isClickable = true
                    buttonNext.isClickable = true
                    mTimer!!.cancel()
                }

            }

        }

    }


}
