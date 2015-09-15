package barqsoft.footballscores.widget;

/**
 * Created by FM on 9/10/2015.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.Data.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] Match_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.MATCH_DAY,
    };
    // these indices must match the projection
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 5;
    public static final int COL_AWAY_GOALS = 6;
    public static final int COL_DATE = 1;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCHDAY = 8;
    public static final int COL_ID = 8;
    public static final int COL_MATCHTIME = 2;
    public double detail_match_id = 7;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                //data = getContentResolver().query(weatherForLocationUri,
                data = getContentResolver().query(         //SQL Lite : SQL
                        DatabaseContract.BASE_CONTENT_URI, //Uri : FROM
                        Match_COLUMNS,                     //projection : col,col,col
                        null,                              //selection : Where
                        null,                              //selectionArgs
                        //WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");//sortOrder: ORDER BY col,col,...
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            /**/

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);


                //get HOME team
                String homeTeam=data.getString(COL_HOME);
                String awayTeam=data.getString(COL_AWAY);
                String homeGoal=data.getString(COL_HOME_GOALS);
                String awayGoal=data.getString(COL_AWAY_GOALS);
                String date=data.getString(COL_DATE);

                String goal=Utilies.getScores(Integer.parseInt(homeGoal),Integer.parseInt(awayGoal));
                Log.d(LOG_TAG,date);
                //Log.d(LOG_TAG,awayGoal);
                Log.d(LOG_TAG,goal);


                views.setTextViewText(R.id.home_team, homeTeam);
                views.setTextViewText(R.id.awawy_team, awayTeam);
                views.setTextViewText(R.id.score_textview, goal);
                views.setTextViewText(R.id.data_textview, date);
                views.setImageViewResource(R.id.widget_icon_home, Utilies.getTeamCrestByTeamName(homeTeam));
                views.setImageViewResource(R.id.widget_icon_away,Utilies.getTeamCrestByTeamName(awayTeam));

                String description = homeTeam +"versus"+awayTeam+goal;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }
                final Intent fillInIntent = new Intent();
                Uri matchUri = DatabaseContract.scores_table.buildScoreWithDate();
                fillInIntent.setData(matchUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon_home, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
