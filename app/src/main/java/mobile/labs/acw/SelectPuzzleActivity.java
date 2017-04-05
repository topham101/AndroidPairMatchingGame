package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Matt on 25/03/2017.
 */

public class SelectPuzzleActivity extends AppCompatActivity {

    //region Members
    private int m_lowerBound = 12, m_upperBound = 36;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_puzzle);

        // Set on Click Event
        ListView listView = (ListView) findViewById(R.id.PuzzleListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Create return Intent
                Intent data = new Intent();
                // Get Puzzle Name
                String text = parent.getItemAtPosition(position).toString();
                int loc = text.indexOf(" ");
                String puzzleName = text.substring(0, loc);
                // Add puzzle name to Intent
                data.putExtra("Puzzle_Name", puzzleName);
                setResult(RESULT_OK, data);
                // Return to Main Menu
                finish();
            }
        });

        // Populate the Filter
        Spinner spinner = (Spinner) findViewById(R.id.selectPuzzleSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // If an item is selected then filter results accordingly
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //String selected = getResources().getStringArray(R.array.PuzzleFilters)[position];
                switch (position) {

                    case 0: {
                        m_lowerBound = 0;
                        m_upperBound = 36;
                        break;
                    }
                    case 1: {
                        m_lowerBound = 12;
                        m_upperBound = 15;
                        break;
                    }
                    case 2: {
                        m_lowerBound = 16;
                        m_upperBound = 23;
                        break;
                    }
                    case 3: {
                        m_lowerBound = 24;
                        m_upperBound = 99;
                        break;
                    }
                }
                populateListView();
            }

            // If nothing is selected then show all results
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                m_lowerBound = 12;
                m_upperBound = 36;
                populateListView();
            }

        });
    }

    // Retrieve all puzzles in Database and load their names into the ListView
    public void populateListView()
    {
        ListView listView = (ListView)findViewById(R.id.PuzzleListView);
        PuzzleRecord[] allPuzzles = new PuzzleDBHelper(this).getAllDBPuzzles();
        ArrayList<String> filteredPuzzles = new ArrayList();
        ArrayList<String> filteredPuzzleHighScores = new ArrayList();

        for (int i = 0; i < allPuzzles.length; i++)
        {
            int size = allPuzzles[i].getLayout().length;
            if (size >= m_lowerBound && size <= m_upperBound) {
                filteredPuzzles.add(allPuzzles[i].getName());
                filteredPuzzleHighScores.add(allPuzzles[i].getHighScore());
            }
        }

        String[] puzzleNameArray = new String[filteredPuzzles.size()];
        String[] highScores_Array = new String[filteredPuzzles.size()];
        for (int i = 0; i < filteredPuzzles.size(); i++)
        {
            puzzleNameArray[i] = filteredPuzzles.get(i);
            if (!filteredPuzzleHighScores.get(i).equals("") && !filteredPuzzleHighScores.get(i).equals("0"))
            {
                highScores_Array[i] = getResources().getString(R.string.HighScorePreString) + " " + filteredPuzzleHighScores.get(i);
            }
            else highScores_Array[i] = getResources().getString(R.string.NotCompleteStr);
        }

        for (int i = 0; i < puzzleNameArray.length; i++)
        {
            puzzleNameArray[i] += " \r\n" + highScores_Array[i];
        }

        // Load results into the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, puzzleNameArray);
        listView.setAdapter(adapter);
    }

    // Repopulate ListView. Even if View is returned to
    @Override
    protected void onStart()
    {
        super.onStart();
        populateListView();
    }

    // Go to Download Puzzle Activity
    public void onClickDownloadPuzzle(View pView)
    {
        Intent intent = new Intent(this, DownloadPuzzleActivity.class);
        startActivityForResult(intent, 0);
    }
}
