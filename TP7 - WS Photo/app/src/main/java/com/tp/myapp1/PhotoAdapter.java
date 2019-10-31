package com.tp.myapp1;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.media.Image;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by georges on 28/12/15.
 */
class PhotoAdapter extends BaseAdapter
{
    private Context mContext;
    private LinkedHashMap<String,Bitmap> m_listInfosImages;
    private List<Bitmap> m_listImages;

    public PhotoAdapter(Context c) {
        mContext = c;
        m_listInfosImages = new LinkedHashMap<>();
        m_listImages = new ArrayList<>();
    }

    public void setListImages(LinkedHashMap<String,Bitmap> listImg)
    {
        m_listInfosImages = listImg;
        m_listImages = new ArrayList<Bitmap>(m_listInfosImages.values());
    }

    public Bitmap getImage(int position)
    {
        return m_listImages.get(position);
    }

    public String getImageName(int position)
    {
        List<String> imagesNames = new ArrayList<String>(m_listInfosImages.keySet());
        return imagesNames.get(position);
    }

    public int getCount() {
        return m_listImages.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageBitmap(m_listImages.get(position));
        return imageView;
    }

}