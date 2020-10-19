package com.knziha.polymer.matrix;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ColorFilterActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    FiltersAdapter filtersAdapter;
    private List<float[]> filters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_filter);

        recyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        inItFilters();
        filtersAdapter = new FiltersAdapter(Glide.with(this), getLayoutInflater(), filters);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(filtersAdapter);
    }

    private void inItFilters() {
        filters.add(ColorFilter.colormatrix_heibai);
        filters.add(ColorFilter.colormatrix_fugu);
        filters.add(ColorFilter.colormatrix_gete);
        filters.add(ColorFilter.colormatrix_chuan_tong);
        filters.add(ColorFilter.colormatrix_danya);
        filters.add(ColorFilter.colormatrix_guangyun);
        filters.add(ColorFilter.colormatrix_fanse);
        filters.add(ColorFilter.colormatrix_hepian);
        filters.add(ColorFilter.colormatrix_huajiu);
        filters.add(ColorFilter.colormatrix_jiao_pian);
        filters.add(ColorFilter.colormatrix_landiao);
        filters.add(ColorFilter.colormatrix_langman);
        filters.add(ColorFilter.colormatrix_ruise);
        filters.add(ColorFilter.colormatrix_menghuan);
        filters.add(ColorFilter.colormatrix_qingning);
        filters.add(ColorFilter.colormatrix_yese);
    }
}
