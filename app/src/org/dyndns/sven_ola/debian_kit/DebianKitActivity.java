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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class DebianKitActivity extends Activity
{
	private static String execShell(String[] args)
	{
		String s_ret = "";
		try
		{
			ProcessBuilder cmd = new ProcessBuilder(args);

 			Process process = cmd.start();
 			InputStream in = process.getInputStream();
 			byte[] re = new byte[1024];
 			while(in.read(re) != -1)
 			{
 				s_ret = s_ret + new String(re);
 			}
 			in.close();
		}
		catch(IOException ex)
		{
 			ex.printStackTrace();
		}
		return s_ret;
	}

	private class Verdict
	{
		public int tv;		// Resource ID of TextView that displays verdict
		public int ic;		// Resource ID of verdict
		public String s;	// Notes / details on verdict 

		public Verdict(int tv_resid)
		{
	        tv = tv_resid;
	        ic = -1;
	    }
	}
	
	Verdict v_cpu = new Verdict(R.id.TextView_cpu);
	Verdict v_mem = new Verdict(R.id.TextView_mem);
	Verdict v_ext = new Verdict(R.id.TextView_ext);
	Verdict v_dev = new Verdict(R.id.TextView_dev);
	Verdict v_mod = new Verdict(R.id.TextView_mod);
	Verdict v_mnt = new Verdict(R.id.TextView_mnt);
	Verdict v_sux = new Verdict(R.id.TextView_sux);
	Verdict v_deb = new Verdict(R.id.TextView_deb);

	private class CourtTask extends AsyncTask<Void, Verdict, Void>
	{
		protected Void doInBackground(Void... voids) {
			// Check if CPU_ABI is arm or x86
			v_cpu.ic = R.drawable.ic_maybe;
			if (0 < android.os.Build.CPU_ABI.length())
			{
				if (android.os.Build.CPU_ABI.startsWith("x86") ||
					android.os.Build.CPU_ABI.startsWith("armeabi"))
				{
					v_cpu.ic = R.drawable.ic_passed;
				}
				else
				{
					v_cpu.ic = R.drawable.ic_failed;
				}
			}
			publishProgress(v_cpu);

			// Check if absolute RAM above certain threshold
			v_mem.ic = R.drawable.ic_unknown;
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
						v_mem.ic = R.drawable.ic_failed;
					}
					else if (tm < 120000)
					{
						v_mem.ic = R.drawable.ic_maybe;
					}
					else
					{
						v_mem.ic = R.drawable.ic_passed;
					}
				}
			}
			catch(IOException ex)
			{
	 			ex.printStackTrace();
			}
			publishProgress(v_mem);

			// Check if ext2/3/4 file systems supported
			v_ext.ic = R.drawable.ic_unknown;
			file = new File("/proc/filesystems");
			try
			{
				String line;
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (null != (line = br.readLine()) && R.drawable.ic_passed != v_ext.ic)
				{
					if (line.matches("\\s+ext[234]$"))
					{
						v_ext.ic = R.drawable.ic_passed;
					}
				}
				if (R.drawable.ic_passed != v_ext.ic)
				{
					v_ext.ic = R.drawable.ic_failed;
				}
			}
			catch(IOException ex)
			{
	 			ex.printStackTrace();
			}
			publishProgress(v_ext);

			// Check if loop-mounts are supported
			v_dev.ic = R.drawable.ic_unknown;
			file = new File("/proc/devices");
			try
			{
				String line;
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (null != (line = br.readLine()) && R.drawable.ic_passed != v_dev.ic)
				{
					if (line.matches("^\\s+7\\s+loop$"))
					{
						v_dev.ic = R.drawable.ic_passed;
					}
				}
				if (R.drawable.ic_passed != v_dev.ic)
				{
					v_dev.ic = R.drawable.ic_failed;
				}
			}
			catch(IOException ex)
			{
	 			ex.printStackTrace();
			}
			publishProgress(v_dev);

			// Check if kernel modules are supported
			v_mod.ic = R.drawable.ic_failed;
			file = new File("/proc/modules");
			if (file.exists())
			{
				v_mod.ic = R.drawable.ic_passed;
			}
			publishProgress(v_mod);

			// Check if /data is not mounted with noexec
			v_mnt.ic = R.drawable.ic_unknown;
			file = new File("/proc/mounts");
			try
			{
				String line;
				Pattern p = Pattern.compile(String.format("^\\S+\\s+%s\\s+\\S+\\s+(\\S+)", Environment.getDataDirectory().getAbsolutePath()));
				BufferedReader br = new BufferedReader(new FileReader(file));
				while (null != (line = br.readLine()) && R.drawable.ic_passed != v_mnt.ic && R.drawable.ic_failed != v_mnt.ic)
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
								v_mnt.ic = R.drawable.ic_failed;
							}
						}
						if (R.drawable.ic_failed != v_mnt.ic)
						{
							v_mnt.ic = R.drawable.ic_passed;
						}
					}
				}
				if (R.drawable.ic_passed != v_mnt.ic && R.drawable.ic_failed != v_mnt.ic)
				{
					v_mnt.ic = R.drawable.ic_maybe;
				}
			}
			catch(IOException ex)
			{
	 			ex.printStackTrace();
			}
			publishProgress(v_mnt);

			// Check if executable su with mode 6755 is available
			v_sux.ic = R.drawable.ic_failed;
			file = new File("/system/bin/sh");
			if (file.exists())
			{
				int i;
				String[] args = {file.getAbsolutePath(), "-c", "echo $PATH"};
				String[] path = execShell(args).split(File.pathSeparator);
				for(i = 0; i < path.length; i++)
				{
					file = new File(path[i]+File.separator+"su");
					if (file.exists())
					{
						String[] args2 = {"/system/bin/ls", "-l", file.getAbsolutePath()};
						String mode = execShell(args2);
						if (mode.startsWith(""))
						{
							v_sux.ic = R.drawable.ic_passed;
						}
					}
				}
			}
			publishProgress(v_sux);

			// Check if /data/local/deb/bootdeb is available
			v_deb.ic = R.drawable.ic_failed;
			file = new File(String.format("%s/local/deb/bootdeb", Environment.getDataDirectory().getAbsolutePath()));
			if (file.exists())
			{
				v_deb.ic = R.drawable.ic_maybe;
				String[] args = {"/system/bin/ls", "-l", file.getAbsolutePath()};
				if (execShell(args).startsWith("-rwx"))
	 			{
					v_deb.ic = R.drawable.ic_passed;
				}
			}
			publishProgress(v_deb);
			return null;
		}

		protected void onProgressUpdate(Verdict... verdicts) {
			int i;
			for (i = 0; i < verdicts.length; i++)
			{
				TextView tv = (TextView)findViewById(verdicts[i].tv);
				tv.setCompoundDrawablesWithIntrinsicBounds(verdicts[i].ic, 0, 0, 0);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView tv = (TextView)findViewById(R.id.TextView_mnt);
		tv.setText(String.format(getString(R.string.str_mnt), Environment.getDataDirectory().getAbsolutePath()));
		new CourtTask().execute();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	private void onReadme()
	{
		Intent intent = new Intent(this, ReadmeActivity.class);
		startActivity(intent);
	}

	private void onDownload()
	{
		Intent intent = new Intent(this, DownloadActivity.class);
		startActivity(intent);
	}

	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.quit:
			finish();
			return true;

			case R.id.download:
			onDownload();
			return true;

			case R.id.readme:
			onReadme();
			return true;
		}
		return false;
	}
}
