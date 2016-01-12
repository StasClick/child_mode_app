package com.bzsoft.childmodeapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    ViewPager viewPager;
    CustomSwipeAdapter adapter;


    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            
            bringApplicationToFront();
            
        }
    }

    // disable long power button press
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(!hasFocus) {
            // Close every kind of system dialog
            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            sendBroadcast(closeDialog);
        }
    }

    private void bringApplicationToFront()
    {
        KeyguardManager myKeyManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if( myKeyManager.inKeyguardRestrictedInputMode())
            return;

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        try
        {
            //Log.d("TAG", "BringToFront: send");
            pendingIntent.send();
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
    }

    Timer timer;
    MyTimerTask myTimerTask;

    protected PowerManager.WakeLock mWakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.content_main);


        myTimerTask = new MyTimerTask();
        timer = new Timer();
        timer.schedule(myTimerTask, 500, 500);


        insertDummyContactWrapper();

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Dialog dialog = CreateExitDialog();
        dialog.show();
    }

    public Dialog CreateExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        Random rand = new Random();
        int ln = (rand.nextInt(7) + 1) * 100 + (rand.nextInt(7) + 1) * 10;
        int rn = rand.nextInt(14) + 10;

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_exit, null);
        TextView textView = (TextView)view.findViewById(R.id.label);
        final EditText editText = (EditText)view.findViewById(R.id.answer);
        final int answer = ln + rn;
        textView.setText(ln + " + " + rn + " = ?");
        builder
                .setView(view)
                // Add action buttons
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String answerUserStr = editText.getText().toString();
                        int answerUser = -1;

                        try
                        {
                            answerUser = Integer.parseInt(answerUserStr);
                        }
                        catch(NumberFormatException ex) { }

                        if (answerUser == answer) {
                            timer.cancel();
                            timer = null;
                            dialog.cancel();
                            System.exit(0);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Invalid number!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private void insertDummyContactWrapper() {
        Log.d("TAG", "HelloQ 11");

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP){

            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
                Log.d("TAG", "Load images: Request permission");
            }
            Log.d("TAG", "Load images: Request permission PERMISSION_GRANTED");
        }


        insertDummyContact();
        Log.d("TAG", "HelloQ 44");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    insertDummyContact();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "READ_EXTERNAL_STORAGE Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void insertDummyContact()
    {
        ArrayList<String> d = getAllShownImagesPath(this);
        Log.d("TAG", "Count of photos: " + d.size());
        Log.d("TAG", "photos: " + d.toString());

        DisplayMetrics display2 = this.getResources().getDisplayMetrics();

        int width = display2.widthPixels;
        int height = display2.heightPixels;

        viewPager = (ViewPager)findViewById(R.id.view_paper);

        Log.d("TAG", "viewPager Resolution: " + viewPager.getWidth() + "x" + viewPager.getHeight());
        Log.d("TAG", "viewPager Resolution: " + viewPager.getMeasuredWidth() + "x" + viewPager.getMeasuredHeight());
        Log.d("TAG", "screen Resolution: " + width + "x" + height);

        Random r = new Random();
        int random = r.nextInt(d.size()) + 1;

        adapter = new CustomSwipeAdapter(this, d, width, height);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(random, false);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    int currentItem = viewPager.getCurrentItem();
                    int count = adapter.getCount();

                    if (currentItem == 0)
                        viewPager.setCurrentItem(count - 2, false);
                    if (currentItem == count - 1)
                        viewPager.setCurrentItem(1, false);
                }
            }
        });
    }











    /**
     * Getting All Images Path
     *
     * @param activity
     * @return ArrayList with images Path
     */
    public static ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            if (!absolutePathOfImage.endsWith(".jpg") &&
                !absolutePathOfImage.endsWith(".jpeg"))
                continue;

            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }
}
