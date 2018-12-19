package com.omriHadad.ParkSafely.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.omriHadad.ParkSafely.R;
import com.omriHadad.ParkSafely.Utilities.ImageAdapter;

public class ImageGalleryActivity extends AppCompatActivity
{
    private GridView gallery;
    private ImageView empty;
    private TextView txtEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        getViewValues();

        ImageAdapter adapter = new ImageAdapter(this);

        this.gallery.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent i = new Intent(getApplicationContext(), FullImageActivity.class);
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        if(adapter.images == null || adapter.getCount() == 0)
        {
            this.empty.setVisibility(View.VISIBLE);
            this.empty.setImageDrawable(getResources().getDrawable(R.drawable.ic_photo_library));
            this.txtEmpty.setVisibility(View.VISIBLE);
        }
        else
        {
            this.gallery.setVisibility(View.VISIBLE);
            this.gallery.setAdapter(adapter);
        }
    }

    private void getViewValues()
    {
        this.gallery = findViewById(R.id.gallery);
        this.empty = findViewById(R.id.empty);
        this.txtEmpty = findViewById(R.id.txtEmpty);
    }
}
