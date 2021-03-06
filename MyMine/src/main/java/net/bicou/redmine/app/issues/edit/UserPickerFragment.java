package net.bicou.redmine.app.issues.edit;

import android.app.Dialog;
import android.os.Bundle;
import com.google.gson.Gson;

import net.bicou.redmine.Constants;
import net.bicou.redmine.data.json.User;
import net.bicou.redmine.app.ga.TrackedDialogFragment;
import net.bicou.redmine.util.L;

/**
 * Created by bicou on 06/08/13.
 */
public class UserPickerFragment extends TrackedDialogFragment {
	public static final String KEY_USER = "net.bicou.redmine.app.issues.edit.User";

	public static UserPickerFragment newInstance(Bundle args) {
		UserPickerFragment frag = new UserPickerFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String json = savedInstanceState == null ? getArguments().getString(KEY_USER) : savedInstanceState.getString(KEY_USER);
		long serverId = getArguments().getLong(Constants.KEY_SERVER_ID);

		UserPickerDialog.OnUserSelectedListener listener = null;
		L.d("act=" + getActivity());
		if (getActivity() instanceof UserPickerDialog.OnUserSelectedListener) {
			listener = (UserPickerDialog.OnUserSelectedListener) getActivity();
		}
		return new UserPickerDialog(getActivity(), new Gson().fromJson(json, User.class), serverId, listener);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_USER, new Gson().toJson(((UserPickerDialog) getDialog()).getUser(), User.class));
	}
}
