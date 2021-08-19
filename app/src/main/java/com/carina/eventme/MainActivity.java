package com.carina.eventme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA = 1;
    private static final int REQUEST_CODE_GALLERY = 0;
    private static Bitmap bitmap;
    private static boolean loading;
    private static String date;
    private static String title;
    private static String time;
    private static boolean sorryNotFound = false;
    private static results fragment;
    EditText titleevent;
    EditText dateevent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.checkPermissionThenInitialize();


    }

    public void checkPermissionThenInitialize() {
        setContentView(R.layout.activity_permission);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            this.initializePlease();
        }else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.INTERNET,
                    Manifest.permission.CAMERA,
            }, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode != 1
    || grantResults.length <= 0
    || grantResults[0]!= PackageManager.PERMISSION_GRANTED
    || grantResults[1]!= PackageManager.PERMISSION_GRANTED
            || grantResults[2]!= PackageManager.PERMISSION_GRANTED
            || grantResults[3]!= PackageManager.PERMISSION_GRANTED
            || grantResults[4]!= PackageManager.PERMISSION_GRANTED
            || grantResults[5]!= PackageManager.PERMISSION_GRANTED){
        Toast.makeText(this,"PERMISSION DENIED!", Toast.LENGTH_SHORT).show();
    } else {
        this.initializePlease();
    }
    }
    public void initializePlease(){
        MainActivity.loading = false;
        //hide the top bar that has the app name
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().hide();
        getSupportActionBar().setTitle("EventMe");
        setContentView(R.layout.activity_main);
    }

    File photoFileForCam = null;
    String currentPhotoPathForCam;

    public void onClickCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("camera", "eins");
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) Log.d("camera", "2");{
            Log.d("camera", "1");
            try {
                String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = File.createTempFile(imageFileName, ".jpg", storageDir);

                //Save a file: path for use with ACTION_VIEW intents
                Log.d("camera", "11");
                currentPhotoPathForCam = image.getAbsolutePath();
                photoFileForCam = image;
            } catch (IOException ex) {
            }
            //Continue only if the File was successfully created
            Log.d("camera", "12");
            if (photoFileForCam != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.carina.eventme.file", photoFileForCam);
                Log.d("camera", "13");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.d("camera", "14");
                startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_CODE_CAMERA){
            //Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(currentPhotoPathForCam, bmOptions);
            //Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 1;
            bmOptions.inPurgeable = true;
            MainActivity.bitmap = BitmapFactory.decodeFile(currentPhotoPathForCam, bmOptions);
            // try to delete the currentPhotoPath
            File toBeDeleted = new File(currentPhotoPathForCam);
            toBeDeleted.delete();
            
        } else if (requestCode == REQUEST_CODE_GALLERY){
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e){
                e.printStackTrace();
            }
            MainActivity.bitmap = BitmapFactory.decodeStream(imageStream);
        }
        // if you forget to do "setContentView(R.layout.activity_confirm)", not only the screen stays on wrong activity, but you also get a nullpointerexception because *findViewById* won't even find "imageview"
        setContentView(R.layout.activity_confirm);
        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onBackPressed() {
        if (MainActivity.loading) setContentView(R.layout.activity_main);
    }

    public void onClickYes(View view) {
        MainActivity.loading = true;
        //go to loading screen:
        setContentView(R.layout.activity_loading);
        //send the file located in "outfile" to the server:
        new DealingWithServerTask().execute();
    }

    public void onClickNo(View view) {setContentView(R.layout.activity_main);
    }

    public void onClickGallery(View view) {
    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
    photoPickerIntent.setType("image/*");
    startActivityForResult(photoPickerIntent, REQUEST_CODE_GALLERY);
    }

   public void addEvent(View view) {
       titleevent = findViewById(R.id.titleevent);
       dateevent = findViewById(R.id.datum);
      Intent calenderintent = new Intent(Intent.ACTION_INSERT);
      calenderintent.setData(CalendarContract.Events.CONTENT_URI);
         calenderintent.putExtra(CalendarContract.Events.TITLE ,  titleevent.getText().toString());
         calenderintent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dateevent.getText().toString());
         if (calenderintent.resolveActivity(getPackageManager())!= null){
               startActivity(calenderintent);
         } else {
              Toast.makeText(MainActivity.this, "There is no app that can support this action",
                    Toast.LENGTH_SHORT).show();
         }
        }




    @SuppressLint("StaticFieldLeak")
    private class DealingWithServerTask extends AsyncTask<Void, Void, Void>{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Void doInBackground(Void... voids){
            try {
                //from bitmap to file
                //saving image into a directory: creating the directory if not existing:
                FileOutputStream fileOutputStream = null;
                File file = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File dir = new File(file.getAbsolutePath() + "/MyPics");

                dir.mkdirs();
                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);
                //compressing the size
                int quality = 60;
                do {
                    fileOutputStream = new FileOutputStream(outFile);
                    MainActivity.bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
                    quality -= 10;
                    try {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }while (outFile.length() > 2000000);
                //from file to bytearray
                //getting the image path and read it in bytes
                Path path = Paths.get(outFile.getAbsolutePath());
                byte[] fileContents = Files.readAllBytes(path);
                Log.d("compress", "1");

                //establishing connection with the API:
                URL url = new URL("https://eventmet.herokuapp.com/imgt");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                Log.d("connection", "1");
                httpURLConnection.setRequestMethod("POST");
                Log.d("connection", "12");
                httpURLConnection.setRequestProperty("Content-Type", "image/jpeg");
                Log.d("connection", "13");
                httpURLConnection.setRequestProperty("Content-Disposition", "form-data;name=image");
                Log.d("connection", "14");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoOutput(true);//#

                //open an outputstream with the connection
                OutputStream out = httpURLConnection.getOutputStream();
                DataOutputStream os = new DataOutputStream(out);
                //writing the image into the data output stream: in other words: send it!
                os.write(fileContents, 0, fileContents.length);
                Log.d("connection", "15");

                //this line should be included!
                if (httpURLConnection.getResponseCode() != 200){
                    System.out.println("SERVER ERROR CONNECTION ! : ------------     " + httpURLConnection.getResponseCode());
                    MainActivity.date = "Unknown";
                    MainActivity.title = "Unknown";
                    MainActivity.time = "Unknown";
                    return null;
                }
                //reading the response and saving into a Stringtext
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String responseLine;
                StringBuilder responseWhole = new StringBuilder();
                while ((responseLine = bufferedReader.readLine())!= null){
                    responseWhole.append(responseLine);
                }
                bufferedReader.close();

                //responseWhole must be parsed with a json parser:
                JSONObject jsonObject = new JSONObject(responseWhole.toString());
                System.out.println("////////////////////////////////////////");
                System.out.println(responseLine);
                System.out.println("////////////////////////////////////////");
                //finally getting the values of date, time, title :
                MainActivity.date = (String) jsonObject.get("text");
                MainActivity.time = (String) jsonObject.get("text");
                MainActivity.title = (String) jsonObject.get("text");


            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            //go back from loading screen, and show the result screen
            if (MainActivity.sorryNotFound){
                MainActivity.sorryNotFound = false;
                setContentView(R.layout.activity_sorry);
                return;
            }
            setContentView(R.layout.activity_result);
            MainActivity.fragment = new results(
                    MainActivity.date, MainActivity.time, MainActivity.title);
            //open fragment:
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .commit();
            MainActivity.loading=false;
        }
    }

}