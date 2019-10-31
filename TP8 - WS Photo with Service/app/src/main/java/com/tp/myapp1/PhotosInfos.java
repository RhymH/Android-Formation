package com.tp.myapp1;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by georges on 28/12/15.
 */
public class PhotosInfos {

    public PhotosInfos()
    {
        photos = new ArrayList<PhotoInfo>();
    }

    public List<PhotoInfo> photos;
    public static class PhotoInfo{
        public String id;
        public String name;
        public String hash;
    }
}
/*
"photos": [
        {
        "id": 1,
        "name": "MishEdit.jpg"
        "hash": "1234567890"
        },
        {
        "id": 2,
        "name": "TDM.jpg"
        "hash": "24356856655485768765"
        }
        ]
*/
