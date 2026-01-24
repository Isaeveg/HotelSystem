package com.hotel.common;

import java.io.Serializable;
import java.util.List;

/**
 * Container for dashboard statistics and recent activity.
 */
public class DashboardData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reservationsToday;
    private double incomeMonth;
    private List<ActivityEntry> recentActivities;

    /**
     * Constructs a new DashboardData object.
     *
     * @param reservationsToday count of reservations made today
     * @param incomeMonth       total income for the current month
     * @param recentActivities  list of recent booking activities
     */
    public DashboardData(int reservationsToday, double incomeMonth, List<ActivityEntry> recentActivities) {
        this.reservationsToday = reservationsToday;
        this.incomeMonth = incomeMonth;
        this.recentActivities = recentActivities;
    }

    public int getReservationsToday() {
        return reservationsToday;
    }

    public double getIncomeMonth() {
        return incomeMonth;
    }

    public List<ActivityEntry> getRecentActivities() {
        return recentActivities;
    }

    /**
     * Represents a single activity entry (e.g., a booking event) for the dashboard.
     */
    public static class ActivityEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        private String time;
        private String description;
        private String status;

        public ActivityEntry(String time, String description, String status) {
            this.time = time;
            this.description = description;
            this.status = status;
        }

        public String getTime() {
            return time;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }
    }
}