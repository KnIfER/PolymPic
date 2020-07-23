package com.knaiver.polymer.matrix;

import android.graphics.Color;
import com.knaiver.polymer.R;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ColorMatrixActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    ImageView imageView;
    SeekBar seekBarR, seekBarG, seekBarB, seekBarA;
    ColorMatrix colorMatrix;
    View colorView;
    TextView colorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_matrix);
        colorMatrix = new ColorMatrix();
        colorMatrix.setScale(caculate(128), caculate(128), caculate(128), caculate(128));
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        seekBarR = (SeekBar) findViewById(R.id.bar_R);
        seekBarG = (SeekBar) findViewById(R.id.bar_G);
        seekBarB = (SeekBar) findViewById(R.id.bar_B);
        seekBarA = (SeekBar) findViewById(R.id.bar_A);

        colorView = findViewById(R.id.color_view);
        colorText = (TextView) findViewById(R.id.color_text);

        seekBarR.setOnSeekBarChangeListener(this);
        seekBarG.setOnSeekBarChangeListener(this);
        seekBarB.setOnSeekBarChangeListener(this);
        seekBarA.setOnSeekBarChangeListener(this);
    }

    protected float caculate(int progress) {
        float scale = progress / 128f;
        return scale;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        colorMatrix.setScale(caculate(seekBarR.getProgress()), caculate(seekBarG.getProgress()),
                caculate(seekBarB.getProgress()), caculate(seekBarA.getProgress()));

        colorText.setText("颜色值：#" + Integer.toHexString(seekBarA.getProgress())
                + Integer.toHexString(seekBarR.getProgress())
                + Integer.toHexString(seekBarG.getProgress())
                + Integer.toHexString(seekBarB.getProgress()));

        colorView.setBackgroundColor(Color.argb(seekBarA.getProgress(),
                seekBarR.getProgress(),
                seekBarG.getProgress(),
                seekBarB.getProgress()));

        imageView.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
