package net.cardroid;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.inject.Inject;
import net.cardroid.analysis.CanLog;
import net.cardroid.analysis.CommandsAndPatterns;
import net.cardroid.analysis.CommandsAndPatterns.CanCommand;
import net.cardroid.analysis.LogOperationProcessor;
import net.cardroid.can.*;
import net.cardroid.io.DeviceConnectionService;
import net.cardroid.util.PropertyUtil;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectView;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class CanAnalysisActivity extends GuiceActivity {
	// Debugging
	private static final String TAG = "CanAnalysisActivity";
	private static final boolean D = true;
	private static final String LOG_PREFS_NAME = "SavedLogs";

    @InjectView (R.id.edit_text_out)        private EditText mOutEditText;
    @InjectView (R.id.status_text)          private TextView mStatusTextView;
    @InjectView (R.id.learn_button)         private ToggleButton mLearnToggleButton;
    @InjectView (R.id.can_log)              private ListView mConversationView;
    @InjectView (R.id.commands_list)        private ListView mCommandsView;
    @InjectView (R.id.button_send)          private Button mSendButton;
    @InjectView (R.id.clear_button)         private Button mClearButton;
    @InjectView (R.id.edit_message_button)  private Button mEditMessageButton;
    @InjectView (R.id.command_input_layout) private LinearLayout mCommandInputLayout;
    @InjectView (R.id.operations_spinner)   private Spinner mOperationsSpinner;

    @Inject private Can232Adapter mCanAdapter;
    @Inject private CanLog mCanLog;
    @Inject private Cardroid mCardroid;
    @Inject private DeviceConnectionService mDeviceConnectionService;

	private ArrayAdapter<String> mConversationArrayAdapter;
	private ArrayAdapter<CommandsAndPatterns.CanCommand> mCommandsArrayAdapter;

	private final LogOperationProcessor mLogProcessor = new LogOperationProcessor();
	
	private final CommandsAndPatterns mCommandsAndPatterns = new CommandsAndPatterns();
	private String mPreviousCommand = "";
	private boolean mFilterOn = true;
    private CanListener mCanListener;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.can_analysis);

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.can_analysis_message);
        mConversationView.setAdapter(mConversationArrayAdapter);
        mConversationView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View v,
                    int position, long id) {
                mCanAdapter.scheduleCommand(mConversationArrayAdapter.getItem(position));
            }
        });

        // Initialize the array adapter for the commands list
        mCommandsArrayAdapter = new ArrayAdapter<CanCommand>(this,
                R.layout.can_analysis_command);
        mCommandsView.setAdapter(mCommandsArrayAdapter);
        mCommandsView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View v,
                    int position, long id) {
                executeCommand(mCommandsArrayAdapter.getItem(position));
            }
        });

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                String message = mOutEditText.getText().toString();
                if (message.length() == 0) {
                    message = mPreviousCommand;
                }

                mCanAdapter.scheduleCommand(message);
                mOutEditText.setText("");
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.operation_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOperationsSpinner.setAdapter(adapter);

        mClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mConversationArrayAdapter.clear();
            }
        });
        mClearButton.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mLearnToggleButton.setChecked(false);
                mCanLog.clear();
                updateStatusView();
                return false;
            }
        });

        mEditMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreviousCommand != null) {
                    EditMessageDialog
                        .show(CanAnalysisActivity.this, mCommandsAndPatterns, mPreviousCommand)
                        .setOnDismissListener(new OnDismissListener() {
                            @Override public void onDismiss(DialogInterface dialog) {
                                updateCommandsView();
                            }
                        });
                }
            }
        });

        mLearnToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mCardroid.isFake()) {
                    mCommandInputLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
                mOperationsSpinner.setEnabled(!isChecked);

                if (isChecked) {
                    mLogProcessor.startOperation(mOperationsSpinner.getSelectedItemPosition(), mCanLog);
                } else {
                    mLogProcessor.stopOperation();
                }
                updateStatusView();
            }
        });

        mCanListener = new CanListenerForHandler(createCanListener(), new Handler());
        mCanAdapter.addListener(mCanListener);
		mCommandsAndPatterns.loadSettings(this);
	}

    @Override protected void onStart() {
        super.onStart();
        BluetoothConnectActivity.verifyConnected(this, mCanAdapter);
    }

    @Override protected void onResume() {
		super.onResume();
		updateStatusView();
		updateCommandsView();
	}

	@Override protected void onDestroy() {
		super.onDestroy();

		mCanAdapter.removeListener(mCanListener);
	}
		
	@Override public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.can_analysis_menu, menu);
	    return true;
	}	
	
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.findItem(R.id.filter_on_menu).setChecked(mFilterOn);
    	menu.findItem(R.id.fake_on_menu).setChecked(mCardroid.isFake());
    	return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.fake_on_menu:
			mCardroid.setIsFake(!item.isChecked());
			break;
		case R.id.filter_on_menu:
			mFilterOn = !item.isChecked();
			break;
		case R.id.view_suspected_menu:
			// disable learning and button toggle, so we'll not do 
			// unexpected things while activity is in background 
			mLearnToggleButton.setChecked(false);
			
			startActivity(new Intent(this, CanLogActivity.class));
			break;
		case R.id.replay_menu:
			replayMessages();
			break;
		case R.id.save_log_menu:
			saveCanLog("canlog");
			break;
		case R.id.load_log_menu:
			loadCanLog("canlog");
			break;
		case R.id.edit_command_menu:			
			EditCommandDialog
				.show(CanAnalysisActivity.this, mCommandsAndPatterns, mCanLog.copy())
				.setOnDismissListener(new OnDismissListener() {					
					@Override public void onDismiss(DialogInterface dialog) {
						updateCommandsView();
					}
				});
			break;			
		}
		return true;
	}

    private void printMessage(String message) {
		Map.Entry<Pattern, String> pattern = mCommandsAndPatterns.findPattern(message);

		if (pattern != null) {
			mConversationArrayAdapter.add(message + " - " + pattern.getValue());
		} else {
			mConversationArrayAdapter.add(message);
		}
	}

	private CanListener createCanListener() {
        return new CanListenerAdapter() {
            @Override public void errorReceived() {
                printMessage("ERROR");
            }

            @Override public void messageReceived(CanMessage message) {
                boolean passesFilter = false;

                if (mLearnToggleButton.isChecked()) {
                    passesFilter = mLogProcessor.applyMessage(message);
                    if (!mFilterOn || passesFilter) {
                        printMessage(message.toString());
                    }
                    updateStatusView();
                } else {
                    if (!mFilterOn || mCanLog.findItem(message) != null) {
                        printMessage(message.toString());
                    }
                }
            }

            @Override public void beforeMessageSent(String message) {
                printMessage(message);
                mPreviousCommand = message;
            }

            @Override public void unknownDataReceived(String data) {
                printMessage(data);
            }

            @Override public void commandExecuted() {
                printMessage("z");
            }

            @Override
            public void connectionLost() {
                BluetoothConnectActivity.start(CanAnalysisActivity.this);
            }
        };
    }
	
	private void updateCommandsView() {
		List<CanCommand> allCommands = mCommandsAndPatterns.getCommandsSortedByName();
				
		mCommandsArrayAdapter.clear();
		for (CanCommand command : allCommands) {
			mCommandsArrayAdapter.add(command);
		}		
	}

	private void executeCommand(final CanCommand command) {
        mCanAdapter.runInCommandThread(new Runnable() {
            @Override
            public void run() {
                try {
                    command.runCommand(mCanAdapter);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to execute command " + command.toString(), e);
                }
            }
        });
	}

	private void updateStatusView() {
		mStatusTextView.setText("Log : " + mCanLog.size());
	}

	private void replayMessages() {
		executeCommand(CommandsAndPatterns.createCanLogCommand("<noname>", mCanLog));
	}
	
	private void saveCanLog(String name) {
		try {
			Properties properties = new Properties();
			mCanLog.save(properties, "log");
			PropertyUtil.writePropertiesFile(this, properties, name);
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
	}

	private void loadCanLog(String name) {
		try {
			Properties properties = PropertyUtil.readPropertiesFile(this, name);
			mCanLog.load(properties, "log");
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
		}
		updateStatusView();
	}
}
