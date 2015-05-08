package phantom.edltracker;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.jjoe64.graphview.*;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class Stats extends ActionBarActivity {

    private double[] gps_data;
    private int[] altitude;
    private int[] time;
    private ArrayList<Location> coords;
    float total_dist = 0;
    int total_time = 0;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        extras = getIntent().getExtras();
        gps_data = extras.getDoubleArray("gps coords");
        altitude = extras.getIntArray("altitude");
        time = extras.getIntArray("time data");
        coords = new ArrayList<Location>();

        processData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    public void processData() {
        //generate the coordinate arraylist
        for (int i = 0; i < gps_data.length/2; i+=2) {
            System.out.println("Adding gps point: " + gps_data[i+1] + ", " + gps_data[i]);
            Location new_point = new Location("");
            new_point.setLatitude(gps_data[i + 1]);
            new_point.setLongitude(gps_data[i]);
            new_point.setAltitude((double)altitude[i]);

            coords.add(new_point);
        }
        System.out.println("Total gps points: " + coords.size());

        //make graph
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        total_dist = 0;
        for (int i = 0; i < coords.size()-1; i++) {
            total_dist = total_dist + coords.get(i).distanceTo(coords.get(i+1));
            System.out.println("Calculating segment distance: " + coords.get(i).distanceTo(coords.get(i+1)));
            series.appendData(new DataPoint(total_dist,coords.get(i).getAltitude()), false,coords.size());
        }
        graph.addSeries(series);

        //add average speed
        final TextView spdView = (TextView) findViewById(R.id.speed);
        total_time = time[0];
        for (int i = 0; i < time.length-1; i++) {
            total_time = total_time + (time[i+1] - time[i]);
        }

        double avgSpeed = 2.23694 * total_dist / (total_time / 1000);
        spdView.setText((double)Math.round(avgSpeed * 100)/100 + " MPH");

        //add time
        int x = total_time / 1000;
        int seconds = x % 60;
        x /= 60;
        int minutes = x % 60;
        x /= 60;
        int hours = x % 24;

        final TextView timeView = (TextView) findViewById(R.id.time);
        timeView.setText(hours + "h " + minutes + "m " + seconds + "s");

        //add mileage
        final TextView distView = (TextView) findViewById(R.id.distance);
        distView.setText((double)Math.round(0.0621371 * total_dist)/100 + " miles");

        //add caloric output
        final TextView calView = (TextView) findViewById(R.id.kcal);
        int calBurnRate = 52;
        double multiplier = avgSpeed / 3;
        calBurnRate = (int)(calBurnRate * multiplier);

        calView.setText((double)Math.round(calBurnRate*(0.00621371 * total_dist))/10 + " calories");
    }
}
