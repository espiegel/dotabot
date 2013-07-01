import twitter4j.RateLimitStatus;

public class TwitterUtils {
    public static String rateLimitToString(RateLimitStatus limitStat) {
	return new StringBuilder()
	    .append("[API Remains: ")
	    .append(limitStat.getRemainingHits())
	    .append(" | Reset Date: ")
	    //.append(limitStat.getResetTime())
	    .append(limitStat.getResetTimeInSeconds())
	    .append("]")
	    .toString();
    }
    public static Throwable sleep(long msec) {
	Throwable t = null;
	try {
	    Thread.sleep(msec);
	} catch (InterruptedException e) {
	    t = e;
	}
	return t;
    }
}