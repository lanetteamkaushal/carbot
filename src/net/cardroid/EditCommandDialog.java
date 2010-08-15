package net.cardroid;

import net.cardroid.analysis.CanLog;
import net.cardroid.analysis.CommandsAndPatterns;
import net.cardroid.analysis.CommandsAndPatterns.CanLogCommand;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditCommandDialog {
	public static Dialog show(final Context context, final CommandsAndPatterns commandsAndPatterns, final CanLog canLog) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.edit_command_dialog);

		Button saveButton = (Button) dialog.findViewById(R.id.save_button);
		Button deleteButton = (Button) dialog.findViewById(R.id.delete_button);
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);

		final EditText commandName = (EditText) dialog.findViewById(R.id.command_name);

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = commandName.getText().toString();
				
				CanLogCommand command = CommandsAndPatterns.createCanLogCommand(name, canLog);
				commandsAndPatterns.removeCommandByName(name);
				commandsAndPatterns.addCommand(command);
				commandsAndPatterns.save(context);
				dialog.dismiss();
			}
		});

		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				commandsAndPatterns.removeCommandByName(commandName.getText().toString());
				commandsAndPatterns.save(context);
				dialog.dismiss();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});
		dialog.show();
		return dialog;
	}
}
