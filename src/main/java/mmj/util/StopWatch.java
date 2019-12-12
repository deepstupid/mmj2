//*****************************************************************************/
//* Copyright (C) 2013                                                        */
//* ALEXEY MERKULOV  steelart (dot) alex (at) gmail (dot) com                 */
//* License terms: GNU General Public License Version 2                       */
//*                or any later version                                       */
//*****************************************************************************/
package mmj.util;

/**
 * This is a very simple stop-watch class.
 */
public class StopWatch {
    private long startTime;
    private long stopTime;
    private boolean running;

    public StopWatch() {}

    public StopWatch(final boolean start) {
        if (start)
            start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
        running = false;
    }

    public long getElapsedMilliseconds() {
        long elapsed;
        elapsed = running ? System.currentTimeMillis() - startTime : stopTime - startTime;
        return elapsed;
    }

    public long getElapsedTimeSeconds() {
        return getElapsedMilliseconds() / 1000;
    }

    public String getElapsedTimeInStr() {
        final double time = (double)getElapsedMilliseconds() / 1000;
        return Double.toString(time) + "s";
    }

    @Override
    public String toString() {
        return "(" + startTime + ":" + stopTime + ")=" + getElapsedTimeInStr();
    }

    public static int compare(final StopWatch x, final StopWatch y) {
        return Long.compare(x.getElapsedMilliseconds(),
            y.getElapsedMilliseconds());
    }
}
