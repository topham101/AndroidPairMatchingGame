package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static mobile.labs.acw.R.layout.activity_download_puzzle;

/**
 * Created by Matt on 25/03/2017.
 */

public class DownloadPuzzleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_download_puzzle);

        // Check network connection exists
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // If connection available
        if (activeNetworkInfo != null)
        {
            // Download Puzzles
            new downloadJSONPuzzles().execute("http://www.hull.ac.uk/php/349628/08027/acw/index.json");
        }
        else // If no connection available
        {
            // Update Interface
            TextView tView = (TextView) findViewById(R.id.downloadPuzzleTextView);
            tView.setText(R.string.DownloadPageNoConnection);
            Toast.makeText(this, getResources().getString(R.string.ConnetionFailureDownloading), Toast.LENGTH_SHORT).show();
        }
    }

    //region Async Methods
    private class downloadJSONPuzzles extends AsyncTask<String, String[], String[]>
    {
        // Downloads and returns a list of the JSON Puzzles
        protected String[] doInBackground(String... args)
        {
            String[] resultArray = new String[0];
            try
            {
                String result = "";

                // Download JSON
                InputStream stream = (InputStream) new URL(args[0]).getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while(line != null)
                {
                    result += line;
                    line = reader.readLine();
                }

                // return empty if cancelled early
                if(isCancelled())
                    return resultArray;

                // Parse JSON
                JSONObject json = new JSONObject(result);
                JSONArray puzzles = json.getJSONArray("PuzzleIndex");
                resultArray = new String[puzzles.length()];

                // Iterate through JSON objects
                for (int i = 0; i < puzzles.length(); ++i)
                {
                    String puzzleString = puzzles.getString(i);
                    if (puzzleString.endsWith(".json") && puzzleString.startsWith("puzzle"))
                    {
                        puzzleString = puzzleString.substring(0, puzzleString.length() - 5);
                        puzzleString = "Puzzle " + puzzleString.substring(6, puzzleString.length());
                    }
                    resultArray[i] = puzzleString;
                }

            }
            catch (Exception e) {}

            // Return results
            return resultArray;
        }

        // Disposes of puzzles that are already downloaded.
        // Places remaining puzzles in a list
        @Override
        protected void onPostExecute(String[] downloadResult) {
            // If some puzzles are downloaded
            if (downloadResult != null && downloadResult.length > 0)
            {
                // Updates Interface
                TextView tView = (TextView) findViewById(R.id.downloadPuzzleTextView);
                tView.setText(R.string.DownloadPageDownloaded);

                // Populate the ListView with puzzles to download
                ListView listView = (ListView) findViewById(R.id.OnlinePuzzleListView);
                ArrayList<String> newPuzzles = new ArrayList();
                PuzzleDBHelper dbHelper = new PuzzleDBHelper(DownloadPuzzleActivity.this);
                PuzzleRecord[] databasePuzzles = dbHelper.getAllDBPuzzles();
                boolean doesContain;
                for (int i = 0; i < downloadResult.length; i++)
                {
                    String puzzleNameStr = downloadResult[i].substring(7, downloadResult[i].length());
                    doesContain = false;
                    for (int j = 0; j < databasePuzzles.length; j++)
                    {
                        // If the database already contains this puzzle. Don't add it.
                        if (databasePuzzles[j].getName().equals(puzzleNameStr))
                        {
                            doesContain = true;
                            break;
                        }
                    }
                    if(!doesContain) // If the database doesn't contain this puzzle
                    {
                        newPuzzles.add(downloadResult[i]);
                    }
                }

                // Populate ListView with the downloaded results
                ArrayAdapter<String> adapter = new ArrayAdapter(DownloadPuzzleActivity.this, android.R.layout.simple_list_item_1, newPuzzles.toArray());
                listView.setAdapter(adapter);

                // If no puzzles are displayed then display Toast
                if(newPuzzles.size() == 0)
                    Toast.makeText(DownloadPuzzleActivity.this, getResources().getString(R.string.NoNewPuzzlesFound), Toast.LENGTH_SHORT).show();

                // Set ListView itemClick event to download the selected Puzzle and components
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(DownloadPuzzleActivity.this, getResources().getString(R.string.DownloadPuzzle), Toast.LENGTH_LONG).show();

                        // Change ListView onItemClick event to ensure that no more puzzles are downloaded at once.
                        ListView listView = (ListView) findViewById(R.id.OnlinePuzzleListView);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Toast.makeText(DownloadPuzzleActivity.this, getResources().getString(R.string.PleaseWait), Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Download selected puzzle
                        new downloadPuzzle(view.getContext()).execute((String)parent.getItemAtPosition(position));
                    }
                });
            }
            else // If no puzzles are found
            {
                // Update the interface accordingly
                Toast.makeText(DownloadPuzzleActivity.this, getResources().getString(R.string.NoPuzzlesFound), Toast.LENGTH_SHORT).show();
                TextView tView = (TextView) findViewById(R.id.downloadPuzzleTextView);
                tView.setText(R.string.DownloadPageEmpty);
            }
        }
    }

    private class downloadPuzzle extends AsyncTask<String, Void, Void>
    {
        private Context m_Context;

        public downloadPuzzle(Context pContext)
        {
            super();
            m_Context = pContext;
        }

        // calls the PuzzleDBHelper class to download the Puzzle and respective components including images
        // Inserts these new elements into the database
        @Override
        protected Void doInBackground(String... args)
        {
            try
            {
                new PuzzleDBHelper(m_Context).addNewPuzzle(args[0]);
            }
            catch (Exception e) {}
            return null;
        }

        // Once finished, returns the activity to the Select-Puzzle activity
        @Override
        protected void onPostExecute(Void v) {
            finish();
        }
    }
    //endregion
}
