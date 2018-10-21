package com.omriHadad.CMIYC;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

public class FullImageActivity extends AppCompatActivity
{
    RelativeLayout top_bar;
    RelativeLayout buttom_bar;
    File image;
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
        image =adapter.images.get(position);
        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
        //imageView.setImageURI(Uri.fromFile(a));
        imageView.setImageBitmap(myBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public void back_buttonOnClick(View v){
        startActivity(new Intent(FullImageActivity.this, ImageGallery.class));
    }

    public void delete_buttonOnClick(View v){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Are you sure you want to delete the image ?");
        alert.setMessage("");
        alert.setCancelable(false);
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                startActivity(new Intent(FullImageActivity.this, ImageGallery.class));
            }
        });
        alert.setNegativeButton("No",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        alert.create().show();
        if (image.exists()) {
            if (image.delete()) {
                System.out.println("file Deleted :" + image.getName());
            } else {
                System.out.println("file not Deleted :" + image.getName());
            }
        }
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
