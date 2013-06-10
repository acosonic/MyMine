package net.bicou.redmine.data.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import net.bicou.redmine.app.issues.IssuesListFilter;
import net.bicou.redmine.app.issues.IssuesListFilter.FilterType;
import net.bicou.redmine.app.issues.IssuesOrderColumnsAdapter.OrderColumn;
import net.bicou.redmine.data.Server;
import net.bicou.redmine.data.json.Issue;
import net.bicou.redmine.data.json.Reference;
import net.bicou.redmine.data.json.User;
import net.bicou.redmine.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IssuesDbAdapter extends DbAdapter {
	public static final String TABLE_ISSUES = "issues";

	public static final String KEY_ID = "id";
	public static final String KEY_PROJECT_ID = "project_id";
	public static final String KEY_TRACKER_ID = "tracker_id";
	public static final String KEY_PRIORITY_ID = "priority_id";
	public static final String KEY_STATUS_ID = "status_id";
	public static final String KEY_AUTHOR_ID = "author_id";
	public static final String KEY_SUBJECT = "subject";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_START_DATE = "start_date";
	public static final String KEY_DONE_RATIO = "done_ratio";
	public static final String KEY_CREATED_ON = "created_on";
	public static final String KEY_UPDATED_ON = "updated_on";
	public static final String KEY_DUE_DATE = "due_date";
	public static final String KEY_FIXED_VERSION_ID = "fixed_version_id";
	public static final String KEY_CATEGORY_ID = "category_id";
	public static final String KEY_PARENT_ID = "parent_id";
	public static final String KEY_ASSIGNED_TO_ID = "assigned_to_id";
	public static final String KEY_ESTIMATED_HOURS = "estimated_hours";
	public static final String KEY_SPENT_HOURS = "spent_hours";

	public static final String KEY_SERVER_ID = "server_id";

	// Fake columns for all the references
	public static final String KEY_PROJECT = "project";
	public static final String KEY_STATUS = "status";
	public static final String KEY_FIXED_VERSION = "version";

	public static final String TABLE_JOURNALS = "issue_journals";
	// TODO public List<Journal> journals;

	public static final String[] ISSUE_FIELDS = new String[] {
			KEY_ID,
			KEY_PROJECT_ID,
			KEY_TRACKER_ID,
			KEY_PRIORITY_ID,
			KEY_STATUS_ID,
			KEY_AUTHOR_ID,
			KEY_SUBJECT,
			KEY_DESCRIPTION,
			KEY_START_DATE,
			KEY_DONE_RATIO,
			KEY_CREATED_ON,
			KEY_UPDATED_ON,
			KEY_DUE_DATE,
			KEY_FIXED_VERSION_ID,
			KEY_CATEGORY_ID,
			KEY_PARENT_ID,
			KEY_ASSIGNED_TO_ID,
			KEY_ESTIMATED_HOURS,
			KEY_SPENT_HOURS,

			KEY_SERVER_ID,
	};

	/**
	 * Table creation statements
	 *
	 * @return
	 */
	public static final String[] getCreateTablesStatements() {
		return new String[] {
				"CREATE TABLE " + TABLE_ISSUES + "(" + Util.join(ISSUE_FIELDS, ", ") + ", PRIMARY KEY (" + KEY_ID + ", " + KEY_SERVER_ID + ", " +
						"" + KEY_PROJECT_ID + "))",
		};
	}

	public IssuesDbAdapter(final Context ctx) {
		super(ctx);
	}

	public IssuesDbAdapter(final DbAdapter db) {
		super(db);
	}

	public long insert(final Issue issue) {
		final ContentValues values = new ContentValues();
		values.put(KEY_ID, issue.id);
		values.put(KEY_PROJECT_ID, issue.project == null ? 0 : issue.project.id);
		values.put(KEY_TRACKER_ID, issue.tracker == null ? 0 : issue.tracker.id);
		values.put(KEY_PRIORITY_ID, issue.priority == null ? 0 : issue.priority.id);
		values.put(KEY_STATUS_ID, issue.status == null ? 0 : issue.status.id);
		values.put(KEY_AUTHOR_ID, issue.author == null ? 0 : issue.author.id);
		values.put(KEY_SUBJECT, issue.subject);
		values.put(KEY_DESCRIPTION, issue.description);
		values.put(KEY_START_DATE, issue.start_date == null ? 0 : issue.start_date.getTimeInMillis());
		values.put(KEY_DONE_RATIO, issue.done_ratio);
		values.put(KEY_CREATED_ON, issue.created_on == null ? 0 : issue.created_on.getTimeInMillis());
		values.put(KEY_UPDATED_ON, issue.updated_on == null ? 0 : issue.updated_on.getTimeInMillis());
		values.put(KEY_DUE_DATE, issue.due_date == null ? 0 : issue.due_date.getTimeInMillis());
		values.put(KEY_FIXED_VERSION_ID, issue.fixed_version == null ? 0 : issue.fixed_version.id);
		values.put(KEY_CATEGORY_ID, issue.category == null ? 0 : issue.category.id);
		values.put(KEY_PARENT_ID, issue.parent == null ? 0 : issue.parent.id);
		values.put(KEY_ASSIGNED_TO_ID, issue.assigned_to == null ? 0 : issue.assigned_to.id);
		values.put(KEY_ESTIMATED_HOURS, issue.estimated_hours);
		values.put(KEY_SPENT_HOURS, issue.spent_hours);
		values.put(KEY_SERVER_ID, issue.server.rowId);
		return mDb.insert(TABLE_ISSUES, "", values);
	}

	public int update(final Issue issue) {
		final ContentValues values = new ContentValues();
		values.put(KEY_PROJECT_ID, issue.project == null ? 0 : issue.project.id);
		values.put(KEY_TRACKER_ID, issue.tracker == null ? 0 : issue.tracker.id);
		values.put(KEY_PRIORITY_ID, issue.priority == null ? 0 : issue.priority.id);
		values.put(KEY_STATUS_ID, issue.status == null ? 0 : issue.status.id);
		values.put(KEY_AUTHOR_ID, issue.author == null ? 0 : issue.author.id);
		values.put(KEY_SUBJECT, issue.subject);
		values.put(KEY_DESCRIPTION, issue.description);
		values.put(KEY_START_DATE, issue.start_date == null ? 0 : issue.start_date.getTimeInMillis());
		values.put(KEY_DONE_RATIO, issue.done_ratio);
		values.put(KEY_CREATED_ON, issue.created_on == null ? 0 : issue.created_on.getTimeInMillis());
		values.put(KEY_UPDATED_ON, issue.updated_on == null ? 0 : issue.updated_on.getTimeInMillis());
		values.put(KEY_DUE_DATE, issue.due_date == null ? 0 : issue.due_date.getTimeInMillis());
		values.put(KEY_FIXED_VERSION_ID, issue.fixed_version == null ? 0 : issue.fixed_version.id);
		values.put(KEY_CATEGORY_ID, issue.category == null ? 0 : issue.category.id);
		values.put(KEY_PARENT_ID, issue.parent == null ? 0 : issue.parent.id);
		values.put(KEY_ASSIGNED_TO_ID, issue.assigned_to == null ? 0 : issue.assigned_to.id);
		values.put(KEY_ESTIMATED_HOURS, issue.estimated_hours);
		values.put(KEY_SPENT_HOURS, issue.spent_hours);
		return mDb.update(TABLE_ISSUES, values, KEY_ID + "=" + issue.id + " AND " + KEY_SERVER_ID + " = " + issue.server.rowId, null);
	}

	public Cursor selectCursor(final Server server, final long id, final String[] columns) {
		final String where = KEY_ID + " = " + id + " AND " + KEY_SERVER_ID + " = " + server.rowId;
		return mDb.query(TABLE_ISSUES, columns, where, null, null, null, null);
	}

	public Issue select(final Server server, final long issueId, final String[] columns) {
		final Cursor c = selectCursor(server, issueId, columns);
		Issue issue = null;
		if (c != null) {
			if (c.moveToFirst()) {
				issue = new Issue(server, c, this);
			}
			c.close();
		}

		return issue;
	}

	private String getOrderBy(final String[] columns, final List<OrderColumn> columnsOrder) {
		if (columns == null || columns.length <= 0 || columnsOrder == null || columnsOrder.size() <= 0) {
			return null;
		}

		final String[] orders = new String[columnsOrder.size()];
		int i = 0;
		String key;
		final List<String> cols = Arrays.asList(columns);
		for (final OrderColumn order : columnsOrder) {
			key = null;
			// Order by value instead of ID?
			if (KEY_PROJECT_ID.equals(order.key)) {
				if (cols.contains(KEY_PROJECT)) {
					key = KEY_PROJECT;
				}
			} else if (KEY_STATUS_ID.equals(order.key)) {
				if (cols.contains(KEY_STATUS)) {
					key = KEY_STATUS;
				}
			} else if (KEY_FIXED_VERSION_ID.equals(order.key)) {
				if (cols.contains(KEY_FIXED_VERSION)) {
					key = KEY_FIXED_VERSION;
				}
			}
			// Default: order by ID or raw value
			else {
				key = TABLE_ISSUES + "." + order.key;
			}

			orders[i++] = key + (order.isAscending ? " ASC" : " DESC");
		}

		return Util.join(orders, ", ");
	}

	/**
	 * Select all the issues that match the given ID from the list, on the given server.
	 *
	 * @param issueIds
	 * @param columns
	 * @return
	 */
	public Cursor selectAllCursor(final Server server, final List<Long> issueIds, final String[] columns, final List<OrderColumn> columnsOrder) {
		final List<String> selection = new ArrayList<String>();
		// KEY_ID is replaced to KEY_ROWID by findMatchingIssues
		selection.add(KEY_ROWID + " IN (" + Util.join(issueIds.toArray(), ",") + ")");
		return findMatchingIssues(selection, null, columns, getOrderBy(columns, columnsOrder));
	}

	/**
	 * Select all the issues that match the given filter.
	 *
	 * @param filter
	 * @param columns
	 * @return
	 * @throws IllegalArgumentException
	 *             if the filter is a {@link FilterType#QUERY}
	 */
	public Cursor selectAllCursor(IssuesListFilter filter, final String[] columns, final List<OrderColumn> columnsOrder) {
		if (filter == null) {
			filter = IssuesListFilter.FILTER_ALL;
		}

		final Cursor c;

		final List<String> selection = new ArrayList<String>();
		final List<String> selArgs = new ArrayList<String>();

		if (filter.type == FilterType.SEARCH) {
			final String[] searchCols = new String[] {
					TABLE_ISSUES + "." + KEY_DESCRIPTION + " LIKE '%' || ? || '%'",
					TABLE_ISSUES + "." + KEY_SUBJECT + " LIKE '%' || ? || '%'",
					TABLE_ISSUES + "." + KEY_ID + " LIKE '%' || ? || '%'",
			};
			selection.add(Util.join(searchCols, " OR "));
			for (@SuppressWarnings("unused")
			final String searchCol : searchCols) {
				selArgs.add(filter.searchQuery);
			}
		}

		final String orderBy = getOrderBy(columns, columnsOrder);

		if (columns == null) {
			switch (filter.type) {
			default:
			case ALL:
				break;

			case PROJECT:
				if (filter.serverId > 0) {
					selection.add(KEY_SERVER_ID + " = " + filter.serverId);
				}
				if (filter.id > 0) {
					selection.add(KEY_PROJECT_ID + " = " + filter.id);
				}
				break;

			case VERSION:
				if (filter.serverId > 0) {
					selection.add(KEY_SERVER_ID + " = " + filter.serverId);
				}
				if (filter.id > 0) {
					selection.add(KEY_FIXED_VERSION_ID + " = " + filter.id);
				}
				break;

			case QUERY:
				throw new IllegalArgumentException("Can't call Filter.QUERY on the DbAdapter");
			}

			final String sel = selection.size() > 0 ? Util.join(selection.toArray(), " AND ") : null;

			String[] selA;

			if (selArgs == null || selArgs.size() <= 0) {
				selA = null;
			} else {
				selA = new String[selArgs.size()];
				selArgs.toArray(selA);
			}

			c = mDb.query(TABLE_ISSUES, columns, sel, selA, null, null, orderBy);
		} else {
			if (filter.serverId > 0) {
				selection.add(TABLE_ISSUES + "." + KEY_SERVER_ID + " = " + filter.serverId);
			}
			if (filter.type == FilterType.PROJECT && filter.id > 0) {
				selection.add(TABLE_ISSUES + "." + KEY_PROJECT_ID + " = " + filter.id);
			}
			if (filter.type == FilterType.VERSION && filter.id > 0) {
				selection.add(TABLE_ISSUES + "." + KEY_FIXED_VERSION_ID + " = " + filter.id);
			}

			c = findMatchingIssues(selection, selArgs, columns, orderBy);
		}

		c.moveToFirst();
		return c;
	}

	/**
	 * Select all the issues that match the SQL <code>WHERE</code> clause expressed in <code>selection</code><br />
	 * Arguments placed in <code>selection</code> without ?'s must be properly escaped, because the Cursor will be created using
	 * {@link SQLiteDatabase#rawQuery(String, String[])}.<br />
	 * Arguments placed in <code>selection</code> with ?'s can place their arguments in <code>selectionArgs</code>.
	 *
	 * @param selection
	 * @param columns
	 * @return
	 */
	private Cursor findMatchingIssues(final List<String> selection, final List<String> selectionArgs, final String[] columns, final String orderBy) {
		final List<String> tables = new ArrayList<String>();
		final List<String> cols = new ArrayList<String>();
		final List<String> onArgs = new ArrayList<String>();

		tables.add(TABLE_ISSUES);
		// Handle fake columns
		for (final String col : columns) {
			onArgs.clear();
			if (KEY_STATUS.equals(col)) {
				cols.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + Reference.KEY_NAME + " AS " + col);
				onArgs.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + IssueStatusesDbAdapter.KEY_ID + " = " + TABLE_ISSUES + "." + KEY_STATUS_ID);
				onArgs.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + IssueStatusesDbAdapter.KEY_SERVER_ID + " = " + TABLE_ISSUES + "." +
						KEY_SERVER_ID);
				tables.add("LEFT JOIN " + IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + " ON " + Util.join(onArgs.toArray(), " AND "));
			} else if (KEY_FIXED_VERSION.equals(col)) {
				cols.add(VersionsDbAdapter.TABLE_VERSIONS + "." + Reference.KEY_NAME + " AS " + col);
				onArgs.add(VersionsDbAdapter.TABLE_VERSIONS + "." + VersionsDbAdapter.KEY_ID + " = " + TABLE_ISSUES + "." + KEY_FIXED_VERSION_ID);
				onArgs.add(VersionsDbAdapter.TABLE_VERSIONS + "." + VersionsDbAdapter.KEY_PROJECT_ID + " = " + TABLE_ISSUES + "." + KEY_PROJECT_ID);
				onArgs.add(VersionsDbAdapter.TABLE_VERSIONS + "." + VersionsDbAdapter.KEY_SERVER_ID + " = " + TABLE_ISSUES + "." + KEY_SERVER_ID);
				tables.add("LEFT JOIN " + VersionsDbAdapter.TABLE_VERSIONS + " ON " + Util.join(onArgs.toArray(), " AND "));
			} else if (KEY_PROJECT.equals(col)) {
				cols.add(ProjectsDbAdapter.TABLE_PROJECTS + "." + Reference.KEY_NAME + " AS " + col);
				onArgs.add(ProjectsDbAdapter.TABLE_PROJECTS + "." + ProjectsDbAdapter.KEY_ID + " = " + TABLE_ISSUES + "." + KEY_PROJECT_ID);
				onArgs.add(VersionsDbAdapter.TABLE_VERSIONS + "." + ProjectsDbAdapter.KEY_SERVER_ID + " = " + TABLE_ISSUES + "." + KEY_SERVER_ID);
				tables.add("LEFT JOIN " + ProjectsDbAdapter.TABLE_PROJECTS + " ON " + Util.join(onArgs.toArray(), " AND "));
			} else if (IssueStatusesDbAdapter.KEY_IS_CLOSED.equals(col)) {
				cols.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + col);
				onArgs.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + IssueStatusesDbAdapter.KEY_ID + " = " + TABLE_ISSUES + "." + KEY_STATUS_ID);
				onArgs.add(IssueStatusesDbAdapter.TABLE_ISSUE_STATUSES + "." + IssueStatusesDbAdapter.KEY_SERVER_ID + " = " + TABLE_ISSUES + "." +
						KEY_SERVER_ID);
			} else {
				cols.add(TABLE_ISSUES + "." + col);
			}
		}

		final String where = selection.size() > 0 ? " WHERE " + Util.join(selection.toArray(), " AND ") : "";
		String sql = "SELECT " + Util.join(cols.toArray(), ", ") + " FROM " + Util.join(tables.toArray(), " ") + where;
		String[] selA;

		if (selectionArgs == null || selectionArgs.size() <= 0) {
			selA = null;
		} else {
			selA = new String[selectionArgs.size()];
			selectionArgs.toArray(selA);
		}

		if (!TextUtils.isEmpty(orderBy)) {
			sql += " ORDER BY " + orderBy;
		}

		return mDb.rawQuery(sql, selA);
	}

	public void saveAll(final Server server, final long projectId, final List<Issue> issues) {
		mDb.beginTransaction();
		try {
			deleteAll(server, projectId);

			for (final Issue issue : issues) {
				insert(issue);
			}

			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * Removes issues
	 *
	 * @return
	 */
	public int deleteAll(final Server server, final long projectId) {
		if (projectId > 0) {
			return mDb.delete(TABLE_ISSUES, KEY_SERVER_ID + " = " + server.rowId + " AND " + KEY_PROJECT_ID + "= " + projectId, null);
		} else {
			return mDb.delete(TABLE_ISSUES, KEY_SERVER_ID + " = " + server.rowId, null);
		}
	}

	public String getName(final Server server, final long issueId) {
		final Issue i = select(server, issueId, new String[] {
				KEY_SUBJECT
		});
		return i == null ? null : i.subject;
	}

	public int countIssues(final Server server, final User user) {
		final String sql = "SELECT COUNT(*) FROM " + TABLE_ISSUES + " WHERE " + KEY_SERVER_ID + " = " + server.rowId + " AND " + KEY_ASSIGNED_TO_ID + " = " +
				user.id;
		final Cursor c = mDb.rawQuery(sql, null);
		int nb = 0;
		if (c != null) {
			if (c.moveToFirst()) {
				nb = c.getInt(0);
			}
			c.close();
		}
		return nb;
	}

	public int countAll() {
		final Cursor c = mDb.rawQuery("SELECT COUNT(*) FROM " + TABLE_ISSUES, null);
		int nb = 0;
		if (c != null) {
			if (c.moveToFirst()) {
				nb = c.getInt(0);
			}
			c.close();
		}
		return nb;
	}
}