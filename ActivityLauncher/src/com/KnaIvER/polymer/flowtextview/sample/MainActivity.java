package com.KnaIvER.polymer.flowtextview.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Html;
import android.view.View;
import android.widget.Button;

import com.KnaIvER.polymer.R;
import com.KnaIvER.polymer.flowtextview.FlowTextView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final float defaultFontSize = 20.0f;

    private FlowTextView flowTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flowtext_main);
        flowTextView = (FlowTextView) findViewById(R.id. ftv);
        String content = getString(R.string.lorem);
        
        flowTextView.setText(Html.fromHtml(content).toString().replace("\n", ""));
        
        Button btnIncreasefontSize = (Button) findViewById(R.id.btn_increase_font_size);
        btnIncreasefontSize.setOnClickListener(this);
        Button btnDecreasefontSize = (Button) findViewById(R.id.btn_decrease_font_size);
        btnDecreasefontSize.setOnClickListener(this);
        Button btnReset = (Button) findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_increase_font_size:
                increaseFontSize();
                break;
            case R.id.btn_decrease_font_size:
                decreaseFontSize();
                break;
            case R.id.btn_reset:
                reset();
                break;
            default:
                break;
        }
    }

    private void increaseFontSize(){
        float currentFontSize = flowTextView.getTextsize();
        flowTextView.setTextSize(currentFontSize+1);
    }

    private void decreaseFontSize(){
        float currentFontSize = flowTextView.getTextsize();
        flowTextView.setTextSize(currentFontSize-1);
    }

    private void reset(){
        flowTextView.setTextSize(defaultFontSize);
    }
}
