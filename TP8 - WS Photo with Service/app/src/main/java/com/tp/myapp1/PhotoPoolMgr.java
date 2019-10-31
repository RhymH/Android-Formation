package com.tp.myapp1;

import android.content.Context;
import android.os.Handler;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by georges on 31/12/15.
 */
public class PhotoPoolMgr implements IPhotoPool {

    private PhotosInfos m_savePhotosInfos = null;

    public PhotoPoolMgr()
    {
        m_savePhotosInfos = new PhotosInfos();
    }

    @Override
    public void setPhotosInfos(PhotosInfos photosInfos) {
        m_savePhotosInfos = photosInfos;
    }

    @Override
    public boolean hasPhotoChanged(PhotosInfos.PhotoInfo photoChecking) {
        PhotosInfos.PhotoInfo photoInfoSaved = getPhotoInfoFromPool(photoChecking.name);
        if( photoInfoSaved == null)
            return true;

        if( photoInfoSaved.hash.equals(photoChecking.hash) )
            return false;

        return true;
    }

    @Override
    public boolean isPhotoMissing(PhotosInfos photosInfos)
    {
        for(PhotosInfos.PhotoInfo curPhoto : m_savePhotosInfos.photos)
        {
            if( null == getPhotoInfoFromList(photosInfos, curPhoto.name))
                return true;
        }

        return false;
    }

    @Override
    public List<String> getPhotoMissingNameList(PhotosInfos photosInfos)
    {
        ArrayList<String> photosName = new ArrayList<String>();
        for(PhotosInfos.PhotoInfo curPhoto : m_savePhotosInfos.photos)
        {
            if( null == getPhotoInfoFromList(photosInfos, curPhoto.name))
                photosName.add(curPhoto.name);
        }

        return photosName;
    }

    public PhotosInfos.PhotoInfo getPhotoInfoFromPool(String name)
    {
        return getPhotoInfoFromList(m_savePhotosInfos, name);
    }

    private PhotosInfos.PhotoInfo getPhotoInfoFromList(PhotosInfos photosInfosList, String name) {
        for(PhotosInfos.PhotoInfo curPhoto : photosInfosList.photos)
        {
            if( curPhoto.name.equals(name))
                return curPhoto;
        }
        return null;
    }
}
