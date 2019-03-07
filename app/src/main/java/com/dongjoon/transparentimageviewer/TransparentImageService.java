package com.dongjoon.transparentimageviewer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;


public class TransparentImageService extends Service implements View.OnTouchListener  {

    private Bitmap selectedImageBitmap = null;
    private String selectedImageBitmapName = "selectedImageByteArray";
    private WindowManager windowManager;
    private ConstraintLayout layout;
    private boolean isMinimized = false;
    private final int MINIMIZED_WIDTH = 300;
    private final int MINIMIZED_HEIGHT = 300;
    private ImageView transparentImageView;
    private VelocityTracker mVelocityTracker = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(hasImageBitmap(intent)) {
            setImageBitmap(intent);
            setLayout();
            setWindowManager();
        }
        return START_NOT_STICKY;
    }

    private boolean hasImageBitmap(Intent intent) {
        return intent.hasExtra(selectedImageBitmapName);
    }

    private void setImageBitmap(Intent intent) {
        this.selectedImageBitmap = BitmapFactory.decodeByteArray(
                intent.getByteArrayExtra(selectedImageBitmapName), 0, intent.getByteArrayExtra(selectedImageBitmapName).length);
    }

    private void setLayout() {
        setConstraintLayout();
        setImageView(selectedImageBitmap);
        setCloseButton();
    }

    private void setConstraintLayout() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = (ConstraintLayout) inflater.inflate(R.layout.transparent_service , null);
        ConstraintLayout.LayoutParams constraintLayoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layout.setLayoutParams(constraintLayoutParams);
        layout.setBackgroundColor(Color.argb(0, 0, 0, 0 ));
        layout.setOnTouchListener(this);
    }

    private void setImageView(Bitmap bitmapImage) {
        transparentImageView = layout.findViewById(R.id.transparent_image_view);
        transparentImageView.setImageBitmap(bitmapImage);
        transparentImageView.setAlpha(0.5f);
        transparentImageView.setBackgroundColor(Color.TRANSPARENT);
        transparentImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(layout);
                if(isMinimized) {
                    inflateLayout();
                } else {
                    minimizeLayout();
                }
                isMinimized = !isMinimized;
            }
        });
    }

    private void minimizeLayout() {
        windowManager.addView(layout, getWindowManagerLayoutParams(MINIMIZED_WIDTH, MINIMIZED_HEIGHT));
    }

    private void inflateLayout() {
        Point size = getScreenSize();
        windowManager.addView(layout, getWindowManagerLayoutParams(size.x, size.y));
    }

    private Point getScreenSize() {
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void setCloseButton() {
        ImageButton closeButton = layout.findViewById(R.id.transparent_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(layout);
                stopSelf();
            }
        });
    }

    private void setWindowManager() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        inflateLayout();
    }

    private WindowManager.LayoutParams getWindowManagerLayoutParams(int width, int height) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(width, height,0, 0, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.CENTER | Gravity.CENTER_VERTICAL;
        return layoutParams;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int index = event.getActionIndex();
        int action = event.getActionMasked();
        int pointerId = event.getPointerId(index);

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(mVelocityTracker == null) {
                    // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                }
                else {
                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }
                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(event);
                Log.d("action_down event", event.getRawX()+", "+event.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.
                mVelocityTracker.computeCurrentVelocity(1000);
                // Log velocity of pixels per second
                // Best practice to use VelocityTrackerCompat where possible.
                Log.d("", "X velocity: " +
                        VelocityTrackerCompat.getXVelocity(mVelocityTracker,
                                pointerId));
                Log.d("", "Y velocity: " +
                        VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                                pointerId));
                Log.d("action_move event", event.getRawX()+", "+event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.d("action_up_cancel event", event.getRawX()+", "+event.getRawY());
                // Return a VelocityTracker object back to be re-used by others.
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }
}
