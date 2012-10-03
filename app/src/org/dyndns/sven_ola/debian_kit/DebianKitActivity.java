package org.dyndns.sven_ola.debian_kit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import android.view.View;
import android.widget.TextView;

public class DebianKitActivity extends Activity
{
	private static String execShell(boolean oneline, String[] args)
	{
		String s_ret = "";
		try
		{
			ProcessBuilder cmd = new ProcessBuilder(args);
			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while(in.read(re) != -1 && (!oneline || 0 == s_ret.length()))
			{
				s_ret = s_ret + new String(re);
			}
			in.close();
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
			return null;
		}
		if (oneline) {
			int i = s_ret.indexOf('\n');
			if (0 <= i) return s_ret.substring(0, i);
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
			String s;

			// Check if CPU_ABI is arm or x86
			v_cpu.ic = R.drawable.ic_maybe;
			v_cpu.s = getString(R.string.str_cpu_maybe);
			if (0 < android.os.Build.CPU_ABI.length())
			{
				if (android.os.Build.CPU_ABI.startsWith("x86") ||
					android.os.Build.CPU_ABI.startsWith("armeabi"))
				{
					v_cpu.ic = R.drawable.ic_passed;
					v_cpu.s = String.format(getString(R.string.str_cpu_passed), android.os.Build.CPU_ABI);
				}
				else
				{
					v_cpu.ic = R.drawable.ic_failed;
					v_cpu.s = String.format(getString(R.string.str_cpu_failed), android.os.Build.CPU_ABI);
				}
			}
			publishProgress(v_cpu);

			// Check if absolute RAM above certain threshold
			v_mem.ic = R.drawable.ic_unknown;
			v_mem.s = getString(R.string.str_mem_unknown);
			try
			{
				File file = new File("/proc/meminfo");
				BufferedReader br = new BufferedReader(new FileReader(file), 4096);
				// Example: MemTotal: 2050880 kB
				Pattern p = Pattern.compile("^MemTotal:\\s+(\\d+)\\s+kB$");
				Matcher m = p.matcher(br.readLine());
				if (m.find())
				{
					int tm=Integer.parseInt(m.group(1));
					if (tm < 90000)
					{
						v_mem.ic = R.drawable.ic_failed;
						v_mem.s = String.format(getString(R.string.str_mem_failed), Math.round(tm / 1024));
					}
					else if (tm < 120000)
					{
						v_mem.ic = R.drawable.ic_maybe;
						v_mem.s = String.format(getString(R.string.str_mem_maybe), Math.round(tm / 1024));
					}
					else
					{
						v_mem.ic = R.drawable.ic_passed;
						v_mem.s = String.format(getString(R.string.str_mem_passed), Math.round(tm / 1024));
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
			v_ext.s = getString(R.string.str_ext_unknown);
			try
			{
				String line;
				File file = new File("/proc/filesystems");
				BufferedReader br = new BufferedReader(new FileReader(file), 4096);
				while (null != (line = br.readLine()) && R.drawable.ic_passed != v_ext.ic)
				{
					if (line.matches("\\t+ext[234]$"))
					{
						v_ext.ic = R.drawable.ic_passed;
						v_ext.s = String.format(getString(R.string.str_ext_passed), line.substring(line.lastIndexOf('\t') + 1));
					}
				}
				if (R.drawable.ic_passed != v_ext.ic)
				{
					v_ext.ic = R.drawable.ic_failed;
					v_ext.s = getString(R.string.str_ext_failed);
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			publishProgress(v_ext);

			// Check if loop-mounts are supported
			v_dev.ic = R.drawable.ic_unknown;
			v_dev.s = getString(R.string.str_dev_unknown);
			try
			{
				String line;
				File file = new File("/proc/devices");
				BufferedReader br = new BufferedReader(new FileReader(file), 4096);
				while (null != (line = br.readLine()) && R.drawable.ic_passed != v_dev.ic)
				{
					if (line.matches("^\\s+7\\s+loop$"))
					{
						v_dev.ic = R.drawable.ic_passed;
						v_dev.s = getString(R.string.str_dev_passed);
					}
				}
				if (R.drawable.ic_passed != v_dev.ic)
				{
					v_dev.ic = R.drawable.ic_failed;
					v_dev.s = getString(R.string.str_dev_failed);
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			publishProgress(v_dev);

			// Check if kernel modules are supported
			v_mod.ic = R.drawable.ic_failed;
			v_mod.s = getString(R.string.str_mod_failed);
			{
				File file = new File("/proc/modules");
				if (file.exists())
				{
					v_mod.ic = R.drawable.ic_passed;
					v_mod.s = getString(R.string.str_mod_passed);
				}
			}
			publishProgress(v_mod);

			// Check if /data is not mounted with noexec
			v_mnt.ic = R.drawable.ic_unknown;
			v_mnt.s = getString(R.string.str_mnt_unknown);
			try
			{
				String line;
				File file = new File("/proc/mounts");
				Pattern p = Pattern.compile(String.format("^\\S+\\s+%s\\s+\\S+\\s+(\\S+)", Environment.getDataDirectory().getAbsolutePath()));
				BufferedReader br = new BufferedReader(new FileReader(file), 8192);
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
								v_mnt.s = String.format(getString(R.string.str_mnt_failed), Environment.getDataDirectory().getAbsolutePath());
							}
						}
						if (R.drawable.ic_failed != v_mnt.ic)
						{
							v_mnt.ic = R.drawable.ic_passed;
							v_mnt.s = String.format(getString(R.string.str_mnt_passed), Environment.getDataDirectory().getAbsolutePath());
						}
					}
				}
				if (R.drawable.ic_passed != v_mnt.ic && R.drawable.ic_failed != v_mnt.ic)
				{
					v_mnt.ic = R.drawable.ic_maybe;
					v_mnt.s = String.format(getString(R.string.str_mnt_maybe), Environment.getDataDirectory().getAbsolutePath());
				}
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			publishProgress(v_mnt);

			// Check if executable su with mode 6755 is available
			v_sux.ic = R.drawable.ic_failed;
			v_sux.s = getString(R.string.str_sux_failed);
			s = System.getenv("PATH");
			if (null != s)
			{
				String[] path = s.split(File.pathSeparator);
				int i;
				for(i = 0; i < path.length; i++)
				{
					File file = new File(path[i]+File.separator+"su");
					if (file.exists())
					{
						s = execShell(true, new String[] {"/system/bin/ls", "-l", file.getAbsolutePath()});
						if (null != s)
						{
							if (s.startsWith("-rws"))
							{
								v_sux.ic = R.drawable.ic_passed;
								v_sux.s = getString(R.string.str_sux_passed);
							}
							InputStream in = null;
							try
							{
								// Check if this is the Android standard-su, which does not allow uid(app)->root
								in = new BufferedInputStream(new FileInputStream(file), 16384);
								byte[] re = new byte[(int)file.length()];
								if (in.read(re) != -1)
								{
									if (0 <= new String(re).indexOf("su: uid %d not allowed to su"))
									{
										v_sux.ic = R.drawable.ic_failed;
										v_sux.s = getString(R.string.str_sux_failed);
									}
								}
								in.close();
							}
							catch(IOException ex)
							{
								ex.printStackTrace();
							}
						}
						else
						{
							v_sux.ic = R.drawable.ic_maybe;
							v_sux.s = getString(R.string.str_sux_maybe_ls);
						}
					}
				}
			}
			else
			{
				v_sux.ic = R.drawable.ic_maybe;
				v_sux.s = getString(R.string.str_sux_maybe_path);
			}
			publishProgress(v_sux);

			// Check if /data/local/deb/bootdeb is available
			s = String.format("%s/local/deb/bootdeb", Environment.getDataDirectory().getAbsolutePath());
			v_deb.ic = R.drawable.ic_failed;
			v_deb.s = String.format(getString(R.string.str_deb_failed), s);
			{
				File file = new File(s);
				if (file.exists())
				{
					s = execShell(true, new String[] {"/system/bin/ls", "-l", file.getAbsolutePath()});
					if (null != s)
					{
						if (s.startsWith("-rwx"))
						{
							v_deb.ic = R.drawable.ic_passed;
							v_deb.s = String.format(getString(R.string.str_deb_passed), file.getAbsolutePath());
						}
						else
						{
							v_deb.ic = R.drawable.ic_failed;
							v_deb.s = String.format(getString(R.string.str_deb_failed_nox), file.getAbsolutePath());
						}
						/*
						try
						{
							String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
						}
						catch (NameNotFoundException e)
						{
							Log.v(tag, e.getMessage());
						}
						*/
					}
					else
					{
						v_deb.ic = R.drawable.ic_maybe;
						v_deb.s = getString(R.string.str_deb_maybe_ls);
					}
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
				tv.setCompoundDrawablesWithIntrinsicBounds(verdicts[i].ic, 0, android.R.drawable.arrow_down_float, 0);
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

	private void onClick(TextView tv, Verdict v, int id)
	{
		TextView vSub = (TextView)findViewById(id);
		vSub.setText(v.s);
		vSub.setVisibility(View.GONE == vSub.getVisibility() ? View.VISIBLE : View.GONE);
		tv.setCompoundDrawablesWithIntrinsicBounds(
				v.ic, 0, 
				View.GONE == vSub.getVisibility() ? 
						android.R.drawable.arrow_down_float : 
						android.R.drawable.arrow_up_float, 
				0);
	}

	public void onClick_cpu(View v)
	{
		onClick((TextView)v, v_cpu, R.id.TextView_cpu_details);
	}

	public void onClick_mem(View v)
	{
		onClick((TextView)v, v_mem, R.id.TextView_mem_details);
	}

	public void onClick_ext(View v)
	{
		onClick((TextView)v, v_ext, R.id.TextView_ext_details);
	}

	public void onClick_dev(View v)
	{
		onClick((TextView)v, v_dev, R.id.TextView_dev_details);
	}

	public void onClick_mod(View v)
	{
		onClick((TextView)v, v_mod, R.id.TextView_mod_details);
	}

	public void onClick_mnt(View v)
	{
		onClick((TextView)v, v_mnt, R.id.TextView_mnt_details);
	}

	public void onClick_sux(View v)
	{
		onClick((TextView)v, v_sux, R.id.TextView_sux_details);
	}

	public void onClick_deb(View v)
	{
		onClick((TextView)v, v_deb, R.id.TextView_deb_details);
	}

}
