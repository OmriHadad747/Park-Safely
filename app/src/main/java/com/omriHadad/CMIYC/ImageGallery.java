package com.omriHadad.CMIYC;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageGallery extends AppCompatActivity
{
    GridView gridView;
    ImageView empty;
    TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        gridView=findViewById(R.id.gallery);
        empty=findViewById(R.id.empty);
        textEmpty=findViewById(R.id.textEmpty);
        ImageAdapter adapter = new ImageAdapter(this);
        if(adapter.images==null || adapter.getCount()==0)
        {
            empty.setVisibility(View.VISIBLE);
            empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_library_black_24dp));
            textEmpty.setVisibility(View.VISIBLE);
        }
        else
        {
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(adapter);
        }
    }
}
