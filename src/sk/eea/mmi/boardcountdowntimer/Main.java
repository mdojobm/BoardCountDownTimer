package sk.eea.mmi.boardcountdowntimer;

import java.lang.System;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
//import android.provider.Settings.System;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	
	public final static String EXTRA_MESSAGE = "sk.eea.mmi.boardcountdowntimer";
	
	private TextView txt_present_countdown, txt_present_name, txt_present_theme;
	private ArrayList<HashMap<String,Object>> listdata=new ArrayList<HashMap<String,Object>>();
	private long time_in_milisec;
	
	private boolean timerCount_was_start = false;
	private CountDown timerCount;
	
	private ListView mainListView;
	private Integer select_ListItem;
	
	final Context context = this;	
	
	String FILENAME = "BoardCountDownTimer";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// load all "board" records
		readFileFromInternalStorage();
		
		// define presentation TextView
		txt_present_name = (TextView) findViewById(R.id.textView_present_name);
		txt_present_theme = (TextView) findViewById(R.id.textView_present_theme);
		txt_present_countdown = (TextView) findViewById(R.id.textView_present_countdown);
		
		// load first "board"  in ListView to present. TextView (after start app.)
		txt_present_name.setText(listdata.get(0).get("lector_name").toString());
		txt_present_theme.setText(listdata.get(0).get("theme_name").toString());
		txt_present_countdown.setText(timeCalculate(60 * Long.parseLong(listdata.get(0).get("countdown_time").toString())));
		time_in_milisec = (60000 * Long.parseLong(listdata.get(0).get("countdown_time").toString())); 
		
   	 	// define selected ListItem for optionaly delete before define Item
		// If not selected Item (board) in ListItem then delete first Item in ListItem 
		// (see: f. btn_delete)
   	 	select_ListItem = 0;
   	 	
	    // Launching new screen on Selecting Single ListItem
	    mainListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	            	
            	// Canacel the earlier countdown
           	 	if(timerCount_was_start) {
           	 		timerCount.cancel(); 
           	 		timerCount_was_start = false;
           	 	}
            	
           	 	// define selected ListItem for optionaly delete item (see: f. btn_delete)
           	 	select_ListItem = position;
            	
           	 	time_in_milisec = (60000 * Long.parseLong(((TextView) view.findViewById(R.id.text_countdown_time)).getText().toString()));
            	txt_present_name.setText( ((TextView) view.findViewById(R.id.text_lector_name)).getText().toString() );
            	txt_present_theme.setText( ((TextView) view.findViewById(R.id.text_theme_name)).getText().toString() );
            	txt_present_countdown.setText(timeCalculate(time_in_milisec / 1000));            
            }	
        });

		/*
		 * START countdown selected "board" in ListView
		 * 
		 * */
		final Button btn_start = (Button) findViewById(R.id.button_start);
		btn_start.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 timerCount =  new CountDown(time_in_milisec, 1000); 
            	 timerCount.start();
            	 timerCount_was_start = true;
            	  
            }
        });
		
		/*
		 * CANCEL countdown started "board" in ListView
		 * 
		 * */
		final Button btn_cancel = (Button) findViewById(R.id.button_cancel);
		btn_cancel.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
            	 timerCount.cancel(); 
            	 txt_present_countdown.setText(timeCalculate(time_in_milisec / 1000));
            }
             
        });		

		/*
		 * DELETE selected "board" in ListView
		 * 
		 * */
		final Button btn_delete = (Button) findViewById(R.id.button_del);
		btn_delete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {			

				if ( 1 >= listdata.size() ) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setMessage(R.string.empty_board).setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // null
	                   }
	               });
				builder.show();
				} else {											
					ArrayList<HashMap<String,Object>> listdata_temp =new ArrayList<HashMap<String,Object>>();
					
					for (int i = 0; i < listdata.size(); ++i) { 
						if(i != select_ListItem) {
							HashMap<String, Object> hm = new HashMap<String, Object>();
							hm.put("lector_name", listdata.get(i).get("lector_name").toString());
							hm.put("theme_name", listdata.get(i).get("theme_name").toString());
							hm.put("countdown_time", listdata.get(i).get("countdown_time").toString());
							listdata_temp.add(hm);
						}
					}
					listdata.clear();
					listdata = listdata_temp;			    
					addToListAdapter(listdata);		
					writeFileToInternalStorage(listdata);
				}
			}
        });

		/*
		 * ADD new "board" in ListView
		 * 
		 * */
		final Button btn_add = (Button) findViewById(R.id.button_add);
		btn_add.setOnClickListener(new View.OnClickListener() {     
			@Override
			public void onClick(View arg0) {
 
				// get activity_board_manager.xml view
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.activity_board_manager, null);
 
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
				// set activity_board_manager.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);
 
				final EditText lector_name = (EditText) promptsView.findViewById(R.id.edittext_lector_name);
				final EditText theme_name = (EditText) promptsView.findViewById(R.id.edittext_theme_name);
				final EditText countdown_time = (EditText) promptsView.findViewById(R.id.edittext_countdown_time);

				// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						    
					    	HashMap<String, Object> hm = new HashMap<String, Object>();	
					    	hm.put("lector_name", lector_name.getText());
					    	hm.put("theme_name", theme_name.getText());
					    	
					    	// Countdown timer cannot be empty!
							if(countdown_time.getText().toString().trim().equals("")) {
								hm.put("countdown_time", "0");
								
								// View toast message
								Toast t =Toast.makeText(context, getString(R.string.empty_timer), 8000);
								t.show();
					    	} else hm.put("countdown_time", countdown_time.getText());

						    listdata.add(hm);
						    addToListAdapter(listdata);

						    writeFileToInternalStorage(listdata);
					    }					  
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
			}
        });
	}
	
	/*
	 * ADD to ListView new item (board)
	 * 
	 * */
	public void addToListAdapter(ArrayList<HashMap<String,Object>> listdata){
		
		String[] from = { "lector_name", "theme_name", "countdown_time" };
	    int[] to={R.id.text_lector_name,R.id.text_theme_name,R.id.text_countdown_time};
	    
	    SimpleAdapter adapter = new SimpleAdapter(this, listdata, R.layout.board_row, from, to);
	    
	    mainListView=(ListView)this.findViewById(R.id.ListView_main);HashMap<String, Object> hm = new HashMap<String, Object>();
	    mainListView.setAdapter(adapter);
	}
	
	/*
	 * CountDown manager
	 * 
	 * */
	public class CountDown extends CountDownTimer {
		
		 public CountDown(long millisInFuture, long countDownInterval) {
		      super(millisInFuture, countDownInterval);
		  }
		  @Override
		  public void onFinish() {
			  
			  //display message
			  txt_present_countdown.setText(R.string.time_out);
			  
			  // play define mp3
			  MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.metal_gong);
			  mediaPlayer.start(); // no need to call prepare(); create() does that for you
/*
			  // play default ring tone
			  try {
				  Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				  Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				  r.play();
			  } catch (Exception e) {
				  Log.v("RingtoneManager: ",e.toString());
			  }*/
		  }
		  @Override
		  public void onTick(long millisUntilFinished) {
			  txt_present_countdown.setText(timeCalculate(millisUntilFinished / 1000));
		  }   
	}
	
	/*
	 * Convert milliseconds to user friendly form (10.000 ms = 00:10:00)
	 * 
	 * */
	public String timeCalculate(double ttime) {
	    long days, hours, minutes, seconds;
	    String daysT = "", restT = "";

	    days = (Math.round(ttime) / 86400);
	    hours = (Math.round(ttime) / 3600) - (days * 24);
	    minutes = (Math.round(ttime) / 60) - (days * 1440) - (hours * 60);
	    seconds = Math.round(ttime) % 60;

	    if(days==1) daysT = String.format("%d day ", days);
	    if(days>1) daysT = String.format("%d days ", days);

	    restT = String.format("%02d:%02d:%02d", hours, minutes, seconds);
	    return daysT + restT;
	}
	
	/*
	 * Write board ListView to local file (data storage)
	 * 
	 * */
	private void writeFileToInternalStorage(ArrayList<HashMap<String,Object>> listdata) {
		  String eol = System.getProperty("line.separator");
		  BufferedWriter writer = null;
		  try {		
			  writer = new BufferedWriter(new OutputStreamWriter(openFileOutput(FILENAME, MODE_PRIVATE)));

			  for(int i=0; i<listdata.size(); i++){
				  writer.write(
						  listdata.get(i).get("lector_name")+","
						  +listdata.get(i).get("theme_name")+","
						  +listdata.get(i).get("countdown_time")+","
						  + eol);
			  }			  
		  } catch (Exception e) {
		      e.printStackTrace();
		  } finally {
			  if (writer != null) {
				  try {
					  writer.close();
				  } catch (IOException e) {
					  e.printStackTrace();
				  }	
			  }
		  }
	} 
	
	/*
	 * Read data from local file to board ListView (data storage).
	 * While list is empty, or file not exist, define default "board"
	 * 
	 * */
	private void readFileFromInternalStorage() {
	    
		BufferedReader input = null;
		try {
		    input = new BufferedReader(new InputStreamReader(openFileInput(FILENAME)));
		    String line;
		    
		    while ((line = input.readLine()) != null) {
		    	List<String> list_buffer = new ArrayList<String>(Arrays.asList(line.split(",")));
		    	if(list_buffer.size() == 3) {
			    	HashMap<String, Object> hm = new HashMap<String, Object>();
					hm.put("lector_name", list_buffer.get(0));
				    hm.put("theme_name", list_buffer.get(1));
				    hm.put("countdown_time", list_buffer.get(2));
				    listdata.add(hm);
				  
		    	}
		    }
		    addToListAdapter(listdata);		    
		    
		  } catch (Exception e) {
			  e.printStackTrace();
		  } finally {
			  if (input != null) {
				  try {
					  input.close();
				  } catch (IOException e) {
					  e.printStackTrace();
				  }
			  } else {
				  // if file not exist or is empty, define default board list
				  HashMap<String, Object> hm = new HashMap<String, Object>();
				  hm.put("lector_name", getString(R.string.start_lector_name));
				  hm.put("theme_name", getString(R.string.start_theme_title));
				  hm.put("countdown_time", getString(R.string.start_coundown_time));
				  listdata.add(hm);				    
				  addToListAdapter(listdata);
			  }
		  }
	} 
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
