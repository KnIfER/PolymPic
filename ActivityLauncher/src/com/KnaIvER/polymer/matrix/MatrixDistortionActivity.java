package com.knaiver.polymer.matrix;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.knaiver.polymer.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class MatrixDistortionActivity extends AppCompatActivity {

    ImageView imageView, imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix2);
        imageView = (ImageView) findViewById(R.id.img);
        imageView.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
        imageView2 = (ImageView) findViewById(R.id.img2);
        Glide.with(this).load(R.drawable.sample).into(imageView);
        Glide.with(this).asBitmap().load(R.drawable.sample).into(new SimpleTarget<Bitmap>() {
			@Override
			public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
				Bitmap bitmap = BitmapUtils.rotate(resource, 190, false);
				imageView2.setImageBitmap(bitmap);
			}
        });
    }
}
