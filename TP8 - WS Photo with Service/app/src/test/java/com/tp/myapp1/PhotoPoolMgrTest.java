package com.tp.myapp1;

import android.support.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

/**
 * Created by georges on 06/01/16.
 */
public class PhotoPoolMgrTest {
    private PhotoPoolMgr m_photoPool;

    @Before
    public void setUp() throws Exception
    {
        PhotosInfos initialPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(initialPhotosInfos, "1", "Toto", "123456");
        addPhotoInfoInList(initialPhotosInfos, "2", "Titi", "789456");

        m_photoPool = new PhotoPoolMgr();
        m_photoPool.setPhotosInfos(initialPhotosInfos);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getPhotoFromPool()
    {
        PhotosInfos.PhotoInfo myPhotoInfo = m_photoPool.getPhotoInfoFromPool("Toto");
        assertNotNull("Toto not found in Pool", myPhotoInfo);
        assertEquals("Wrong Photo Id", myPhotoInfo.id, "1");
        assertEquals("Wrong Photo Id", myPhotoInfo.hash, "123456");
    }

    @Test
    public void hasPhotoChangedFromPool_returnsFalse()
    {
        PhotosInfos.PhotoInfo photoToCheck = createPhotoInfo("1", "Toto", "123456");

        assertFalse("Photo has not changed", m_photoPool.hasPhotoChanged(photoToCheck));
    }

    @Test
    public void hasPhotoChangedFromPool_returnsTrue()
    {
        PhotosInfos.PhotoInfo photoToCheck = createPhotoInfo("1", "Toto", "123457");

        assertTrue("Photo has changed", m_photoPool.hasPhotoChanged(photoToCheck));
    }

    @Test
    public void hasPhotoChangedFromPool_NotFoundReturnsTrue()
    {
        PhotosInfos.PhotoInfo photoToCheck = createPhotoInfo("1", "Germain", "123457");

        assertTrue("Photo has changed", m_photoPool.hasPhotoChanged(photoToCheck));
    }

    @Test
    public void isPhotoMissing_ReturnsFalse()
    {
        PhotosInfos newPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(newPhotosInfos, "1", "Toto", "7777");
        addPhotoInfoInList(newPhotosInfos, "2", "Titi", "555");

        assertFalse("Photo is not missing", m_photoPool.isPhotoMissing(newPhotosInfos));
    }

    @Test
    public void isPhotoMissing_ReturnsTrue()
    {
        PhotosInfos newPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(newPhotosInfos, "8", "Totu", "7777");
        addPhotoInfoInList(newPhotosInfos, "2", "Titi", "555");

        assertTrue("Photo is missing", m_photoPool.isPhotoMissing(newPhotosInfos));
    }

    @Test
    public void getPhotoMissingNameList_ReturnsEmpty()
    {
        PhotosInfos newPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(newPhotosInfos, "1", "Toto", "7777");
        addPhotoInfoInList(newPhotosInfos, "3", "Totu", "8888");
        addPhotoInfoInList(newPhotosInfos, "2", "Titi", "555");

        List<String> photoMissingNameList = m_photoPool.getPhotoMissingNameList(newPhotosInfos);
        assertEquals("Photo List should be Empty", 0, photoMissingNameList.size());
    }

    @Test
    public void getPhotoMissingNameList_Returns1Element()
    {
        PhotosInfos newPhotosInfos = createPhotoInfosList();
        addPhotoInfoInList(newPhotosInfos, "1", "Totu", "7777");
        addPhotoInfoInList(newPhotosInfos, "2", "Titi", "555");

        List<String> photoMissingNameList = m_photoPool.getPhotoMissingNameList(newPhotosInfos);
        assertEquals("Photo List should be Empty", 1, photoMissingNameList.size());
        assertTrue("Photo List should contain Toto element", photoMissingNameList.contains("Toto"));
    }


    private void addPhotoInfoInList(PhotosInfos photosInfos,String id,String name,String hash)
    {
        PhotosInfos.PhotoInfo pho1 = createPhotoInfo(id, name, hash);

        photosInfos.photos.add(pho1);
    }

    private PhotosInfos.PhotoInfo createPhotoInfo(String id, String name, String hash) {
        PhotosInfos.PhotoInfo pho1 = new PhotosInfos.PhotoInfo();
        pho1.id = id;
        pho1.name = name;
        pho1.hash = hash;
        return pho1;
    }

    public PhotosInfos createPhotoInfosList()
    {
        PhotosInfos photosInfos = new PhotosInfos();
        photosInfos.photos = new ArrayList<PhotosInfos.PhotoInfo>();

        return photosInfos;
    }

}