package com.example.aug12_2;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NFIQResources {
    private String mNfiqModelPath;
    private String mNfiqYAMLPath;
    private String mAbsolutePath;
    NFIQResources(){

    };

    public void writeFileToPrivateStorage(int fromFile, String toFile, Context context){
        InputStream is = context.getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try
        {
            FileOutputStream fos = context.openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void writeModelToPrivateStorage(int fromFile, String toFile, Context context){
        this.mAbsolutePath = context.getApplicationContext().getFilesDir().toString();
        this.mNfiqModelPath = mAbsolutePath+ "/nist_plain_tir-ink.txt";
        InputStream is = context.getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try
        {
            FileOutputStream fos = context.openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void writeYamlToPrivateStorage(int fromFile, String toFile, Context context){
        this.mAbsolutePath = context.getApplicationContext().getFilesDir().toString();
        this.mNfiqYAMLPath = mAbsolutePath+ "/nist_plain_tir-ink.yaml";
        InputStream is = context.getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try
        {
            FileOutputStream fos = context.openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public String getmAbsoluteDirPath(Context context){
        return context.getApplicationContext().getFilesDir().toString();
    }
    public String getNfiqModelPath(){
        return this.mNfiqModelPath;
    }
    public String getNfiqYAMLPath(){
        return this.mNfiqYAMLPath;
    }
}
