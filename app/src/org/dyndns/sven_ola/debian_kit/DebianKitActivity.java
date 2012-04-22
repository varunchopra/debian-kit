package org.dyndns.sven_ola.debian_kit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DebianKitActivity extends Activity
{
	int ic_cpu;
	int ic_mem;
	int ic_fsys;
	int ic_dev;
	int ic_mod;
	int ic_mnt;
	int ic_deb;

	// Change image for a textview entry
	private int setDrawable(int res, int ic)
	{
		TextView tv = (TextView)findViewById(res);
		tv.setCompoundDrawablesWithIntrinsicBounds(ic, 0, 0, 0);
		return ic;
	}

	// Check if CPU_ABI is arm or x86
	private int checkCpu()
	{
		int ic = R.drawable.ic_maybe;
		if (0 < android.os.Build.CPU_ABI.length())
		{
			if (android.os.Build.CPU_ABI.startsWith("x86") ||
				android.os.Build.CPU_ABI.startsWith("armeabi"))
			{
				ic = R.drawable.ic_passed;
			}
			else
			{
				ic = R.drawable.ic_failed;
			}
		}
		return setDrawable(R.id.TextView_cpu, ic);
	}
	
	// Check if absolute RAM above certain threshold
	private int checkMem()
	{
		int ic = R.drawable.ic_unknown;
		File file = new File("/proc/meminfo");
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			// Example: MemTotal: 2050880 kB
			Pattern p = Pattern.compile("^MemTotal:\\s+(\\d+)\\s+kB$");
			Matcher m = p.matcher(br.readLine());
			if (m.find())
			{
				int tm=Integer.parseInt(m.group(1));
				if (tm < 90000)
				{
					ic = R.drawable.ic_failed;
				}
				else if (tm < 120000)
				{
					ic = R.drawable.ic_maybe;
				}
				else
				{
					ic = R.drawable.ic_passed;
				}
			}
		}
		catch (IOException ex) {}
		return setDrawable(R.id.TextView_mem, ic);
	}

	// Check if ext2/3/4 file systems supported
	private int checkFsys()
	{
		int ic = R.drawable.ic_unknown;
		File file = new File("/proc/filesystems");
		try
		{
			String line;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while (null != (line = br.readLine()) && R.drawable.ic_passed != ic)
			{
				if (line.matches("\\s+ext[234]$"))
				{
					ic = R.drawable.ic_passed;
				}
			}
			if (R.drawable.ic_passed != ic)
			{
				ic = R.drawable.ic_failed;
			}
		}
		catch (IOException ex) {}
		return setDrawable(R.id.TextView_fsys, ic);
	}

	// Check if kernel modules are supported
	private int checkMod()
	{
		int ic = R.drawable.ic_failed;
		File file = new File("/proc/modules");
		if (file.exists())
		{
			ic = R.drawable.ic_passed;
		}
		
		return setDrawable(R.id.TextView_mod, ic);
	}

	// Check if loop-mounts are supported
	private int checkDev()
	{
		int ic = R.drawable.ic_unknown;
		File file = new File("/proc/devices");
		try
		{
			String line;
			BufferedReader br = new BufferedReader(new FileReader(file));
			while (null != (line = br.readLine()) && R.drawable.ic_passed != ic)
			{
				if (line.matches("^\\s+7\\s+loop$"))
				{
					ic = R.drawable.ic_passed;
				}
			}
			if (R.drawable.ic_passed != ic)
			{
				ic = R.drawable.ic_failed;
			}
		}
		catch (IOException ex) {}
		return setDrawable(R.id.TextView_dev, ic);
	}

	// Check if /data is not mounted with noexec
	private int checkMnt()
	{
		int ic = R.drawable.ic_unknown;
		File file = new File("/proc/mounts");
		try
		{
			String line;
			Pattern p = Pattern.compile(String.format("^\\S+\\s+%s\\s+\\S+\\s+(\\S+)", Environment.getDataDirectory().getAbsolutePath()));
			BufferedReader br = new BufferedReader(new FileReader(file));
			while (null != (line = br.readLine()) && R.drawable.ic_passed != ic&& R.drawable.ic_failed != ic)
			{
				Matcher m = p.matcher(line);
				if (m.find())
				{
					int i;
					String[] opts = m.group(1).split(",");
					for (i = 0; i < opts.length; i++)
					{
						if (opts[i].contentEquals("noexec"))
						{
							ic = R.drawable.ic_failed;
						}
					}
					if (R.drawable.ic_failed != ic)
					{
						ic = R.drawable.ic_passed;
					}
				}
			}
			if (R.drawable.ic_passed != ic && R.drawable.ic_failed != ic)
			{
				ic = R.drawable.ic_maybe;
			}
		}
		catch (IOException ex) {}
		return setDrawable(R.id.TextView_mnt, ic);
	}

	// Check if /data/local/deb/bootdeb is available
	private int checkDeb()
	{
		int ic = R.drawable.ic_failed;
		File file = new File(String.format("%s/local/deb/bootdeb", Environment.getDataDirectory().getAbsolutePath()));
		if (file.exists())
		{
			ic = R.drawable.ic_maybe;
			ProcessBuilder cmd;
			try
			{
				String result = "";
				String[] args = {"/system/bin/ls", "-l", file.getAbsolutePath()};
				cmd = new ProcessBuilder(args);

	 			Process process = cmd.start();
	 			InputStream in = process.getInputStream();
	 			byte[] re = new byte[1024];
	 			while(in.read(re) != -1)
	 			{
	  				result = result + new String(re);
	 			}
	 			in.close();
	 			if (result.startsWith("-rwx"))
	 			{
	 				ic = R.drawable.ic_passed;
	 			}
			}
			catch(IOException ex)
			{
	 			ex.printStackTrace();
			}
		}
		
		return setDrawable(R.id.TextView_deb, ic);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView tv = (TextView)findViewById(R.id.TextView_mnt);
		tv.setText(String.format(getString(R.string.str_mnt), Environment.getDataDirectory().getAbsolutePath()));

		ic_cpu = checkCpu();
		ic_mem = checkMem();
		ic_fsys = checkFsys();
		ic_mod = checkMod();
		ic_dev = checkDev();
		ic_mnt = checkMnt();
		ic_deb = checkDeb();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	private void onAbout()
	{
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void onRepo()
	{
		Intent intent = new Intent(this, RepoActivity.class);
		startActivity(intent);
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.quit:
			finish();
			return true;

			case R.id.repo:
			onRepo();
			return true;

			case R.id.help:
			onAbout();
			return true;
		}
		return false;
	}
}
