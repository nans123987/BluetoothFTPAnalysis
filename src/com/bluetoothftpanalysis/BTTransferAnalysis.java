package com.bluetoothftpanalysis;

import java.io.File;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class BTTransferAnalysis extends Activity {

	protected static final int FILE_SEND = 10;

	protected static final String STARTTIME = "STARTTIME";

	private static int REQUEST_CODE = 1;

	private static String TARGET_DEVICE_ADDRESS = "";
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	public File file;
	long starttime = 0;
	long fileSize = 0;
	String devicedistance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bttransfer_analysis);

		Intent TargetDeviceAddress = getIntent();
		TARGET_DEVICE_ADDRESS = TargetDeviceAddress
				.getStringExtra(BluetoothTransfer.DEVICE_ADDRESS);

		RadioGroup OptionBTNGroup = (RadioGroup) findViewById(R.id.RadioBTNGrp);
		OptionBTNGroup.setOnCheckedChangeListener(OptionCheckChangeListener);

		Button SelectFileButton = (Button) findViewById(R.id.btnOpenFileExplorer);
		SelectFileButton.setOnClickListener(OpenFileExplorerClickListener);

		ImageButton sendViaBluetoothButton = (ImageButton) findViewById(R.id.btnSendFile);
		sendViaBluetoothButton
				.setOnClickListener(sendFileViaBluetoothClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bttransfer_analysis, menu);
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

		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	OnCheckedChangeListener OptionCheckChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			if (checkedId == R.id.selectManualOpt) {
				EditText txtInptDistance = (EditText) findViewById(R.id.txtDistanceInput);
				if (!txtInptDistance.isEnabled()) {
					txtInptDistance.setEnabled(true);
				}
				txtInptDistance.setInputType(InputType.TYPE_CLASS_PHONE);
				RelativeLayout manualOptResult = (RelativeLayout) findViewById(R.id.manualOptResult);
				manualOptResult.setVisibility(View.VISIBLE);

			} else if (checkedId == R.id.selectGPSOpt) {
				EditText txtInptDistance = (EditText) findViewById(R.id.txtDistanceInput);
				txtInptDistance.setText("");
				txtInptDistance.setEnabled(false);
				RelativeLayout btnSendFileLayout = (RelativeLayout) findViewById(R.id.btnSendFileViaBluetooth);
				btnSendFileLayout.setVisibility(View.VISIBLE);

			}
		}
	};

	OnClickListener OpenFileExplorerClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent OpenFileDirectory = new Intent(BTTransferAnalysis.this,
					FileListActivity.class);
			OpenFileDirectory.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivityForResult(OpenFileDirectory, REQUEST_CODE);

		}
	};

	OnClickListener sendFileViaBluetoothClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			try {
				BluetoothDevice BT_Target_Device = null;

				@SuppressWarnings("unused")
				byte[] data = null;
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
						.getBondedDevices();

				if (pairedDevices.size() > 0) {
					for (BluetoothDevice device : pairedDevices) {
						if (device.getAddress().toString()
								.equals(TARGET_DEVICE_ADDRESS)) {
							BT_Target_Device = device;
						}

					}
				}

				TextView pathLabel = (TextView) findViewById(R.id.labelFilePath);
				String uriString = pathLabel.getText().toString();
				file = new File(uriString);
				Uri.fromFile(file);

				devicedistance = ((EditText) findViewById(R.id.txtDistanceInput)).getText().toString();
				
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);

				// setMimeType(sharingIntent);
				sharingIntent.setType("*/*");

				sharingIntent.setPackage("com.android.bluetooth");
				sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
				starttime = System.currentTimeMillis();
				fileSize = Long.valueOf(file.length());
				startActivityForResult(
						Intent.createChooser(sharingIntent, "Share file"),
						FILE_SEND);
			} catch (Exception e) {
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				String pathLabeltxt = data
						.getStringExtra(FileListActivity.SELECTED_FILE_PATH);
				TextView pathLabel = (TextView) findViewById(R.id.labelFilePath);
				pathLabel.setText(pathLabeltxt);
				RelativeLayout selectedFilePathLayout = (RelativeLayout) findViewById(R.id.selectedFilePathLayout);
				selectedFilePathLayout.setVisibility(View.VISIBLE);
				RelativeLayout btnSendFileLayout = (RelativeLayout) findViewById(R.id.btnSendFileViaBluetooth);
				btnSendFileLayout.setVisibility(View.VISIBLE);
			}
		} else if (requestCode == FILE_SEND) {
			
			sendDataToParse(fileSize, starttime,
					devicedistance);
		}
	};

	private void sendDataToParse(long fileSize2, long starttime2,
			String devicedistance) {
		final ParseObject object = new ParseObject("ManualDataRateLog");
		object.put("FileSize", String.valueOf(fileSize2));
		object.put("DeviceDistance", devicedistance);
		object.put("StartTime", String.valueOf(starttime2));
		object.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {
				if (e == null) {
					Toast.makeText(getApplicationContext(),
							"Successfully updated database", Toast.LENGTH_LONG)
							.show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Error while inserting in database",
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
					Log.e("BTT", e.toString());
				}
			}
		});
	}

}
