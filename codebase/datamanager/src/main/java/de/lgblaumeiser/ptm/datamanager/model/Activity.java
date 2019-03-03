/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.util.Objects.hash;

/**
 * Data structure for representation of an activity. An activity represents a
 * booking number under which work is done. Each work booking is assigned to an
 * activity and there are reports on hours spent on activities.
 */
public class Activity {
	public static class ActivityBuilder {
		private Long id = valueOf(-1);
		private String user;
		private String activityName;
		private String projectId;
		private String projectActivity;
		private boolean hidden = false;

		private ActivityBuilder(final Activity activity) {
			id = activity.getId();
			user = activity.getUser();
			activityName = activity.getActivityName();
			projectId = activity.getProjectId();
			projectActivity = activity.getProjectActivity();
			hidden = activity.isHidden();
		}

		private ActivityBuilder() {
			// Nothing to do
		}

		public ActivityBuilder setUser(final String user) {
			this.user = user;
			return this;
		}

		public ActivityBuilder setActivityName(final String activityName) {
			this.activityName = activityName;
			return this;
		}

		public ActivityBuilder setProjectId(final String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ActivityBuilder setProjectActivity(final String projectActivity) {
			this.projectActivity = projectActivity;
			return this;
		}

		public ActivityBuilder setHidden(final boolean hidden) {
			this.hidden = hidden;
			return this;
		}

		/**
		 * @return An unmodifiable activity representing the data given to the builder,
		 *         Non null, returns with exception if the data is invalid
		 */
		public Activity build() {
			checkData();
			return new Activity(id, user, activityName, projectId, projectActivity, hidden);
		}

		private void checkData() {
			assertState(stringHasContent(user));
			assertState(stringHasContent(activityName));
			assertState(stringHasContent(projectId));
			assertState(stringHasContent(projectActivity));
		}
	}

	/**
	 * Creates a new activity builder with no data set.
	 *
	 * @return A new activity builder, never null
	 */
	public static ActivityBuilder newActivity() {
		return new ActivityBuilder();
	}

	/**
	 * Change an existing activity by providing a builder preset with the activity
	 * data
	 *
	 * @return A new activity builder, never null
	 */
	public ActivityBuilder changeActivity() {
		return new ActivityBuilder(this);
	}

	private String activityName;
	private String projectActivity;
	private String projectId;
	private boolean hidden = false;
	private String user;
	private Long id = valueOf(-1);

	/**
	 * @return The internal id of the activity. Automatically created by storage
	 *         system
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return Name of the activity. Non null
	 */
	public String getActivityName() {
		return activityName;
	}

	/**
	 * @return Project id of the activities type. Non null
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @return Project sub category of the activities type. Non null
	 */
	public String getProjectActivity() {
		return projectActivity;
	}

	/**
	 * @return true, if activity is hidden
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return User owning the activity
	 */
	public String getUser() {
		return user;
	}

	@Override
	public String toString() {
		return format("Activity: Project Id: %s, Project Category: %s, Name: %s, Hidden: %b, User: %s, Id: %d",
				projectId, projectActivity, activityName, hidden, user, id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Activity) {
			Activity act = (Activity) obj;
			return id == act.id && hidden == act.isHidden() && activityName.equals(act.activityName)
					&& projectId.equals(act.projectId) && projectActivity.equals(act.projectActivity)
					&& user.equals(act.user);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash(id, activityName, projectId, projectActivity, user);
	}

	private Activity(final Long id, final String user, final String activityName, final String projectId,
			final String projectActivity, final boolean hidden) {
		this.id = id;
		this.activityName = activityName;
		this.projectId = projectId;
		this.projectActivity = projectActivity;
		this.hidden = hidden;
		this.user = user;
	}

	private Activity() {
		// Only needed for deserialization
	}
}
