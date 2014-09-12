package com.manas.asyncimageloader.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.manas.asyncimageloader.AsyncImageLoader;

public class MyActivity extends Activity implements View.OnClickListener{

    Button listButton;
    Button gridButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        findViews();
        listButton.setOnClickListener(this);
        gridButton.setOnClickListener(this);
    }

    private void findViews() {
        listButton = (Button) findViewById(R.id.listSampleButton);
        gridButton = (Button) findViewById(R.id.gridSampleButton);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_clear_cache) {
            AsyncImageLoader.getInstance().clearCache();
            Toast.makeText(this, "Cache successfully cleared", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.listSampleButton:
                Intent listActivityIntent = new Intent(this, ListActivity.class);
                startActivity(listActivityIntent);
                break;
            case R.id.gridSampleButton:
                Intent gridActivityIntent = new Intent(this, GridActivity.class);
                startActivity(gridActivityIntent);
                break;
        }
    }
}
