package net.bicou.splitactivity.samples;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

public class EmptyFragment extends Fragment{
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		Log.d("SplitActivitySamples", "EmptyFragment#onCreateView(" + savedInstanceState + ")");
		return inflater.inflate(R.layout.fragment_content_empty, parent, false);
	}
}
