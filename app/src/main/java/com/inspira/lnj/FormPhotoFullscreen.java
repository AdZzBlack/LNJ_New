package com.inspira.lnj;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;

import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.LibInspira.getShared;

@SuppressLint("ValidFragment")
public class FormPhotoFullscreen extends AppCompatActivity {
    public FormPhotoFullscreen() {

    }

    PhotoViewAttacher photoViewAttacher;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FormPhotoFullscreen.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_photo_fullscreen);
        PhotoView photoView = (PhotoView) findViewById(R.id.photoView);

        Picasso.get()
                .load(getShared(global.temppreferences, global.temp.photo_url, ""))
                .into(photoView);//Regular
        photoViewAttacher = new PhotoViewAttacher(photoView);
    }
}
