package net.bicou.redmine.app.roadmap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;

import net.bicou.redmine.Constants;
import net.bicou.redmine.R;
import net.bicou.redmine.app.AsyncTaskFragment;
import net.bicou.redmine.app.ProjectsSpinnerAdapter;
import net.bicou.redmine.app.RefreshProjectsTask;
import net.bicou.redmine.app.issues.order.IssuesOrder;
import net.bicou.redmine.app.issues.order.IssuesOrderingFragment;
import net.bicou.redmine.app.misc.EmptyFragment;
import net.bicou.redmine.data.json.Issue;
import net.bicou.redmine.data.json.Project;
import net.bicou.redmine.data.json.Version;
import net.bicou.redmine.data.sqlite.IssuesDbAdapter;
import net.bicou.redmine.data.sqlite.ServersDbAdapter;
import net.bicou.redmine.util.L;
import net.bicou.splitactivity.SplitActivity;

import java.util.ArrayList;
import java.util.List;

public class RoadmapActivity extends SplitActivity<RoadmapsListFragment, RoadmapFragment> implements RoadmapsListFragment.RoadmapSelectionListener, ActionBar.OnNavigationListener, RoadmapsListFragment.CurrentProjectInfo, AsyncTaskFragment.TaskFragmentCallbacks {
	public static final int ACTION_LOAD_ROADMAP = 0;
	public static final int ACTION_ISSUE_TOGGLE_FAVORITE = 1;

	@Override
	public void onRoadmapSelected(Version version) {
		L.d("");
		Bundle args = new Bundle();
		args.putString(RoadmapFragment.KEY_VERSION_JSON, new Gson().toJson(version));
		selectContent(args);
		supportInvalidateOptionsMenu();
	}

	@Override
	protected RoadmapsListFragment createMainFragment(Bundle args) {
		return RoadmapsListFragment.newInstance(args);
	}

	@Override
	protected RoadmapFragment createContentFragment(Bundle args) {
		return RoadmapFragment.newInstance(args);
	}

	@Override
	protected Fragment createEmptyFragment(Bundle args) {
		return EmptyFragment.newInstance(R.drawable.roadmaps_empty_fragment);
	}

	@Override
	protected void onPreCreate() {
		supportRequestWindowFeature(Window.FEATURE_PROGRESS);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(false);
		AsyncTaskFragment.attachAsyncTaskFragment(this);
		initProjectsSpinner(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Show the "sort issues" item only if the issues list is displayed
		MenuItem sortIssues = menu.findItem(R.id.menu_roadmap_sort_issues);
		if (sortIssues != null) {
			sortIssues.setVisible(getContentFragment() != null);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		L.d("");
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;

		case R.id.menu_roadmap_sort_issues:
			final RoadmapFragment rf = getContentFragment();
			if (rf == null) {
				supportInvalidateOptionsMenu();
			} else {
				// Display the issues ordering popup
				final IssuesOrderingFragment issuesOrder = IssuesOrderingFragment.newInstance(rf.getCurrentOrder());
				issuesOrder.setOrderSelectionListener(new IssuesOrderingFragment.IssuesOrderSelectionListener() {
					@Override
					public void onOrderColumnsSelected(final IssuesOrder orderColumns) {
						// Upon selection, update the roadmap fragment issues ordering
						rf.setNewIssuesOrder(orderColumns);
					}
				});
				issuesOrder.show(getSupportFragmentManager(), "issues_order");
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}


	@Override
	public void onResume() {
		super.onResume();
		refreshProjectsList();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		L.d("");
		outState.putParcelableArrayList(KEY_REDMINE_PROJECTS_LIST, mProjects);
		outState.putInt(Constants.KEY_PROJECT_POSITION, mCurrentProjectPosition);
	}

	//-----------------------------------------------------------
	// Projects spinner

	public static final String KEY_REDMINE_PROJECTS_LIST = "net.bicou.mymine.RedmineProjectsList";

	protected ArrayList<Project> mProjects;
	protected ArrayAdapter<Project> mAdapter;
	public int mCurrentProjectPosition;

	private void initProjectsSpinner(Bundle savedInstanceState) {
		mCurrentProjectPosition = -1;
		if (savedInstanceState == null) {
			mProjects = new ArrayList<Project>();
			mAdapter = new ProjectsSpinnerAdapter(this, mProjects);
			mCurrentProjectPosition = -1;
		}

		// Specific project/server?
		final Bundle args = getIntent().getExtras();
		if (args != null && args.containsKey(Constants.KEY_PROJECT_POSITION)) {
			mCurrentProjectPosition = args.getInt(Constants.KEY_PROJECT_POSITION);
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		mProjects = savedInstanceState.getParcelableArrayList(KEY_REDMINE_PROJECTS_LIST);
		mAdapter = new ProjectsSpinnerAdapter(this, mProjects);
		mCurrentProjectPosition = savedInstanceState.getInt(Constants.KEY_PROJECT_POSITION);

		enableListNavigationMode();
	}

	private void enableListNavigationMode() {
		L.d("current proj pos=" + mCurrentProjectPosition);
		final ActionBar ab = getSupportActionBar();
		ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ab.setListNavigationCallbacks(mAdapter, this);
		if (mCurrentProjectPosition >= 0) {
			ab.setSelectedNavigationItem(mCurrentProjectPosition);
		}
	}

	public void refreshProjectsList() {
		if (mProjects.size() > 0) {
			return;
		}

		new RefreshProjectsTask(RoadmapActivity.this, new RefreshProjectsTask.ProjectsLoadCallbacks() {
			@Override
			public void onProjectsLoaded(List<Project> projectList) {
				mProjects.addAll(projectList);
				if (getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_LIST) {
					enableListNavigationMode();
				}
			}
		}).execute();
	}

	@Override
	public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
		L.d("position: " + itemPosition);
		if (mProjects == null || itemPosition < 0 || itemPosition > mProjects.size()) {
			return true;
		}

		mCurrentProjectPosition = itemPosition;

		RoadmapsListFragment list = getMainFragment();
		if (list == null) {
			showMainFragment(new Bundle());
		} else {
			list.updateRoadmap();
		}

		return true;
	}

	@Override
	public Project getCurrentProject() {
		if (mProjects == null || mCurrentProjectPosition < 0 || mCurrentProjectPosition >= mProjects.size()) {
			return null;
		}
		return mProjects.get(mCurrentProjectPosition);
	}

	@Override
	public void onPreExecute(final int action, final Object parameters) {
		setSupportProgressBarIndeterminateVisibility(true);
	}

	@Override
	public Object doInBackGround(Context applicationContext, final int action, final Object parameters) {
		switch (action) {
		case ACTION_LOAD_ROADMAP:
			Project project = getCurrentProject();
			if (project != null) {
				return RoadmapsListFragment.getRoadmap(this, getCurrentProject().server, getCurrentProject());
			}
			break;

		case ACTION_ISSUE_TOGGLE_FAVORITE:
			IssuesDbAdapter idb = new IssuesDbAdapter(applicationContext);
			idb.open();
			ServersDbAdapter sdb = new ServersDbAdapter(idb);
			Bundle args = (Bundle) parameters;
			final long serverId = args.getLong(Constants.KEY_SERVER_ID);
			final long issueId = args.getLong(Constants.KEY_ISSUE_ID);
			Issue issue = idb.select(sdb.getServer(serverId), issueId, null);
			issue.is_favorite = args.getBoolean(IssuesDbAdapter.KEY_IS_FAVORITE);
			idb.update(issue);
			idb.close();
			break;
		}
		return null;
	}

	@Override
	public void onPostExecute(final int action, final Object parameters, final Object result) {
		setSupportProgressBarIndeterminateVisibility(false);
		switch (action) {
		case ACTION_LOAD_ROADMAP:
			RoadmapsListFragment list = getMainFragment();
			if (list != null) {
				list.onRoadmapLoaded((List<Version>) result);
			}
			break;
		}
	}
}
