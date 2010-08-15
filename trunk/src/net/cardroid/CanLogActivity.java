package net.cardroid;

import android.view.*;
import android.widget.AdapterView;
import com.google.inject.Inject;
import net.cardroid.analysis.CanLog;
import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import net.cardroid.can.CanMessage;
import net.cardroid.io.DeviceConnectionService;
import roboguice.activity.GuiceActivity;
import roboguice.inject.InjectView;

public class CanLogActivity extends GuiceActivity {
	// Debugging
	private static final String TAG = "CanReplayActivity";
	private static final boolean D = true;

	@InjectView(R.id.can_replay_log) private ListView mReplayLogView;
	@InjectView(R.id.save_button) private Button mSaveButton;
	@InjectView(R.id.cancel_button) private Button mCancelButton;
    private ArrayAdapter<CanLog.Item> mReplayLogArrayAdapter;

	@Inject CanLog mReplayLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initLayout();
		loadReplayMessages();
	}

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.can_analysis_menu, menu);
	    return true;
	}	
	
	private void initLayout() {
		setContentView(R.layout.can_log_activity);

		mSaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveReplayMessages();
				CanLogActivity.this.finish();
			}
		});

		mCancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CanLogActivity.this.finish();
			}
		});
		
		mReplayLogArrayAdapter = new ArrayAdapter<CanLog.Item>(this, android.R.layout.simple_list_item_multiple_choice);
		mReplayLogView.setAdapter(mReplayLogArrayAdapter);
		for (int i = 0; i < mReplayLog.size(); i++) {
			mReplayLogView.setItemChecked(i, true);			
		}
		registerForContextMenu(mReplayLogView);
	}

    @Override public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_all_menu:
                bulkCheckItems(item.getMenuInfo(), true);
                return true;
            case R.id.deselect_all_menu:
                bulkCheckItems(item.getMenuInfo(), false);
                return true;
        }
        return false;
    }

    private void bulkCheckItems(ContextMenu.ContextMenuInfo menuInfo, boolean check) {
        CanMessage selectedCanMessage = getSelectedItem(menuInfo).getCanMessage();
        for (int i = 0; i < mReplayLogArrayAdapter.getCount(); i++) {
            if (mReplayLogArrayAdapter.getItem(i).matches(selectedCanMessage)) {
                mReplayLogView.setItemChecked(i, check);
            }
        }
    }

    @Override public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.can_log_activity_context_menu, menu);
        boolean canRunBulkActions = !mReplayLogArrayAdapter.isEmpty()
                && getSelectedItem(menuInfo).getCanMessage() != null;
        boolean isChecked = !mReplayLogArrayAdapter.isEmpty()
                && mReplayLogView.isItemChecked(getSelectedItemPosition(menuInfo));

        boolean showSelectAll = canRunBulkActions && !isChecked;
        menu.findItem(R.id.select_all_menu).setVisible(showSelectAll);
        menu.findItem(R.id.select_all_menu).setEnabled(showSelectAll);

        boolean showDeselectAll = canRunBulkActions && isChecked;
        menu.findItem(R.id.deselect_all_menu).setVisible(showDeselectAll);
        menu.findItem(R.id.deselect_all_menu).setEnabled(showDeselectAll);
    }

    private CanLog.Item getSelectedItem(ContextMenu.ContextMenuInfo menuInfo) {
        return mReplayLogArrayAdapter.getItem(getSelectedItemPosition(menuInfo));
    }

    private int getSelectedItemPosition(ContextMenu.ContextMenuInfo menuInfo) {
        return ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
    }

    private void loadReplayMessages() {
		mReplayLogArrayAdapter.clear();
		for (CanLog.Item item : mReplayLog.getItems()) {
			mReplayLogArrayAdapter.add(item);
		}		
	}

	private void saveReplayMessages() {
		for (int i = 0; i < mReplayLogView.getChildCount(); i++) {
			if (!mReplayLogView.isItemChecked(i)) {
				mReplayLog.remove(mReplayLogArrayAdapter.getItem(i));
			}
		}
	}
}
