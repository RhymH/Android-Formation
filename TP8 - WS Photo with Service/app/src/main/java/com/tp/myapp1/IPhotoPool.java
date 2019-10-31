package com.tp.myapp1;

import java.util.List;

/**
 * Created by georges on 19/03/16.
 */
public interface IPhotoPool {

    void setPhotosInfos(PhotosInfos photosInfos);

    boolean hasPhotoChanged(PhotosInfos.PhotoInfo photoChecking);

    boolean isPhotoMissing(PhotosInfos photosInfos);

    List<String> getPhotoMissingNameList(PhotosInfos photosInfos);
}
