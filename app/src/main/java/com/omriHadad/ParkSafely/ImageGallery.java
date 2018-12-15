package com.omriHadad.ParkSafely;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageGallery extends AppCompatActivity
{
    private GridView gridView;
    private ImageView empty;
    private TextView textempty;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        gridView=findViewById(R.id.gallery);
        empty=findViewById(R.id.empty);
        textempty=findViewById(R.id.textempty);

        ImageAdapter adapter = new ImageAdapter(this);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
                i.putExtra("id",position);
                startActivity(i);
            }
        });

        if(adapter.images==null || adapter.getCount()==0)
        {
            empty.setVisibility(View.VISIBLE);
            empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_library));
            textempty.setVisibility(View.VISIBLE);

        }
        else
        {
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(adapter);
        }
    }
}
