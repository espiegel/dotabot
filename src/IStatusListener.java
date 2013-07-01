import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public interface IStatusListener {
    void onReceiveStatuses(ResponseList<Status> list);
    void onCaughtTwitterException(TwitterException e);
}