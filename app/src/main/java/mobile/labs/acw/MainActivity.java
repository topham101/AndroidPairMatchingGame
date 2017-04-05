package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    static final int SELECT_PUZZLE = 1;
    static final int CLICK_MODE = 0;
    static final int DRAG_MODE = 1;

    static String m_SelectedPuzzleName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //region Button Methods
    // Go to Select Puzzle Activity
    public void onClickSelectPuzzle(View pView)
    {
        Intent intent = new Intent(this, SelectPuzzleActivity.class);
        startActivityForResult(intent, SELECT_PUZZLE);
    }

    // Play Game in Click Mode
    public void onClickPlayClick(View pView)
    {
        // Pass Selected Puzzle information to game activity
        Intent intent = new Intent(this, PlayPuzzleActivity.class);
        intent.putExtra("Play_Mode", CLICK_MODE);
        intent.putExtra("PuzzleName", m_SelectedPuzzleName);
        startActivity(intent);
    }

    // Play Game in Click Mode
    public void onClickPlayDrag(View pView)
    {
        // Pass Selected Puzzle information to game activity
        Intent intent = new Intent(this, PlayPuzzleActivity.class);
        intent.putExtra("Play_Mode", DRAG_MODE);
        intent.putExtra("PuzzleName", m_SelectedPuzzleName);
        startActivity(intent);
    }
    //endregion

    //region Activity Return Method
    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData){
        Button pickPuzzleButton = (Button)findViewById(R.id.selectPuzzleButton);

        // If a puzzle is selected
        if(pResultCode == RESULT_OK && pRequestCode == SELECT_PUZZLE)
        {
            String puzzleName = pData.getStringExtra("Puzzle_Name");
            pickPuzzleButton.setText(getResources().getString(R.string.SelectedPuzzle) + " " + puzzleName);
            m_SelectedPuzzleName = puzzleName;
        }
        else // If a puzzle is not returned
        {
            // Set view text to default
            m_SelectedPuzzleName = null;
            pickPuzzleButton.setText(R.string.MainPageSelectPuzzlePage);
        }

        // If a puzzle is selected, Display the play game buttons
        if (m_SelectedPuzzleName != null)
        {
            Button playButton = (Button)findViewById(R.id.playPuzzleClickButton);
            playButton.setVisibility(View.VISIBLE);
            playButton = (Button)findViewById(R.id.playPuzzleDragButton);
            playButton.setVisibility(View.VISIBLE);
        }
        else // If a puzzle is not selected, hide the play game buttons
        {
            Button playButton = (Button)findViewById(R.id.playPuzzleClickButton);
            playButton.setVisibility(View.INVISIBLE);
            playButton = (Button)findViewById(R.id.playPuzzleDragButton);
            playButton.setVisibility(View.INVISIBLE);
        }
    }
    //endregion
}
