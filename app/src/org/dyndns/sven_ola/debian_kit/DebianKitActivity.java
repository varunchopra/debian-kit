package org.dyndns.sven_ola.debian_kit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DebianKitActivity extends Activity {
	private Button cpuButton;
	private Button mntButton;
	private Button debButton;
	private Button memButton;
	private Button fsysButton;
	private Button devButton;
	
	private void checkTest(String name) {
		File file = new File(name);
		StringBuilder text = new StringBuilder();
		try {
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    String line;
		    while ((line = br.readLine()) != null) {
		        text.append(line);
		        text.append('\n');
		    }
		}
		catch (IOException e) {
		    //You'll need to add proper error handling here
		}
		/*TextView tv = (TextView)findViewById(R.id.text_view);
		tv.setText(text);*/
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        /*cpuButton = (Button)findViewById(R.id.button_cpu);
        mntButton = (Button)findViewById(R.id.button_mnt);
        debButton = (Button)findViewById(R.id.button_deb);
        memButton = (Button)findViewById(R.id.button_mem);
        fsysButton = (Button)findViewById(R.id.button_fsys);
        devButton = (Button)findViewById(R.id.button_dev);
        cpuButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/proc/cpuinfo");
            }
        });
        mntButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/proc/mounts");
            }
        });
        debButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/data/local/deb/bootdeb");
            }
        });
        memButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/proc/meminfo");
            }
        });
        fsysButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/proc/filesystems");
            }
        });
        devButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              checkTest("/proc/devices");
            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.quit:
            finish();
            return true;
        case R.id.about: {
        		Intent intent = new Intent(this, AboutActivity.class);
        		startActivity(intent);
        	}
        }
        return false;
    }
}