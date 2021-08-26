package com.example.aug12_2;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.aug12_2.databinding.ActivityMainBinding;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'aug12_2' library on application startup.
    static {
        System.loadLibrary("aug12_2");
    }

    private ActivityMainBinding binding;
    TextView mTextView;
    public short[][] image;
    public int mHeight;
    public int mWidth;
    public int mSize;
    public String magicNumber;
    public int[][] mData;
    public String mPGM;
    public String mPath;
    public int cols,rows,maxVal;

    public static String getLineNumber() {
        return String.valueOf(Thread.currentThread().getStackTrace()[2].getLineNumber());
    }
    private static final int REQUEST_ALL_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = new String[]{
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE
    };
    public int getHeight(){
        this.mHeight = image.length;
        return image.length;
    }
    public int getWidth(){
        this.mWidth = image[0].length;
        return image[0].length;
    }

    public static final int MAXVAL = 255;

    private static final String TAG = "DemoInitialApp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mTextView=(TextView) findViewById(R.id.tVdoMagic);                              // Text field
        Button btn = findViewById(R.id.BtnDoMagic);                                     // Button
        btn.setOnClickListener(new View.OnClickListener() {                             // Takes action on Click
            @Override
            public void onClick(View view) {
                doOnClick();                                                            // Invokes the function on Button Click
            }
        });

        TextView tv = binding.sampleText;
        tv.setText("NFIQ2: Fingerprint Quality Score");
    }
    private void doOnClick()
    {
        Log.i(TAG,"enter:doOnClick(); Line #"+this.getLineNumber());                                            // Adding the function call on log
        try {
            AssetManager mgr = this.getApplicationContext().getResources().getAssets();
            /*
            Make sure that the image size for the fingerprint bmp is more than 294x160
            */
            InputStream ims = getAssets().open("test6.bmp");                                             // Input Stream:   INPUT BMP FILE HERE!!!
            BufferedInputStream bufferedInputStream = new BufferedInputStream(ims);                             // Buffered Input Stream
            Bitmap testBitmap= BitmapFactory.decodeStream(bufferedInputStream);                                 // Creating a Bitmap Object
            byte[] testBitmapRawDataBuffer= BitmapUtils.toGrayScaleByteArray(testBitmap);                       // Filling the byte[] from the object
            String res = ("Image width="+testBitmap.getWidth()+". \nImage height="+testBitmap.getHeight() + ". \nRaw Data Length="+testBitmapRawDataBuffer.length);

            // ------------------ 1). Set all the Image properties in the JNI cpp ------------------

            mHeight = testBitmap.getHeight();
            mWidth = testBitmap.getWidth();
            mSize = testBitmapRawDataBuffer.length;
            int result = (setImageConstraints(testBitmapRawDataBuffer,mHeight, mWidth));

            // ------------------- 1.1). Asking for permissions -------------------------------------------

            this.requestPermissions();

            // --------------2). Load Resources(Model, Yaml file) and set their Absolute Paths--------------


            Context context = getBaseContext();
            NFIQResources resources = new NFIQResources();
            resources.writeFileToPrivateStorage(R.raw.example1,"my_output.file.txt",context);                 // Test text file
            resources.writeModelToPrivateStorage(R.raw.nfiq_model,"nist_plain_tir-ink.txt",context);          // NFIQ2 Model File
            resources.writeYamlToPrivateStorage(R.raw.nfiq_yaml_file,"nist_plain_tir-ink.yaml",context);      // NFIQ2 YAML File

            // 4). Get Paths for NFIQ2 resources
            String resourcesPath = resources.getmAbsoluteDirPath(context);
            String NfiqModelPath = resources.getNfiqModelPath();
            String NfiqYAMLPath = resources.getNfiqYAMLPath();
            resourcesPath += "/my_output.file.txt";

            String value = passResourcesToJNI(NfiqModelPath);

            Log.i("MainActivity; Line #"+this.getLineNumber()+": Path value=",NfiqModelPath);


            String score = getNfiq2Score();

            mTextView.setText(res+"\n"+"Size of ByteArray: "+String.valueOf(result)+"\nNFIQ2 Score: "+score+"\nContents of the file:"+value);

        }
        catch (Exception ex)
        {
            Log.i( "Couldn't go inside; Line #"+this.getLineNumber(),ex.getMessage());
            mTextView.setText(ex.getMessage());
        }
    }



    private void
    requestPermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ){
                requestPermissions(PERMISSIONS,REQUEST_ALL_PERMISSIONS);
            }
        }
    }


    public native int setImageConstraints(byte[] b, int h, int w);
    public native String getNfiq2Score();
    public native String readAssets(AssetManager assetManager);

    public static native String passResourcesToJNI(String yourpath);

}