import twitter4j.*;

import java.util.Iterator;

public class TwitterClient {

	public static final int MAX_RECEIVE_STATS = 10;

	private Twitter twitter;
	private IStatusListener listener;

	private Paging paging = new Paging(1, MAX_RECEIVE_STATS);

	public TwitterClient(Twitter twitter, IStatusListener listener) {
		this.twitter = twitter;
		this.listener = listener;
	}

	public void getTimeline(String name)
	{
		try {
			String[] splits = name.split("/");
			if (splits.length == 1)
				getTimeline(twitter.showUser(splits[0]));

			else if (splits.length == 2)
			{
				//Iterator<UserList> it = twitter.getUserLists(splits[0], -1)
				Iterator<UserList> it = twitter.getUserLists(splits[0])
						.iterator();
				while (it.hasNext())
				{
					TwitterUtils.sleep(500);
					UserList list = it.next();
					if (list.getSlug().equals(splits[1]))
						getTimeline(list);
					else if (!it.hasNext())
						throw new RuntimeException("list not found: " + name);
				}
			}
			else
			{
				throw new RuntimeException("can not understanding parameter: "
						+ name);
			}
		} catch (TwitterException e) {
			listener.onCaughtTwitterException(e);
		}
	}

	public void getTimeline(User user) {
		try {
			ResponseList<Status> res = twitter.getUserTimeline(user.getId(),
					paging);
			if (res.size() > 0) {
				paging.setSinceId(res.get(0).getId());
			}
			listener.onReceiveStatuses(res);
		} catch (TwitterException e) {
			listener.onCaughtTwitterException(e);
		}
	}

	public void getTimeline(UserList list) {
		try {
			ResponseList<Status> res = twitter.getUserListStatuses(
					list.getId(), paging);
			if (res.size() > 0) {
				paging.setSinceId(res.get(0).getId());
			}
			listener.onReceiveStatuses(res);
		} catch (TwitterException e) {
			listener.onCaughtTwitterException(e);
		}
	}
}