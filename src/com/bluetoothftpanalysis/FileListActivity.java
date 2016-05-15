package com.bluetoothftpanalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class FileListActivity extends Activity {

	protected static String SELECTED_FILE_PATH = "SELECTED_FILE_PATH";
	private List<String> FileListItems = new ArrayList<String>();
	private ArrayAdapter<String> fileListAdapter;
	private ArrayList<String> files= new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);
		
		getDirectoryFiles();
		
		
	}

   
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void ShowToastMsg(String msg) {

		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
	
	public void getDirectoryFiles(){
		
		
		ListView FileListItem_ListView= (ListView)findViewById(R.id.FileList);
		File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		if(f.isDirectory())
		{
		    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		    File fileList[] = file.listFiles();
		    for(int i=0;i< fileList.length;i++)
		    {
		    	if(fileList[i].isFile()){
			        files.add(fileList[i].getAbsolutePath());
			        FileListItems.add(fileList[i].getName()+"\n"+fileList[i].getAbsolutePath());
			        
		        }
		    }
		    fileListAdapter  = new ArrayAdapter<String>(FileListActivity.this, R.layout.device_list_items, FileListItems);
		    
		    FileListItem_ListView.setAdapter(fileListAdapter);
		}
		FileListItem_ListView.setOnItemClickListener(FileListItemClickListener);
		
		
	}

	
	OnItemClickListener FileListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			   String FileItemAbsolutePath = files.get(position);
//			   SELECTED_FILE_PATH = FileItemAbsolutePath;
			   Intent setFilePathToSelectedFileLabel = new Intent(FileListActivity.this, BTTransferAnalysis.class);
			   setFilePathToSelectedFileLabel.putExtra(SELECTED_FILE_PATH, FileItemAbsolutePath);
			   
			   setResult(RESULT_OK, setFilePathToSelectedFileLabel);
			   finish();
		}
	};
}
