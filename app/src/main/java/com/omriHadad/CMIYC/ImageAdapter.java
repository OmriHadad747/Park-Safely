package com.omriHadad.CMIYC;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter
{
    private Context context;
    ArrayList<File> images;

    public ImageAdapter(Context c)
    {
        context = c;

        images=new ArrayList<>();
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File (root + "/asaveImage");
        if(myDir.exists())
        {
            File files[]=myDir.listFiles();
            for(int i=0; i<files.length; i++)
            {
                File file = files[i];
                /*It's assumed that all file in the path are in supported type*/
                String filePath = file.getPath();
                if (filePath.endsWith(".jpg")) // Condition to check .jpg file extension
                    images.add(new File(filePath));
            }
        }
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView = new ImageView(context);
        File a =images.get(position);
        imageView.setImageURI(Uri.fromFile(a));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(240,240));

        return imageView;
    }
}
