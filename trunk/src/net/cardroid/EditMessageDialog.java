package net.cardroid;

import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.cardroid.analysis.CommandsAndPatterns;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditMessageDialog {
	public static Dialog show(final Context context, final CommandsAndPatterns commandsAndPatterns, String command) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.edit_message_dialog);

		Button saveButton = (Button) dialog.findViewById(R.id.save_button);
		Button deleteButton = (Button) dialog.findViewById(R.id.delete_button);
		Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);

		final EditText messageName = (EditText) dialog.findViewById(R.id.messsage_name_edit);
		final EditText messagePattern = (EditText) dialog.findViewById(R.id.messsage_pattern);

		final Entry<Pattern, String> pattern = commandsAndPatterns.findPattern(command);

		if (pattern != null) {
			messagePattern.setText(pattern.getKey().toString());
			messageName.setText(pattern.getValue());
		} else {
			messagePattern.setText(command);
			deleteButton.setEnabled(false);
		}

		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Pattern newPattern = Pattern.compile(messagePattern
							.getText().toString());
					String newName = messageName.getText().toString();

					if (pattern != null) {
						commandsAndPatterns.removePatternByName(pattern.getValue());
					}

					commandsAndPatterns.addPattern(newName, newPattern);
					commandsAndPatterns.save(context);
					dialog.dismiss();
				} catch (PatternSyntaxException e) {
					new AlertDialog.Builder(context)
							.setMessage("Cannot parse pattern " + messagePattern)
							.show();
				}
			}
		});

		deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (pattern != null) {
					commandsAndPatterns.removePatternByName(pattern.getValue());
				}
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
