package net.bicou.redmine.app.issues;

import net.bicou.redmine.R;
import net.bicou.redmine.data.sqlite.DbAdapter;
import net.bicou.redmine.data.sqlite.IssueStatusesDbAdapter;
import net.bicou.redmine.data.sqlite.IssuesDbAdapter;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * CursorAdapter that will map Cursor data to a layout
 * 
 * @author bicou
 * 
 */
public final class IssuesListCursorAdapter extends CursorAdapter {
	private final Context mContext;

	public class ViewHolder {
		TextView issueID, subject, targetVersion, status, description, project;
	}

	public IssuesListCursorAdapter(final Context context, final Cursor c, final int flags) {
		super(context, c, flags);
		mContext = context;
	}

	public IssuesListCursorAdapter(final Context context, final Cursor c, final boolean autoRequery) {
		super(context, c, autoRequery);
		mContext = context;
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = LayoutInflater.from(mContext).inflate(R.layout.issue_listitem, parent, false);
		final ViewHolder viewHolder = new ViewHolder();
		viewHolder.issueID = (TextView) view.findViewById(R.id.issue_item_id);
		viewHolder.subject = (TextView) view.findViewById(R.id.issue_item_subject);
		viewHolder.targetVersion = (TextView) view.findViewById(R.id.issue_item_fixed_version);
		viewHolder.status = (TextView) view.findViewById(R.id.issue_item_status);
		viewHolder.description = (TextView) view.findViewById(R.id.issue_item_description);
		viewHolder.project = (TextView) view.findViewById(R.id.issue_item_project_name);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final ViewHolder viewHolder = (ViewHolder) view.getTag();
		final long issueId = Long.parseLong(cursor.getString(cursor.getColumnIndex(DbAdapter.KEY_ROWID)));
		viewHolder.issueID.setText("#" + issueId);
		viewHolder.subject.setText(cursor.getString(cursor.getColumnIndex(IssuesDbAdapter.KEY_SUBJECT)));

		String version = null;
		String status = null;

		try {
			version = cursor.getString(cursor.getColumnIndex(IssuesDbAdapter.KEY_FIXED_VERSION));
		} catch (final Exception e) {
		}
		try {
			status = cursor.getString(cursor.getColumnIndex(IssuesDbAdapter.KEY_STATUS));
		} catch (final Exception e) {
		}

		if (TextUtils.isEmpty(version)) {
			version = mContext.getString(R.string.issue_version_na);
		} else {
			version = mContext.getString(R.string.issue_listview_target_version, version);
		}
		final String desc = cursor.getString(cursor.getColumnIndex(IssuesDbAdapter.KEY_DESCRIPTION));
		viewHolder.description.setText(desc);// Html.fromHtml(Util.htmlFromTextile(desc)));
		viewHolder.targetVersion.setText(version);
		viewHolder.status.setText(TextUtils.isEmpty(status) ? mContext.getString(R.string.issue_status_na) : status);
		viewHolder.project.setText(cursor.getString(cursor.getColumnIndex(IssuesDbAdapter.KEY_PROJECT)));

		final String isClosed = cursor.getString(cursor.getColumnIndex(IssueStatusesDbAdapter.KEY_IS_CLOSED));
		if ("1".equals(isClosed)) {
			viewHolder.subject.setPaintFlags(viewHolder.subject.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		} else {
			viewHolder.subject.setPaintFlags(viewHolder.subject.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
		}
	}
}