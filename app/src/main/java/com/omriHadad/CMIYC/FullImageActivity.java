package com.omriHadad.CMIYC;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;

public class FullImageActivity extends AppCompatActivity
{
    RelativeLayout top_bar;
    RelativeLayout buttom_bar;
    boolean hide;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);
        hide=true;
        Intent i=getIntent();
        top_bar=findViewById(R.id.top_bar);
        buttom_bar=findViewById(R.id.buttom_bar);
        int position = i.getExtras().getInt("id");
        ImageAdapter adapter = new ImageAdapter(this);
        ImageView imageView = findViewById(R.id.image);
        File a =adapter.images.get(position);
        Bitmap myBitmap = BitmapFactory.decodeFile(a.getAbsolutePath());
        //imageView.setImageURI(Uri.fromFile(a));
        imageView.setImageBitmap(myBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void imageOnClick(View v){
        if(hide){
            hide=false;
            top_bar.setVisibility(View.INVISIBLE);
            buttom_bar.setVisibility(View.INVISIBLE);
        }
        else{
            hide=true;
            top_bar.setVisibility(View.VISIBLE);
            buttom_bar.setVisibility(View.VISIBLE);

        }
    }
}
