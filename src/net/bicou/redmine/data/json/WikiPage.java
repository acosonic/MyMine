package net.bicou.redmine.data.json;

import android.database.Cursor;
import net.bicou.redmine.data.Server;
import net.bicou.redmine.data.sqlite.DbAdapter;
import net.bicou.redmine.data.sqlite.ProjectsDbAdapter;
import net.bicou.redmine.data.sqlite.ServersDbAdapter;
import net.bicou.redmine.data.sqlite.WikiDbAdapter;
import net.bicou.redmine.util.L;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class WikiPage {
	public String title;
	public String text;
	public int version;
	public Reference author;
	public String comments;
	public Calendar created_on;
	public Calendar updated_on;

	public Server server;
	public Project project;

	public boolean is_favorite;

	public WikiPage() {
		author = new Reference();
	}

	public WikiPage(final Cursor c, final Server server, final Project project) {
		this.server = server;
		this.project = project;
		int columnIndex;
		for (final String col : WikiDbAdapter.WIKI_FIELDS) {
			try {
				columnIndex = c.getColumnIndex(col);
				if (col.equals(WikiDbAdapter.KEY_AUTHOR_ID)) {
					// TODO
				} else if (col.equals(WikiDbAdapter.KEY_COMMENTS)) {
					comments = c.getString(columnIndex);
				} else if (col.equals(WikiDbAdapter.KEY_CREATED_ON)) {
					created_on = new GregorianCalendar();
					created_on.setTimeInMillis(c.getLong(columnIndex));
				} else if (col.equals(WikiDbAdapter.KEY_UPDATED_ON)) {
					updated_on = new GregorianCalendar();
					updated_on.setTimeInMillis(c.getLong(columnIndex));
				} else if (col.equals(WikiDbAdapter.KEY_TEXT)) {
					text = c.getString(columnIndex);
				} else if (col.equals(WikiDbAdapter.KEY_TITLE)) {
					title = c.getString(columnIndex);
				} else if (col.equals(WikiDbAdapter.KEY_VERSION)) {
					version = c.getInt(columnIndex);
				} else if (col.equals(WikiDbAdapter.KEY_PROJECT_ID)) {
					// not needed
				} else if (col.equals(WikiDbAdapter.KEY_SERVER_ID)) {
					// not needed
				} else {
					L.e("Unhandled column: " + col, null);
				}
			} catch (final Exception e) {
			}
		}
	}

	public WikiPage(final Cursor c, DbAdapter db) {
		this(c, null, null);
		ServersDbAdapter sdb = new ServersDbAdapter(db);
		server = sdb.getServer(c.getLong(c.getColumnIndex(WikiDbAdapter.KEY_SERVER_ID)));
		ProjectsDbAdapter pdb = new ProjectsDbAdapter(db);
		project = pdb.select(server, c.getLong(c.getColumnIndex(WikiDbAdapter.KEY_PROJECT_ID)));
	}
}
