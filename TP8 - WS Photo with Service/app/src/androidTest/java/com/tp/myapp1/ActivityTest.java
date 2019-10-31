package com.tp.myapp1;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

/**
 * Created by georges on 06/01/16.
 */
public class ActivityTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getPhotoFromPool()
    {
        PhotosInfos initialPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(initialPhotosInfos, "1", "Toto", "123456");
        addPhotoInfoInList(initialPhotosInfos, "2", "Titi", "789456");

        PhotoPoolMgr photoPool = new PhotoPoolMgr();
        photoPool.setPhotosInfos(initialPhotosInfos);

        assertNotNull("Toto not found in Pool", photoPool.getPhotoInfoFromPool("Toto"));
    }

    private void addPhotoInfoInList(PhotosInfos photosInfos,String id,String name,String hash)
    {
        PhotosInfos.PhotoInfo pho1 = new PhotosInfos.PhotoInfo();
        pho1.id = id;
        pho1.name = name;
        pho1.hash = hash;

        photosInfos.photos.add(pho1);
    }

    public PhotosInfos createPhotoInfosList()
    {
        PhotosInfos photosInfos = new PhotosInfos();
        photosInfos.photos = new ArrayList<PhotosInfos.PhotoInfo>();

        return photosInfos;
    }

}