package com.example.rolf.sunshine;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstancesState) {
        super.onCreate(savedInstancesState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        FetchWeatherTask actualWeatherTask = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(
                    getString(R.string.pref_location_key),
                    getString(R.string.pref_location_default));
        actualWeatherTask.execute(location);
        String[] cityCountry = location.split(",");
        heading.setText(cityCountry[0]);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    String[] forecastArray = {"  Please wait, weather data beeing retrieved ... ",
            " ", " ", " ", " ", " ", " "};
    double[] tempArray = {0,0,0,0,0,0,0};  // to be filled with actual temperatures later - for Graph
    double[] precipArray = {1,0,0,2,8,9.5,3};
    String[] dayNameArray = {"a","b","c","4","5","6","7"};
    DataPoint[] tempPoint;
    DataPoint[] precipPoint;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();


    //ArrayList<DayWeather> dayWeatherList = new ArrayList<DayWeather>();

    ArrayAdapter<String> myForecastAdapter;
    GraphView diagr;
    LineGraphSeries<DataPoint> series;
    BarGraphSeries<DataPoint> series2;
    TextView heading;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        List<String> weekForecast = new ArrayList<String>( Arrays.asList(forecastArray) );

        myForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(myForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity().getApplicationContext(),"list id = " + id, Toast.LENGTH_SHORT).show();
                String forecast = myForecastAdapter.getItem(position);
                Intent launchDetailActivity = new Intent(getActivity().getApplicationContext(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                Bundle transitionBundle =
                        ActivityOptions.makeCustomAnimation(
                                getActivity().getApplicationContext(),
                                R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
                //startActivity(launchDetailActivity, transitionBundle); // geht nicht
                startActivity(launchDetailActivity);
                //overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);
                // geht auch nicht, weil diese MEthode in Fragment offenbar nicht zur Verfügung steht
                // den Befehl in MainActivity onCreate gesetzt - funktioniert aber auch nicht
            }
        });

        heading = (TextView) rootView.findViewById((R.id.textview_forecast_heading));

/* Old Graph with achartenginge
        BarGraph barGraph = new BarGraph(tempArray);
        GraphicalView graphicalView = barGraph.getView(getActivity().getApplicationContext());
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.bar_graph);
        layout.addView(graphicalView);
        graphicalView.setBackgroundColor(Color.RED);
        graphicalView.repaint();
*/
        // New graph with GraphView
        diagr = (GraphView) rootView.findViewById(R.id.diagr);
        tempPoint = new DataPoint[tempArray.length];
        precipPoint = new DataPoint[precipArray.length];
        for (int i = 0; i < tempArray.length; i++) {
            tempPoint[i] = new DataPoint( i, tempArray[i] );
            precipPoint[i] = new DataPoint( i, precipArray[i] );
        }
        series = new LineGraphSeries<>(tempPoint);
        series2 = new BarGraphSeries<>(precipPoint);
        //series.setBackgroundColor(Color.GREEN);
        //series.setDrawBackground(true);
        series.setColor(Color.rgb(250, 200, 0));
        series2.setColor(Color.argb(60, 0, 0, 255) );
        series.setThickness(8);

        //diagr.setBackgroundColor(Color.GREEN);
        diagr.getViewport().setMinY(-5);
        diagr.getViewport().setMaxY(5);
        diagr.getViewport().setMaxX(6);
        diagr.getViewport().setXAxisBoundsManual(true);
        diagr.getViewport().setYAxisBoundsManual(true);
        //diagr.getGridLabelRenderer().set bla bla
        StaticLabelsFormatter labels = new StaticLabelsFormatter(diagr);
        labels.setHorizontalLabels(dayNameArray);
        labels.setDynamicLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double v, boolean b) {
                return null;
            }
            @Override
            public void setViewport(Viewport viewport) {
            }
        });

        diagr.getSecondScale().addSeries(series2);
        //diagr.addSeries(series2);
        diagr.addSeries(series);
        diagr.getSecondScale().setMinY(0);
        diagr.getSecondScale().setMaxY(10);
        diagr.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.BLUE);
        diagr.getGridLabelRenderer().setVerticalLabelsColor(Color.rgb(250, 200, 0));

        Locale locale = Locale.GERMAN;
        Date today = new Date();
        System.out.println("Date format in "
                + locale.getDisplayName()
                + ": "
                + SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale)
                    .format(today).toUpperCase());

        return rootView;
    }

    private String formatTemperature(double t) {
        String tStr = "";
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());

        String unitType = sharedPrefs.getString(
                getString(R.string.pref_units_key),
                getString(R.string.pref_units_metric));
        if(unitType.equals(getString(R.string.pref_units_imperial))) {
            t = (t*1.8) + 32;
            tStr = Math.round(10*t)/10. + "°F";
        } else if (unitType.equals(getString(R.string.pref_units_metric))) {
            tStr = Math.round(10*t)/10. + "°C";
        } else Log.d(LOG_TAG, "Unit type not found: " + unitType);
        return tStr;
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String LANG_PARAM = "lang";
            final String FORMAT_PARAM = "mode";
            final String UNIT_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String lang = "sp";
            String format = "jason";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                                // set in zero position of params array, where there is the location
                        .appendQueryParameter(LANG_PARAM, lang)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNIT_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "--> Built URI: " + url);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                // rolf why a type cast here??
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ( (line = reader.readLine()) != null ) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG, "--> forecastJsonStr: " + forecastJsonStr);

                // Pass string data to JSON object
                JSONObject forecastJsonObject = new JSONObject(forecastJsonStr);

                // Build array; "list" is the keyword in forecastJsonStr where the array follows
                JSONArray allDaysJsonArray = forecastJsonObject.getJSONArray("list");

                Log.v(LOG_TAG,"--> allDaysJsonArray length = " + allDaysJsonArray.length() );

                Time dayTime = new Time();
                dayTime.setToNow();  // ?? what happens here and why, later a new object is created
                int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff); // ??
                dayTime = new Time();

                for(int i = 0; i < allDaysJsonArray.length(); i++) {
                    JSONObject jsonDayWeather = allDaysJsonArray.getJSONObject(i);
                    //DayWeather dayWeather = new DayWeather();  // DTO - data transfer object

                    JSONObject jsonTemperatureObject = jsonDayWeather.getJSONObject("temp");
                    String tempDay = jsonTemperatureObject.getString("day");
                    double tempDayDouble = Double.parseDouble(tempDay);
                    tempArray[i] = Math.round(tempDayDouble*10)/10.;
                    //long tempDayRounded = Math.round(tempDayDouble);

                    // the weather description is hidden in another array with keyword "weather"
                    // this array has only one JSONObject-field
                    JSONArray jsonDescriptionArray = jsonDayWeather.getJSONArray("weather");
                    String description = jsonDescriptionArray.getJSONObject(0).getString("description");

                    long dateTime = dayTime.setJulianDay(julianStartDay+i);
                    SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE dd. MMM", Locale.GERMAN);
                    String day = shortenedDateFormat.format(dateTime);
                    if ( i==0 ) day = "<b>Heute</b>,  ";
                    if ( i==1 ) day = "Morgen, ";

                    //dayWeather.temp = tempDay;
                    //dayWeather.description = description;
                    Log.v(LOG_TAG,"--> day " + i + ":  "  + description + ", " + formatTemperature(tempDayDouble));
                    //dayWeatherList.add(dayWeather);
                    forecastArray[i] = " " + day + ":  " + description + ", " + formatTemperature(tempDayDouble);
                    tempPoint[i] = new DataPoint( i, tempArray[i] );
                }
                return forecastArray;

            } catch (IOException e) {
                Log.e(LOG_TAG, "--> IO exception ", e);
                // If the code didn't successfully get the weather data,
                // there's no point in attempting to parse it.
                return null;

            } catch (JSONException e) {
                Log.e(LOG_TAG, "--> JSON exception ", e);

            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
        // the argument is automatically what the doInBackground method of AsynchTask returns
            if (result != null) {
                myForecastAdapter.clear();
                for (String day : result) {
                    // this is, what ultimately triggers the ListView zu update
                    myForecastAdapter.add(day);
                    //myForecastAdapter.add(Html.fromHtml(day));

                }
                series.resetData(tempPoint);
                diagr.getViewport().setMinY(Math.floor(series.getLowestValueY() - 2));
                diagr.getViewport().setMaxY(Math.ceil(series.getHighestValueY() + 2));
            }
            super.onPostExecute(result);
        }
    }
}

