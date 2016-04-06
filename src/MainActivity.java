package com.vanillastep.example.steps;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

   private static final String TAG = "NetworkSample";
   private ImageView mImageView;
   private TextView mTextLabel;
   Handler handler = new Handler();
   private static final String imageUrl = "http://www.ibiblio.org/wm/paint/auth/munch/munch.scream.jpg";

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      mImageView = (ImageView)findViewById(R.id.imageView);
      mTextLabel = (TextView)findViewById(R.id.textLabel);

      // 단계1 - 기본 쓰레드 사용. 이미지 뷰에 반영
      findViewById(R.id.step1).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mImageView.setImageBitmap(null);
            mTextLabel.setText("기본 단계1");
            new NetworkThread1().start();
         }
      });

      // 단계2 - 기본 쓰레드 사용. 진행 상황 로그로 출력
      findViewById(R.id.step2).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mImageView.setImageBitmap(null);
            mTextLabel.setText("진행 상황 로그로 출력");
            new NetworkThread2().start();
         }
      });

      // 단계3 - 기본 쓰레드 사용. 진행 상황 로그로 출력
      findViewById(R.id.step3).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mTextLabel.setText("진행 상황 화면에 반영");
            mImageView.setImageBitmap(null);
            new NetworkThread3().start();
         }
      });

      // 단계 - AsyncTask
      findViewById(R.id.step4).setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            mTextLabel.setText("Async Task 사용");
            mImageView.setImageBitmap(null);
            // Parameter 타입이 String
            new ImageDownloadTask().execute(imageUrl);
         }
      });
   }

   class NetworkThread1 extends Thread {
      @Override
      public void run() {


         try {
            URL url = new URL(imageUrl);
            URLConnection conn = url.openConnection();

            Log.d(TAG, "Content Type : " + conn.getContentType());
            Log.d(TAG, "Content Length : " + conn.getContentLength());
            Log.d(TAG, "Content Encoding : " + conn.getContentEncoding());

            InputStream is = (InputStream) conn.getContent();
            final Bitmap bitmap = BitmapFactory.decodeStream(is);

            handler.post(new Runnable() {
               @Override
               public void run() {
                  mImageView.setImageBitmap(bitmap);
               }
            });

         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }

   class NetworkThread2 extends Thread {
      @Override
      public void run() {
         String imageUrl = "http://www.ibiblio.org/wm/paint/auth/munch/munch.scream.jpg";

         try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int length = connection.getContentLength();
            Log.d(TAG, "Total Length : " + length);

            InputStream is = (InputStream) connection.getContent();
            byte buffer[] = new byte[1024];
            int byteRead = 0;
            int offset = 0;

            ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
            BufferedOutputStream os = new BufferedOutputStream(bos);

            while ( ( byteRead = is.read(buffer) ) != -1 ) {

               os.write(buffer, 0, byteRead);
               offset += byteRead;

               int progress = (int) ((float) offset / length * 100);
               Log.d(TAG, "progress : " + offset + " ratio : " + progress);
            }
            os.flush();
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, length);

            handler.post(new Runnable() {
               @Override
               public void run() {
                  mImageView.setImageBitmap(bitmap);
               }
            });
         }
         catch ( Exception e ) {
            Log.e(TAG, "Exception : " + e.getLocalizedMessage());
         }
      }
   }

   ProgressDialog dialog;

   class NetworkThread3 extends Thread {
      @Override
      public void run() {
         handler.post(new Runnable() {
            @Override
            public void run() {
               dialog = new ProgressDialog(MainActivity.this);
               dialog.show();
            }
         });

         try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int length = connection.getContentLength();
            Log.d(TAG, "Total Length : " + length);

            InputStream is = (InputStream) connection.getContent();
            byte buffer[] = new byte[1024];
            int byteRead = 0;
            int offset = 0;

            ByteArrayOutputStream bos = new ByteArrayOutputStream(length);
            BufferedOutputStream os = new BufferedOutputStream(bos);

            while ( ( byteRead = is.read(buffer) ) != -1 ) {

               os.write(buffer, 0, byteRead);
               offset += byteRead;

               final int progress = (int) ((float) offset / length * 100);
               Log.d(TAG, "progress : " + offset + " ratio : " + progress);

               // 진행 상황을 화면에 반영
               handler.post(new Runnable() {
                  @Override
                  public void run() {
                     mTextLabel.setText("Progress : " + progress);
                     dialog.setProgress(progress);

                  }
               });

            }
            os.flush();

            dialog.dismiss();

            final Bitmap bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0, length);
            // 최종 이미지를 화면에 반영
            handler.post(new Runnable() {
               @Override
               public void run() {
                  mImageView.setImageBitmap(bitmap);
               }
            });
         }
         catch ( Exception e ) {
            Log.e(TAG, "Exception : " + e.getLocalizedMessage());
         }
      }
   }

   class ImageDownloadTask extends AsyncTask<String, Integer, Bitmap> {

      private Bitmap bitmap;

      @Override
      protected Bitmap doInBackground(String... strings) {
         String imageUrl = strings[0];
         try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int length = connection.getContentLength();
            InputStream is = (InputStream) connection.getContent();
            byte[] buffer = new byte[1024];

            ByteArrayOutputStream os = new ByteArrayOutputStream(length);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int byteRead = 0;
            int progress = 0;

            while ( ( byteRead = is.read(buffer)) != -1 ) {
               bos.write(buffer, 0, byteRead);

               progress += byteRead;

               int ratio = (int)((float)progress / length * 100);
               // onProgressUpdate로 진행상황 업데이트
               publishProgress(ratio);
            }
            bos.flush();
            byte[] imageBytes = os.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, length);


         } catch (MalformedURLException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         }

         return bitmap;
      }

      @Override
      protected void onProgressUpdate(Integer... values) {
         super.onProgressUpdate(values);
         mTextLabel.setText("Progress : " + values[0]);
      }

      @Override
      protected void onPostExecute(Bitmap bitmap) {
         if ( bitmap != null ) {
            // Task 동작 끝
            mImageView.setImageBitmap(bitmap);
         }
      }
   }
}
