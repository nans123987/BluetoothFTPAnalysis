package com.bluetoothftpanalysis;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class BluetoothTransfer extends Activity {

	private static final int DISCOVERABLE_REQUEST = 1;
	protected static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public BluetoothServerSocket serverSocket;
	public FileOutputStream fos;

	private static String tag = "debugging";

	protected static final int MESSAGE_READ_SAVE = 1;
	private static final String BT_DISCONNECTED = "android.bluetooth.device.action.ACL_DISCONNECTED";
	private static final String BT_CONNECTED = "android.bluetooth.device.action.ACL_CONNECTED";
	protected static final String STARTTIME = "STARTTIME";
	String endTime;
	double datarate;
	double downloadTime;
	
	final int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	String deviceDetail;
	
	public  BroadcastReceiver mReceiver;
	public ArrayList<String> deviceAddressList = new ArrayList<String>();
	public ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_transfer);

		 mReceiver = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (action == BT_DISCONNECTED) {
		        	endTime = String.valueOf(System.currentTimeMillis());
		            ParseQuery<ParseObject> query = ParseQuery.getQuery("ManualDataRateLog");
		            query.orderByDescending("createdAt").setLimit(1);
		            query.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> results,
								ParseException e) {
							if (e == null) {
		                        // Success scenario.
		                        ParseObject tempObj;
		                        Iterator<ParseObject> iterator = results.iterator();
		                        //Iterating through the list of tuples returned from Parse.
		                        while(iterator.hasNext()){
		                            tempObj = iterator.next();
		                            System.out.println(tempObj.getString("FileSize"));
		                            long fileSize = Long.valueOf(tempObj.getString("FileSize"));
		                            downloadTime = (Double.valueOf(endTime) - Double.valueOf(tempObj.getString("StartTime")))/1000;
		                            datarate = Double.valueOf(fileSize)/(downloadTime);
		                            System.out.println(tempObj.getObjectId());
		                            String objectId = tempObj.getObjectId();
		                            ParseQuery<ParseObject> query = ParseQuery.getQuery("ManualDataRateLog");
		        		            query.getInBackground(objectId, new GetCallback<ParseObject>() {
										@Override
										public void done(ParseObject tempObj,
												ParseException e) {
											if (e == null) {
												tempObj.put("EndTime", endTime);
					                            tempObj.put("DataRate", String.valueOf(datarate));
					                            tempObj.put("DownloadTime", String.valueOf(downloadTime));
					                            tempObj.saveInBackground(new SaveCallback() {
													
													@Override
													public void done(ParseException arg0) {
														if (arg0 == null) {
															System.out
																	.println("Done");
														}else{
															arg0.printStackTrace();
														}														
													}
												});
											    }											
										}
		        		            });
		                        }
		                    } else {
		                        //Fail scenario
		                        Log.d(this.getClass().getSimpleName(),"No results found");
		                    }// TODO Auto-generated method stub
							
						}
		            });
		        }
		    }
		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
	    
		this.registerReceiver(mReceiver, filter);
	}

	@Override
	public void onPause() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(mReceiver);
	}



	
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.bluetooth_transfer, menu);
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

	public void SwitchOnBluetoothAdapter(View view) {

		Button btnSwitchOn = (Button) findViewById(R.id.btnSwitchOnBluetooth);
		if (mBluetoothAdapter == null) {

			System.out.println("The device doess not support bluetooth");
		}

		if (!mBluetoothAdapter.isEnabled()) {
			startActivityForResult(new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE),
					DISCOVERABLE_REQUEST);
			btnSwitchOn.setText("Switch Off Bluetooth");
		} else {
			mBluetoothAdapter.disable();
			ShowToastMsg("Turning off bluetooth");
			ListView deviceListView = (ListView) findViewById(R.id.deviceList);
			deviceListView.setAdapter(null);
			btnSwitchOn.setText("Switch On Bluetooth");
		}

	}

	@SuppressWarnings("unused")
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DISCOVERABLE_REQUEST) {
			boolean isDiscoverable = resultCode > 0;
			if (isDiscoverable) {

			      }
			}
		}
	

	public void SearchPairedDevices(View view) {
		findDevices();
	}

	private void findDevices() {

		ListView deviceListView = (ListView) findViewById(R.id.deviceList);

		ArrayAdapter<String> btArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_list_items);

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				btArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
				deviceList.add(device);
				deviceAddressList.add(device.getAddress());
			}
			deviceListView.setAdapter(btArrayAdapter);
		}
		deviceListView.setOnItemClickListener(deviceItemClickListener);
	}

	private OnItemClickListener deviceItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int position,
				long arg3) {
			Intent BTTransfer = new Intent(BluetoothTransfer.this,
					BTTransferAnalysis.class);
			BTTransfer
					.putExtra(DEVICE_ADDRESS, deviceAddressList.get(position));
			startActivity(BTTransfer);

		}
	};

}
