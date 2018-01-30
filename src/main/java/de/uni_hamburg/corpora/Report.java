/**
 * @file StatisticsReport.java
 *
 * Auxiliary data structure for user friendly validation reports. Bit like a
 * logger.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * Statistics report is a container class to facilitate building reports for
 * different validators and other file processors. The statistics consist of
 * "messages" that are singular events of success, failure or other notes,
 * categorised in named buckets. It's quite generic, the main point is to create
 * reports like:
 *
 * <pre>
 *   File "xyz.xml" has:
 *      1567 of things and stuff: 95 % done correctly,
 *      1 % missing, and 4 % with errors (see details here: ___)
 *      12400 of annotations: 100 % done correctly, 7 % with warnings.
 * </pre>
 */
public class Report {
    
    
    //Anne: Is this the ErrorList in the UML? If so, should we rename here or use StatisticsReport in UML? Or maybe best: ErrorReport?
    //But what would be the Items in here? ReportItems? Errors? StatisticsStuff?

    /** Special statistics counter for higher level exceptions.
     * I use this to produce an error count with no possible successes, a bit
     * like root logger in logging
     */
    public final String ROOT_BUCKET = "root";

    /** the data structure holding all statistics. */
    private Map<String, Collection<ReportItem>> statistics;

    /**
     * convenience function to create new statistic set if missing or get old.
     */
    private Collection<ReportItem> getOrCreateStatistic(String statId) {
        if (!statistics.containsKey(statId)) {
            statistics.put(statId, new ArrayList<ReportItem>());
        }
        return statistics.get(statId);
    }

    /**
     * Create empty report.
     */
    public Report() {
        statistics = new HashMap<String, Collection<ReportItem>>();
    }

    /**
     * Merge two error reports. Efficiently adds statistics from other report
     * to this one.
     */
    public void merge(Report sr) {
        for (Map.Entry<String, Collection<ReportItem>> kv :
                sr.statistics.entrySet()) {
            if (statistics.containsKey(kv.getKey())) {
                Collection<ReportItem> c =
                    statistics.get(kv.getKey());
                c.addAll(kv.getValue());
                statistics.put(kv.getKey(), c);
            } else {
                statistics.put(kv.getKey(), kv.getValue());
            }
        }
    }

    /**
     * Add a critical error in the root log.
     *
     * @sa addCritical(String, String)
     */
    public void addCritical(String description) {
        addCritical(ROOT_BUCKET, description);
    }

    /**
     * Add a critical error in named statistics bucket.
     */
    public void addCritical(String statId, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.CRITICAL,
                    description));
    }

    /**
     * Add a critical error in named statistics bucket.
     * @todo extrablah
     */
    public void addCritical(String statId, String description, String extraBlah) {
        addCritical(statId, description + extraBlah);
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addCritical(String statId, Throwable e, String description, String
            extrablah) {
        addCritical(statId, description + "::" + extrablah +
                "..." + e.getStackTrace()[0]);
    }
    /**
     * Add a critical error in named statistics bucket.
     * @todo extrablah
     */
    public void addCritical(String statId, Throwable e, String description) {
        addCritical(statId, description + e.getStackTrace()[0]);
    }


    /**
     * Add a non-critical error in named statistics bucket.
     */
    public void addWarning(String statId, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.WARNING,
                    description));
    }

    /**
     * Add a non-critical error in named statistics bucket.
     * @todo extrablah
     */
    public void addWarning(String statId, String description, String extraBlah) {
        addWarning(statId, description + extraBlah);
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addWarning(String statId, Throwable e, String description, String
            extrablah) {
        addWarning(statId, description + "::" + extrablah +
                "..." + e.getStackTrace()[0]);
    }
    /**
     * Add error about missing data in named statistics bucket.
     */
    public void addMissing(String statId, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.MISSING,
                    description));
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addCorrect(String statId, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.CORRECT,
                    description));
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addNote(String statId, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.NOTE,
                    description));
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addNote(String statId, Throwable e, String description) {
        addNote(statId, description + "..." + e.getStackTrace()[0]);
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addNote(String statId, Throwable e, String description, String
            extrablah) {
        addNote(statId, description + "::" + extrablah +
                "..." + e.getStackTrace()[0]);
    }
    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addNote(String statId, String description, String extraBlah) {
        addNote(statId, description + "::" + extraBlah);
    }


    /**
     * Add error with throwable to root log.
     *
     * @sa addException(String, Throwable, String)
     */
    public void addException(Throwable e, String description) {
        addException(ROOT_BUCKET, e, description);
    }

    /**
     * Add error with throwable in statistics bucket. The exception provides
     * extra information about the error, ideally e.g. when parsing a file if
     * error comes in form of exception is a good idea to re-use the throwable
     * in statistics.
     */
    public void addException(String statId, Throwable e, String description) {
        Collection<ReportItem> stat = getOrCreateStatistic(statId);
        stat.add(new ReportItem(ReportItem.Severity.CRITICAL,
                    e, description));
    }


    /**
     * Add error with throwable in statistics bucket. The exception provides
     */
    public void addException(String statId, Throwable e, String description,
            String extrablah) {
        addException(statId, e, description + "\n\t" + extrablah);
    }

    /**
     * Generate a one-line text-only message summarising the named bucket.
     */
    public String getSummaryLine(String statId) {
        int good = 0;
        int bad = 0;
        int unk = 0;
        Collection<ReportItem> stats = statistics.get(statId);
        for (ReportItem s : stats) {
            if (s.isBad()) {
                bad += 1;
            } else if (s.isGood()) {
                good += 1;
            } else {
                unk += 1;
            }
        }
        return MessageFormat.format("  {0}: {1} %: {2} OK, {3} bad, " +
                " and {4} unknown. " +
                "= {5} items.\n", statId, 100 * good / (good + bad + unk),
                good, bad, unk, good + bad + unk);
    }

    /**
     * Genereate summaries for all buckets.
     */
    public String getSummaryLines() {
        String rv = "";
        for (Map.Entry<String, Collection<ReportItem>> kv :
                statistics.entrySet()) {
            rv += getSummaryLine(kv.getKey());
        }
        return rv;
    }

    /**
     * Generate error report for given bucket. Includes only severe errors and
     * problems in detail.
     */
    public String getErrorReport(String statId) {
        Collection<ReportItem> stats = statistics.get(statId);
        String rv = MessageFormat.format("{0}:\n", statId);
        int suppressed = 0;
        for (ReportItem s : stats) {
            if (s.isSevere()) {
                rv += s.getSummary() + "\n";
            } else {
                suppressed += 1;
            }
        }
        if (suppressed != 0) {
            rv += MessageFormat.format("{0} warnings and notes hidden\n",
                    suppressed);
        }
        return rv;
    }

    /**
     * Generate error reports for all buckets.
     */
    public String getErrorReports() {
        String rv= "Errors:\n";
        for (Map.Entry<String, Collection<ReportItem>> kv :
                statistics.entrySet()) {
            rv += getErrorReport(kv.getKey());
        }
        return rv;
    }

    /**
     * Generate verbose report for given bucket.
     */
    public String getFullReport(String statId) {
        Collection<ReportItem> stats = statistics.get(statId);
        String rv = MessageFormat.format("{0}:\n", statId);
        for (ReportItem s : stats) {
            if (s.isGood()) {
                rv += s.toString() + "\n";
            }
        }
        for (ReportItem s : stats) {
            if (s.isBad()) {
                rv += s.toString() + "\n";
            }
        }
        return rv;
    }

    /**
     * Generate verbose reports for all buckets.
     */
    public String getFullReports() {
        String rv = "All reports\n";
        for (Map.Entry<String, Collection<ReportItem>> kv :
                statistics.entrySet()) {
            rv += getFullReport(kv.getKey());
        }
        return rv;
    }

    /**
     * Get single collection of statistics.
     */
    public Collection<ReportItem> getRawStatistics() {
        Collection<ReportItem> allStats = new ArrayList<ReportItem>();
        for (Map.Entry<String, Collection<ReportItem>> kv :
                statistics.entrySet()) {
            allStats.addAll(kv.getValue());
        }
        return allStats;
    }
}
