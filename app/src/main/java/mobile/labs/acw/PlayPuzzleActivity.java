package mobile.labs.acw;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

/**
 * Created by Matt on 25/03/2017.
 */

public class PlayPuzzleActivity extends AppCompatActivity {

    //region Constants
    static final int CLICK_MODE = 0;
    static final int DRAG_MODE = 1;
    private final String REAR_PICTURE_NAME = "REAR";
    private final String REARSWAP_PICTURE_NAME = "REARSWAP";
    //endregion

    //region Members
    private int m_PLAY_MODE, m_Score, m_numColumns, m_tempPositionOne, m_tempPositionTwo, m_FirstImagePressed = -1;
    private PuzzleRecord m_GamePuzzle;
    private Context m_Context;
    private boolean m_WaitingForTurnOver = false, m_IsSecondPress = false;
    private Toast m_ScoreToast;
    private String[] m_PuzzleLayout_PictureSet_FilePaths, m_GameLayout;
    private boolean[] m_CompletedPictures;
    private Bitmap cardrear, cardrearSwap;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_puzzle);

        // Initialisation
        m_PLAY_MODE = getIntent().getIntExtra("Play_Mode", -1);
        m_Context = this;
        String pPuzzleName = getIntent().getStringExtra("PuzzleName");
        PuzzleDBHelper dbHelper = new PuzzleDBHelper(this);
        m_GamePuzzle = dbHelper.getDBPuzzle(pPuzzleName);
        int gameSize = m_GamePuzzle.getLayout().length;
        String[] pictureSet_FilePaths = dbHelper.GetPictureSet(m_GamePuzzle.getPictureSet_ID());
        m_CompletedPictures = new boolean[gameSize];

        // Load Game Card-Front Layout
        m_PuzzleLayout_PictureSet_FilePaths = new String[gameSize];
        for (int i = 0; i < gameSize; i++)
        {
            m_PuzzleLayout_PictureSet_FilePaths[i] = pictureSet_FilePaths[m_GamePuzzle.getLayout()[i] - 1];
        }
        // Load Starting Game Layout
        m_GameLayout = new String[gameSize];
        for (int i = 0; i < gameSize; i++)
        {
            m_GameLayout[i] = REAR_PICTURE_NAME;
        }

        // Scale the Card-rear image
        Bitmap tempBitmap = GetTempBitmap(0);
        cardrear = GetTempBitmap(-1);
        cardrearSwap = GetTempBitmap(-2);
        cardrear = Bitmap.createScaledBitmap(cardrear, tempBitmap.getWidth(), tempBitmap.getHeight(), false);
        cardrearSwap = Bitmap.createScaledBitmap(cardrearSwap, tempBitmap.getWidth(), tempBitmap.getHeight(), false);

        // Initialise and populate the views
        GridView gridView = (GridView)findViewById(R.id.puzzleGridView);
        m_numColumns = m_GamePuzzle.getLayout().length / m_GamePuzzle.getRows();
        gridView.setNumColumns(m_numColumns);
        gridView.setAdapter(new ImageAdapter());

        // Set Game login in OnClick event
        if (m_PLAY_MODE == CLICK_MODE)
        {
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View v, int pPosition, long id)
                {
                    // If a card is not waiting to be turned over
                    if (!m_WaitingForTurnOver) {
                        // Run game-card clicked event
                        GameItemClicked_ClickMode(pPosition);
                    }
                }
            });
        }
        else if (m_PLAY_MODE == DRAG_MODE) // If Drag mode
        {
            // Show the Surrender Button
            Button button = (Button)findViewById(R.id.surrenderPuzzleButton);
            button.setVisibility(View.VISIBLE);
        }
    }

    //region User Game Action Events
    // Card Clicked Game Event - Click Mode
    private void GameItemClicked_ClickMode(int pPosition)
    {
        // First Click on Card Back - If card is not already completed AND if no other card is selected
        if(m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && !m_IsSecondPress && m_FirstImagePressed == -1 && !m_CompletedPictures[pPosition])
        {
            // Show Front Side of image
            m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
            // Next Click is now second Click
            m_IsSecondPress = true;
            // Remember which image was Clicked first
            m_FirstImagePressed = pPosition;
            // Reload View
            RefreshGridView();
        } // Second Click on Card Front - If it is the second Click && If this card is already clicked && if this card is not already completed
        else if (!m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && m_IsSecondPress && m_FirstImagePressed == pPosition && !m_CompletedPictures[pPosition])
        {
            // Display & Update Score
            UpdateScore(-10);
            // Show Rear Side of Image
            m_GameLayout[pPosition] = REAR_PICTURE_NAME;
            // Next Click is now first Click
            m_IsSecondPress = false;
            // Forget the image that was selected first
            m_FirstImagePressed = -1;
            // Reload View
            RefreshGridView();
        } // Second Click on card back - If it is the second Click && If this card is mot already completed && If this card is not the first card Clicked
        else if (m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && m_IsSecondPress && !m_CompletedPictures[pPosition] && m_FirstImagePressed != -1) // Second Click on Card Back
        {
            // If the Card Fronts Match
            if (m_PuzzleLayout_PictureSet_FilePaths[pPosition].equals(m_PuzzleLayout_PictureSet_FilePaths[m_FirstImagePressed]))
            {
                // Set two front facing images as completed
                m_CompletedPictures[pPosition] = true;
                m_CompletedPictures[m_FirstImagePressed] = true;
                // Next Click is First Click
                m_IsSecondPress = false;
                // Forget the image that was selected first
                m_FirstImagePressed = -1;
                // Show Front Side of image
                m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
                // Reload View
                RefreshGridView();
                // Display and Update Score
                UpdateScore(100);
                // Check if game is finished
                CheckIfFinished(false);
            }
            else // If the Cards don't match
            {
                m_WaitingForTurnOver = true;
                // Show the Front of the Second Card
                m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
                // Reload View
                RefreshGridView();
                // Display and Update Score
                UpdateScore(-20);
                // Waot a second and turn them back
                new PauseandResetWrongPictures().execute(pPosition);
            }
        }
    }

    // Card Clicked Game Event - Click Mode
    private void GameItemClicked_DragMode(int pPosition)
    {
        // First Click on Card Back - If card is not already completed AND if no other card is selected
        if(m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && !m_IsSecondPress && m_FirstImagePressed == -1 && !m_CompletedPictures[pPosition])
        {
            // Display & Update Score
            UpdateScore(-10);
            // Show Front Side of image
            m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
            // Next Click is now second Click
            m_IsSecondPress = true;
            // Remember which image was Clicked first
            m_FirstImagePressed = pPosition;
            // Reload View
            RefreshGridView();
        } // Second Click on Card Front - If it is the second Click && If this card is already clicked && if this card is not already completed
        else if (!m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && m_IsSecondPress && m_FirstImagePressed == pPosition && !m_CompletedPictures[pPosition])
        {
            // Show Rear Side of Image
            m_GameLayout[pPosition] = REAR_PICTURE_NAME;
            // Next Click is now first Click
            m_IsSecondPress = false;
            // Forget the image that was selected first
            m_FirstImagePressed = -1;
            // Reload View
            RefreshGridView();
        } // Second Click on card back - If it is the second Click && If this card is mot already completed && If this card is not the first card Clicked
        else if (m_GameLayout[pPosition].equals(REAR_PICTURE_NAME) && m_IsSecondPress && !m_CompletedPictures[pPosition] && m_FirstImagePressed != -1) // Second Click on Card Back
        {
            // Check if he two selected cards are next to each other
            boolean cardsAreBesidesEachOther = GetViewBesidesPosition(pPosition, "left") == m_FirstImagePressed || GetViewBesidesPosition(pPosition, "right") == m_FirstImagePressed
                                            || GetViewBesidesPosition(pPosition, "up") == m_FirstImagePressed || GetViewBesidesPosition(pPosition, "down") == m_FirstImagePressed;
            // If the two cards are not side by side then return
            if (!cardsAreBesidesEachOther)
                return;
            // If the Card Fronts Match
            if (m_PuzzleLayout_PictureSet_FilePaths[pPosition].equals(m_PuzzleLayout_PictureSet_FilePaths[m_FirstImagePressed]))
            {
                // Set two front facing images as completed
                m_CompletedPictures[pPosition] = true;
                m_CompletedPictures[m_FirstImagePressed] = true;
                // Next Click is First Click
                m_IsSecondPress = false;
                // Forget the image that was selected first
                m_FirstImagePressed = -1;
                // Show Front Side of image
                m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
                // Reload View
                RefreshGridView();
                // Display and Update Score
                UpdateScore(160);
                // Check if game is finished
                CheckIfFinished(false);
            }
            else // If the Cards don't match
            {
                m_WaitingForTurnOver = true;
                // Show the Front of the Second Card
                m_GameLayout[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
                // Reload View
                RefreshGridView();
                // Display and Update Score
                UpdateScore(-25);
                // Waot a second and turn them back
                new PauseandResetWrongPictures().execute(pPosition);
            }
        }
    }

    // Card Dragged Game Event - Click Mode
    private void GameItemDragged_DragMode(int pPosition, String pDirection)
    {
        // Update & Display the score
        UpdateScore(-8);

        // Get the position of the view to swap with
        int PositionTwo = GetViewBesidesPosition(pPosition, pDirection);
        // If the view on that side does not exist, return
        if (PositionTwo == -1)
            return;

        // If either image is face up then return
        boolean isImageOne_FaceDown = m_GameLayout[pPosition].equals(REAR_PICTURE_NAME);
        boolean isImageTwo_FaceDown = m_GameLayout[PositionTwo].equals(REAR_PICTURE_NAME);
        if (!isImageOne_FaceDown || !isImageTwo_FaceDown)
            return;

        // Swap the front images
        String temp = m_PuzzleLayout_PictureSet_FilePaths[pPosition];
        m_PuzzleLayout_PictureSet_FilePaths[pPosition] = m_PuzzleLayout_PictureSet_FilePaths[PositionTwo];
        m_PuzzleLayout_PictureSet_FilePaths[PositionTwo] = temp;
        RefreshGridView();

        // Change shows images to the swap card
        m_tempPositionOne = pPosition;
        m_tempPositionTwo = PositionTwo;
        m_GameLayout[pPosition] = REARSWAP_PICTURE_NAME;
        m_GameLayout[PositionTwo] = REARSWAP_PICTURE_NAME;

        // Wait a second and turn them back
        m_WaitingForTurnOver = true;
        new PauseandResetSwappedPictures().execute();
    }
    //endregion

    //region Game Utility Functions
    // Gets the position of the view in the direction specified
    // Returns -1 if the view in that direction does not exist
    private int GetViewBesidesPosition(int pPosition, String pDirection)
    {
        switch (pDirection)
        {
            case "left":
            {
                // If the Image is on the Left hand Side
                if (pPosition % m_numColumns == 0 || pPosition == 0)
                {
                    return -1;
                } // If the Image is not on the Left hand Side
                else return (pPosition - 1);
            }
            case "right":
            {
                // If the Image is on the Right hand Side
                if ((pPosition + 1) % m_numColumns == 0)
                {
                    return -1;
                } // If the Image is not on the Right hand Side
                else return (pPosition + 1);
            }
            case "up":
            {
                // If the Image is on the First Row
                if (pPosition < m_numColumns)
                {
                    return -1;
                } // If the image is not on the First Row
                else return pPosition - m_numColumns;
            }
            case "down":
            {
                // If the Image is on the Last Row
                if (pPosition >= m_numColumns * (m_GamePuzzle.getRows() - 1))
                {
                    return -1;
                } // If the image is not on the Last Row
                else return pPosition + m_numColumns;
            }
        }
        // If incorrect direction then return -1
        return -1;
    }

    // Checks the Game to see if the Puzzle is complete
    // Set pParam to true to Finish whether complete or not
    private void CheckIfFinished(boolean pParam)
    {
        boolean finished = true;
        for (int i = 0; i < m_GameLayout.length; i++)
        {
            if (m_GameLayout[i].equals(REAR_PICTURE_NAME)) {
                finished = false;
                break;
            }
        }
        if (finished || pParam) // If finishing...
        {
            // Declare and build Dialog box
            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_display);
            dialog.setTitle(R.string.PuzzleComplete);
            TextView Title = (TextView) dialog.findViewById(android.R.id.title);
            try
            {
                Title.setGravity(Gravity.CENTER);
            }
            catch (Exception e) {}
            TextView textView = (TextView)dialog.findViewById(R.id.HighScoreTextView);
            textView.setText(getString(R.string.NewScore, m_Score));
            TextView textView2 = (TextView)dialog.findViewById(R.id.WellDoneTextView);

            // If New High Score
            if (m_Score > Integer.parseInt(m_GamePuzzle.getHighScore()))
            {
                SaveHighScore();
                textView2.setText(R.string.NewHighScore);
            }
            else textView2.setText(R.string.WellDone);

            // When Dialog button is pressed, close the Puzzle game
            Button button = (Button)dialog.findViewById(R.id.MainMenu);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });;

            // Show the dialog
            dialog.show();
        }
    }

    // Updates the database with the score of the current game
    private void SaveHighScore()
    {
        // Get database helper
        PuzzleDBHelper dbHelper = new PuzzleDBHelper(this);
        // Run update command
        dbHelper.UpdateHighScore(m_GamePuzzle.getID(), m_Score);
    }

    // Updates Score Updates the Score TextView. Also display Toast showing Score Change.
    private void UpdateScore(int pPoints)
    {
        // Accumulate points
        m_Score += pPoints;

        // Build Toast text
        String outputText;
        if (pPoints >= 0)
            outputText = "+" + pPoints;
        else outputText = Integer.toString(pPoints);
        if(m_ScoreToast != null)
        {
            m_ScoreToast.cancel();
        }
        // Show Toast
        m_ScoreToast = Toast.makeText(this, outputText + " Points!", Toast.LENGTH_SHORT);
        m_ScoreToast.show();

        // Update Score Text
        TextView tView = (TextView)findViewById(R.id.downloadPuzzleTextView);
        tView.setText("Score: " + m_Score);
    }

    // Finished the game whether the puzzle is complete or not
    public void GiveUp_DragMode(View v)
    {
        CheckIfFinished(true);
    }
    //endregion

    //region Async Tasks
    // Async Task which waits 1 second before turning the two selected images face down
    private class PauseandResetWrongPictures extends AsyncTask<Integer, Void, Integer>
    {
        @Override
        public Integer doInBackground(Integer... Params)
        {
            // Waits for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            // return position of second image
            return Params[0];
        }

        // After Waiting...
        @Override
        protected void onPostExecute(Integer pPosition)
        {
            // Show Rear Side of both images
            m_GameLayout[pPosition] = REAR_PICTURE_NAME;
            m_GameLayout[m_FirstImagePressed] = REAR_PICTURE_NAME;
            // Next Click is First Click
            m_IsSecondPress = false;
            // Forget the image that was selected first
            m_FirstImagePressed = -1;
            // Reload View
            GridView gridView = (GridView)findViewById(R.id.puzzleGridView);
            gridView.setAdapter(new ImageAdapter());
            m_WaitingForTurnOver = false;
        }
    }

    // Async Task which waits 1 second before turning the two swapped images to their normal image
    private class PauseandResetSwappedPictures extends AsyncTask<Integer, Void, Void>
    {
        @Override
        public Void doInBackground(Integer... Params)
        {
            // Watis 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {
            // Show Rear Side of both images
            m_GameLayout[m_tempPositionOne] = REAR_PICTURE_NAME;
            m_GameLayout[m_tempPositionTwo] = REAR_PICTURE_NAME;
            // Next Click is First Click
            m_IsSecondPress = false;
            // Forget the image that was selected first
            m_FirstImagePressed = -1;
            // Reload View
            GridView gridView = (GridView)findViewById(R.id.puzzleGridView);
            gridView.setAdapter(new ImageAdapter());
            m_WaitingForTurnOver = false;
        }
    }
    //endregion

    //region Layout Functions
    // Updates the GridViews Images
    private void RefreshGridView()
    {
        // Creates a new ImageAdapter for the Display View
        GridView gridView = (GridView)findViewById(R.id.puzzleGridView);
        gridView.setAdapter(new ImageAdapter());
    }

    // Image Adapter class used to place images in a GridView
    public class ImageAdapter extends BaseAdapter
    {
        private int imagePosition = -1;
        private float x1 = 0, y1 = 0;
        private boolean m_Downed = false;

        public ImageAdapter() {
        }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            ImageView imageView;
            if (convertView == null)
            {
                imagePosition++;
                imageView = new ImageView(m_Context);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding(0, 0, 0, 0);
            }
            else imageView = (ImageView) convertView;
            Bitmap temp = GetLayoutBitmap(imagePosition);
            imageView.setImageBitmap(temp);

            if (m_PLAY_MODE == DRAG_MODE)
            {
                imageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(final View view, final MotionEvent event) {
                        if (m_WaitingForTurnOver)
                            return true;
                        GridView gView = (GridView) view.getParent();
                        int myPosition = gView.getPositionForView(view);
                        String direction;
                        switch (event.getAction()) {
                            case (MotionEvent.ACTION_DOWN):
                                x1 = event.getX();
                                y1 = event.getY();
                                m_Downed = true;
                                break;

                            case (MotionEvent.ACTION_UP): {
                                if (!m_Downed)
                                    break;

                                float x2 = event.getX();
                                float y2 = event.getY();
                                float dx = x1 - x2;
                                float dy = y1 - y2;

                                final int minSpeed = 4;

                                // Use dx and dy to determine the movement vector
                                if (Math.abs(dx) < minSpeed || Math.abs(dy) < minSpeed) {
                                        GameItemClicked_DragMode(myPosition);
                                }
                                m_Downed = false;
                                break;
                            }

                            case (MotionEvent.ACTION_MOVE): {
                                if (!m_Downed)
                                    break;

                                float x2 = event.getX();
                                float y2 = event.getY();
                                float dx = x1 - x2;
                                float dy = y1 - y2;

                                final int minSpeed = 4;

                                // Use dx and dy to determine the movement vector
                                if (Math.abs(dx) > Math.abs(dy) && (Math.abs(dx) > minSpeed || Math.abs(dy) > minSpeed)) {
                                    if (dx > 0) {
                                        direction = "left";
                                        GameItemDragged_DragMode(myPosition, direction);
                                    } else {
                                        direction = "right";
                                        GameItemDragged_DragMode(myPosition, direction);
                                    }
                                } else if (Math.abs(dx) <= Math.abs(dy) && (Math.abs(dx) > minSpeed || Math.abs(dy) > minSpeed)) {
                                    if (dy > 0) {
                                        direction = "up";
                                        GameItemDragged_DragMode(myPosition, direction);
                                    } else {
                                        direction = "down";
                                        GameItemDragged_DragMode(myPosition, direction);
                                    }

                                }
                                else GameItemClicked_DragMode(myPosition);
                                x1 = 0;
                                y1 = 0;
                                m_Downed = false;
                            }
                        }
                        return true;
                    }
                });
            }

            return imageView;
        }

        public int getCount() {
            return m_GamePuzzle.getLayout().length;
        }

        public Object getItem(int position)
        {
            return null;
        }

        public long getItemId(int position)
        {
            return 0;
        }

        private Bitmap GetLayoutBitmap(int pGridPosition)
        {
            try
            {
                String fileName = m_GameLayout[pGridPosition];
                if (fileName.equals(REAR_PICTURE_NAME))
                {
                    return cardrear;
                }
                else if (fileName.equals(REARSWAP_PICTURE_NAME))
                {
                    return cardrearSwap;
                }
                FileInputStream reader = getApplicationContext().openFileInput(fileName);
                try
                {
                    Bitmap bitmap = BitmapFactory.decodeStream(reader);
                    return bitmap;
                }
                catch (Exception e) {}
                finally
                {
                    reader.close();
                }
            }
            catch (Exception e) {}
            return null;
        }
    }

    // Returns temporary Bitmap so as to provide image parameters
    // Also returns the images for the CardRear and CardRearSwap
    private Bitmap GetTempBitmap(int pGridPosition)
    {
        try
        {
            if (pGridPosition == -1)
            {
                return BitmapFactory.decodeResource(getResources(), R.drawable.cardrear_hard);
            }
            else if (pGridPosition == -2)
            {
                return BitmapFactory.decodeResource(getResources(), R.drawable.cardrear_swap);
            }
            String fileName = m_PuzzleLayout_PictureSet_FilePaths[pGridPosition];
            FileInputStream reader = getApplicationContext().openFileInput(fileName);
            try
            {
                Bitmap bitmap = BitmapFactory.decodeStream(reader);
                return bitmap;
            }
            catch (Exception e) {}
            finally
            {
                reader.close();
            }
        }
        catch (Exception e) {}
        return null;
    }
    //endregion
}
